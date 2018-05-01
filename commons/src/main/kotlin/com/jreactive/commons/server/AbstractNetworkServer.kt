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

package com.jreactive.commons.server

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelOption
import io.netty.channel.EventLoopGroup
import io.netty.channel.socket.ServerSocketChannel

/**
 * @param T type of the server (Epol, NIO etc.)
 */
abstract class AbstractNetworkServer<T : ServerSocketChannel>(private val host: String,
                                                              private val port: Int) : NetworkServer {

    private val server: ServerBootstrap = ServerBootstrap()

    private lateinit var serverFuture: ChannelFuture

    protected lateinit var bossGroup: EventLoopGroup

    protected lateinit var workGroup: EventLoopGroup

    protected lateinit var type: Class<T>

    override fun start(): Boolean {
        server
                .group(bossGroup, workGroup)
                .channel(type)
                .option(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true)

        serverFuture = server.bind(host, port).sync()
        return serverFuture.isDone
    }

    override fun stop(): Boolean {
        return serverFuture.channel().closeFuture().sync().isSuccess &&
                workGroup.shutdownGracefully().isSuccess &&
                bossGroup.shutdownGracefully().isSuccess
    }

    override fun host(): String {
        return host
    }

    override fun port(): Int {
        return port
    }

    override fun childHandler(handler: ChannelHandler) {
        server.childHandler(handler)
    }

    override fun handler(handler: ChannelHandler) {
        server.handler(handler)
    }

}

