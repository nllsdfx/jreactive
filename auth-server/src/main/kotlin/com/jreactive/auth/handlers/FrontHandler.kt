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

package com.jreactive.auth.handlers

import akka.actor.AbstractActor
import akka.actor.ActorRef
import akka.actor.Props
import com.jreactive.auth.messages.DestroyMessage
import com.jreactive.auth.messages.PacketMsg
import io.netty.channel.Channel
import io.netty.channel.ChannelId

class FrontHandler : AbstractActor() {

    private val sessions: HashMap<ChannelId, ActorRef> = hashMapOf()

    override fun createReceive(): Receive {
        return receiveBuilder()
                .match(Channel::class.java, { sessions[it.id()] = context.actorOf(com.jreactive.auth.server.props(it, self)) })
                .match(PacketMsg::class.java, { sessions[it.channelId]?.tell(it, ActorRef.noSender()) })
                .match(DestroyMessage::class.java, { context.stop(sessions.remove(it.channelId)) })
                .build()
    }
}

fun props(): Props {
    return Props.create(FrontHandler::class.java)
}