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
import com.jreactive.auth.entity.UserAccount
import com.jreactive.auth.messages.AuthResult
import com.jreactive.auth.messages.PacketMsg
import com.jreactive.auth.packet.`in`.AUTH_LOGON_READER
import com.jreactive.auth.packet.out.AuthQuickResponse
import com.jreactive.auth.util.AuthHelper
import com.jreactive.commons.packet.SendablePacket
import com.jreactive.commons.security.Role
import com.jreactive.commons.util.BigNumber
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.Channel
import org.jetbrains.exposed.sql.transactions.transaction
import java.net.InetSocketAddress
import java.util.*

private val daoMgr = aSystem.actorOf(Props.create(AccountDAOManager::class.java), "daoManager")

class AuthSession(private val channel: Channel, private val manager: ActorRef) : AbstractActor() {

    private var status: AuthStatus = AuthStatus.STATUS_CHALLENGE

    private var build: Int = 0

    private lateinit var info: AccountInfo

    private lateinit var ip: String

    private lateinit var B: BigNumber

    private val handlers: Map<AuthStatus, (PacketMsg) -> Unit> = mapOf(
            AuthStatus.STATUS_CHALLENGE to ::handleLogonChallenge
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

        handlers[status]?.invoke(packetMsg)
        packetMsg.msg.release()

    }

    private fun handleLogonChallenge(packetMsg: PacketMsg) {
        status = AuthStatus.STATUS_CLOSED
        val packet = AUTH_LOGON_READER.readPacket(packetMsg.msg)
        build = packet.build
        daoMgr.tell(AccountInfoMsg(packet.login), self)

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

        if (!AuthHelper.IsAcceptedClientBuild(build)) {
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
        val rI = rs.user.shaPass
        val databaseV = rs.user.v
        val databaseS = rs.user.s

        val variable: HashMap<String, BigNumber>

        val s: BigNumber?
        val v: BigNumber?

        // multiply with 2 since bytes are stored as hexstring
        if (databaseV.length != BufferSizes.SRP_6_V.size * 2 || databaseS.length != BufferSizes.SRP_6_S.size * 2) {
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
            s.setHexStr(databaseS)
            v.setHexStr(databaseV)

        }

        val B = AccountUtils.getB(v!!)

        val unk3 = BigNumber()
        unk3.setRand(16 )

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