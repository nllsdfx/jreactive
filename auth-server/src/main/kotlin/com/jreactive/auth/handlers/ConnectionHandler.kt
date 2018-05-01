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

import akka.actor.ActorRef
import com.jreactive.auth.aSystem
import com.jreactive.auth.messages.DestroyMessage
import com.jreactive.auth.messages.PacketMsg
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter

private val frontActor = aSystem.actorOf(props())

class ConnectionHandler : ChannelInboundHandlerAdapter() {


    override fun channelRegistered(ctx: ChannelHandlerContext) {
        frontActor.tell(ctx.channel(), ActorRef.noSender())
    }

    override fun channelRead(ctx: ChannelHandlerContext, data: Any) {
        val msg = data as ByteBuf
        frontActor.tell(PacketMsg(msg.readByte().toInt() and 0xFF, ctx.channel().id(), msg), ActorRef.noSender())
    }

    override fun channelUnregistered(ctx: ChannelHandlerContext) {
       frontActor.tell(DestroyMessage(ctx.channel().id()), ActorRef.noSender())
    }
}