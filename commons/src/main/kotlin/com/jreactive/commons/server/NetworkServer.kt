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

import io.netty.channel.ChannelHandler

/**
 * Server to run.
 */
interface NetworkServer {

    /**
     * Runs server. Method returns only after service completely started.
     */
    fun start(): Boolean

    /**
     * Stops server. Method returns only after service completely stopped.
     */
    fun stop(): Boolean

    /**
     * Returns ip of the server.
     */
    fun host(): String

    /**
     * Returns port of the server.
     */
    fun port(): Int

    /**
     * Sets a child handler for the server.
     */
    fun childHandler(handler: ChannelHandler)

    /**
     * Sets a handler for the server.
     */
    fun handler(handler: ChannelHandler)
}