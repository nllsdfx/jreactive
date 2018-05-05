/*
 * Copyright (C) 2018 JReactive <http://www.jreactive.com/>
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.jreactive.auth.server

import akka.actor.AbstractActor
import akka.actor.ActorRef
import akka.actor.Props
import com.jreactive.auth.aSystem
import com.jreactive.auth.dao.AccountDAOManager
import com.jreactive.auth.dao.AccountInfoMsg
import com.jreactive.auth.dao.IPBanCheck
import com.jreactive.auth.entity.Account
import com.jreactive.auth.entity.UserAccount
import com.jreactive.auth.messages.AuthResult
import com.jreactive.auth.messages.PacketMsg
import com.jreactive.auth.packet.`in`.AUTH_LOGON_PROOF_READER
import com.jreactive.auth.packet.`in`.AUTH_LOGON_READER
import com.jreactive.auth.packet.out.AuthQuickResponse
import com.jreactive.auth.util.AuthHelper
import com.jreactive.commons.packet.SendablePacket
import com.jreactive.commons.security.Role
import com.jreactive.commons.util.BigNumber
import com.jreactive.commons.util.io.ArrayUtil
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.Channel
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.math.BigInteger
import java.net.InetSocketAddress
import java.nio.charset.Charset
import java.security.MessageDigest
import java.util.*
import kotlin.experimental.xor

private val daoMgr = aSystem.actorOf(Props.create(AccountDAOManager::class.java), "daoManager")

class AuthSession(private val channel: Channel, private val manager: ActorRef) : AbstractActor() {

    private var status: AuthStatus = AuthStatus.STATUS_CHALLENGE

    private var build: Int = 0

    private lateinit var info: AccountInfo

    private lateinit var ip: String

    private lateinit var B: BigNumber

    private lateinit var b: BigNumber

    private var s: BigNumber? = null

    private var v: BigNumber? = null

    private val handlers: Map<AuthStatus, (PacketMsg) -> Boolean> = mapOf(
            AuthStatus.STATUS_CHALLENGE to ::handleLogonChallenge,
            AuthStatus.STATUS_LOGON_PROOF to ::handleLogonProof
    )

    override fun preStart() {
        val ia = channel.remoteAddress() as InetSocketAddress
        ip = ia.address.hostAddress
        daoMgr.tell(IPBanCheck(ip, ::bannedCallback), self)
    }

    override fun createReceive(): Receive {
        return receiveBuilder()
                .match(PacketMsg::class.java, ::readPacket)
                .match(AccountInfoArray::class.java, ::logonChallengeCallback)
                .build()
    }

    private fun readPacket(packetMsg: PacketMsg) {

        val status = AuthStatus.values()[packetMsg.id]

        if (status != this.status) {
            destroy()
        }


        if (!handlers[status]!!.invoke(packetMsg)) {
//            destroy() // todo close channel after sending data
        }

        packetMsg.msg.release()

    }

    private fun handleLogonProof(packetMsg: PacketMsg): Boolean {
        status = AuthStatus.STATUS_CLOSED
        val packet = AUTH_LOGON_PROOF_READER.readPacket(packetMsg.msg)

        return checkPassword(packet.a, packet.m1)
    }

    private fun checkPassword(a: ByteArray, m1: ByteArray): Boolean {

        val sha = MessageDigest.getInstance("SHA-1")

        sha.update(a)
        sha.update(B.asByteArray(32))
        val u = BigNumber()
        u.setBinary(sha.digest())
        val A = BigNumber()
        A.setBinary(a)
        val S = A.multiply(v!!.modPow(u, AccountUtils.N)).modPow(
                b, AccountUtils.N)

        val t1 = ByteArray(16)
        val vK = ByteArray(40)

        val t = S.asByteArray(32)
        for (i in 0..15) {
            t1[i] = t[i * 2]
        }
        sha.update(t1)
        var t2 = sha.digest()
        for (i in 0..19) {
            vK[i * 2] = t2[i]
        }
        for (i in 0..15) {
            t1[i] = t[i * 2 + 1]
        }
        sha.update(t1)
        t2 = sha.digest()
        for (i in 0..19) {
            vK[i * 2 + 1] = t2[i]
        }

        val hash: ByteArray
        sha.update(AccountUtils.N.asByteArray(32))
        hash = sha.digest()
        sha.update(AccountUtils.g.asByteArray(1))
        val gH = sha.digest()
        for (i in 0..19) {
            hash[i] = hash[i] xor gH[i]
        }

        val t4: ByteArray
        sha.update(info.login.toByteArray(Charset.forName("UTF-8")))
        t4 = sha.digest()

        sha.update(hash)
        sha.update(t4)
        sha.update(this.s!!.asByteArray(32))
        sha.update(A.asByteArray(32))
        sha.update(B.asByteArray(32))
        sha.update(vK)

        val sh = sha.digest()

        if (Arrays.equals(sh, m1)) {

            sha.update(A.asByteArray(32))
            sha.update(sh)
            sha.update(vK)
            ArrayUtil.reverse(vK)

            val key = BigInteger(1, vK).toString(16).toUpperCase()

            transaction {
                Account.update({Account.userName eq info.login.toLowerCase()}) {
                    it[sessionKey] = key
                }
            }

            val packet = Unpooled.directBuffer()
            SendablePacket.wui8(AuthStatus.STATUS_LOGON_PROOF.ordinal, packet)
            SendablePacket.wui8(0, packet)
            SendablePacket.wb(sha.digest(), packet)
            packet.writeInt(0x00800000) // 0x01 = GM, 0x08 = Trial, 0x00800000 = Pro pass (arena tournament)
            packet.writeInt(0)
            SendablePacket.wui16(0, packet)

            sendPacket(packet)

            status = AuthStatus.STATUS_AUTHED

            return true

        } else {
            sendPacket(AuthQuickResponse.wrongPassword())
            //todo handle lot's of wrong attempts to login
            return false
        }

    }


    private fun handleLogonChallenge(packetMsg: PacketMsg): Boolean {
        status = AuthStatus.STATUS_CLOSED
        val packet = AUTH_LOGON_READER.readPacket(packetMsg.msg)
        build = packet.build
        daoMgr.tell(AccountInfoMsg(packet.login), self)
        return true
    }

    private fun logonChallengeCallback(rs: AccountInfoArray) {
        val packet = Unpooled.directBuffer()
        SendablePacket.wui8(AuthStatus.STATUS_CHALLENGE.ordinal, packet)
        SendablePacket.wui8(0x00, packet)

        if (rs.user == null) {
            SendablePacket.wui8(AuthResult.WOW_FAIL_UNKNOWN_ACCOUNT.code, packet)
            sendPacket(packet)
            return
        }

        if (!AuthHelper.isAcceptedClientBuild(build)) {
            SendablePacket.wui8(AuthResult.WOW_FAIL_VERSION_INVALID.code, packet)
            sendPacket(packet)
            return
        }

        info = loadAccountInfo(rs.user)

        if (!info.isLocked) {
            if (info.lastIP != ip) {
                SendablePacket.wui8(AuthResult.WOW_FAIL_LOCKED_ENFORCED.code, packet)
                sendPacket(packet)
                return
            }
        }

        if (info.isBanned) {
            if (info.isPermanentlyBanned) {
                SendablePacket.wui8(AuthResult.WOW_FAIL_BANNED.code, packet)
                sendPacket(packet)
                return
            } else {
                SendablePacket.wui8(AuthResult.WOW_FAIL_SUSPENDED.code, packet)
                sendPacket(packet)
                return
            }
        }

        SendablePacket.wui8(AuthResult.WOW_SUCCESS.code, packet)
        status = AuthStatus.STATUS_LOGON_PROOF

        // Get the password from the account table, upper it, and make the SRP6 calculation
        val databaseV = rs.user.v
        val databaseS = rs.user.s

        val variable: HashMap<String, BigNumber>


        // multiply with 2 since bytes are stored as hexstring
        if (databaseV.length != BufferSizes.SRP_6_V.size * 2 || databaseS.length != BufferSizes.SRP_6_S.size * 2) {
            val rI = rs.user.shaPass
            variable = AccountUtils.calculateVSFields(rI)
            s = variable["s"]
            v = variable["v"]

            transaction {
                rs.user.v = v!!.asHexStr()
                rs.user.s = s!!.asHexStr()
            }

        } else {
            s = BigNumber()
            v = BigNumber()
            s!!.setHexStr(databaseS)
            v!!.setHexStr(databaseV)

        }

        val bmap = AccountUtils.getB(v!!)
        B = bmap["B"]!!
        b = bmap["b"]!!

        val unk3 = BigNumber()
        unk3.setRand(16)

        SendablePacket.wb(B.asByteArray(32), packet)
        SendablePacket.wui8(1, packet)
        SendablePacket.wb(AccountUtils.g.asByteArray(1), packet)
        SendablePacket.wui8(32, packet)
        SendablePacket.wb(AccountUtils.N.asByteArray(32), packet)
        SendablePacket.wb(s!!.asByteArray(32), packet)
        SendablePacket.wb(unk3.asByteArray(16), packet)
        SendablePacket.wui8(0, packet)


        sendPacket(packet)

    }


    private fun loadAccountInfo(user: UserAccount): AccountInfo {
        return AccountInfo(
                id = user.id.value,
                login = user.login.toUpperCase(),
                isLocked = user.locked,
                lastIP = user.lastIP,
                failedLogins = user.failedLogins,
                isBanned = false,
                isPermanentlyBanned = false,
                role = Role.PLAYER,
                token = user.tokenKey
        )
    }

    private fun bannedCallback(banned: Boolean) {

        if (banned) {
            sendPacket(AuthQuickResponse.banned())
        }

    }

    private fun sendPacket(b: ByteBuf) {
        channel.writeAndFlush(b)
    }

    private fun destroy() {
        channel.flush().close()
    }


}

enum class AuthStatus {
    STATUS_CHALLENGE,
    STATUS_LOGON_PROOF,
    STATUS_RECONNECT_PROOF,
    STATUS_AUTHED,
    STATUS_CLOSED
}

class AccountInfo(
        val id: Long,
        val login: String,
        val isLocked: Boolean,
        val lastIP: String,
        val failedLogins: Long,
        val isBanned: Boolean,
        val isPermanentlyBanned: Boolean,
        val role: Role,
        val token: String = ""
)

class AccountInfoArray(val user: UserAccount?)


fun props(channel: Channel, manager: ActorRef): Props {
    return Props.create(AuthSession::class.java, channel, manager)
}