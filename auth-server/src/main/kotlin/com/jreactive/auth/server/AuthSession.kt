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
import com.jreactive.auth.dao.IPBanCheck
import com.jreactive.auth.messages.PacketMsg
import com.jreactive.auth.packet.`in`.AUTH_LOGON_CHALLENGE_C
import com.jreactive.auth.packet.out.AuthQuickResponse
import io.netty.buffer.ByteBuf
import io.netty.channel.Channel
import java.net.InetSocketAddress

private val daoMgr = aSystem.actorOf(Props.create(AccountDAOManager::class.java), "daoManager")

class AuthSession(private val channel: Channel, private val manager: ActorRef) : AbstractActor() {

    private var status: AuthStatus = AuthStatus.STATUS_CHALLENGE

    private val handlers: Map<AuthStatus, (PacketMsg) -> Unit> = mapOf(
            AuthStatus.STATUS_CHALLENGE to ::handleLogonChallenge
    )

    override fun preStart() {
        val ia = channel.remoteAddress() as InetSocketAddress
        daoMgr.tell(IPBanCheck(ia.address.hostAddress, ::bannedCallback), self)
    }

    override fun createReceive(): Receive {
        return receiveBuilder()
                .match(PacketMsg::class.java, ::readPacket)
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
        val packet = AUTH_LOGON_CHALLENGE_C()
        packet.read(packetMsg.msg)
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

fun props(channel: Channel, manager: ActorRef): Props {
    return Props.create(AuthSession::class.java, channel, manager)
}

enum class AuthStatus {
    STATUS_CHALLENGE,
    STATUS_LOGON_PROOF,
    STATUS_RECONNECT_PROOF,
    STATUS_AUTHED,
    STATUS_CLOSED
}