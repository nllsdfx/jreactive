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

package com.jreactive.auth

import akka.actor.ActorSystem
import com.jreactive.auth.config.DataBaseConfig
import com.jreactive.auth.handlers.AuthHandlersInitializer
import com.jreactive.commons.server.TCPServer
import org.jetbrains.exposed.sql.Database

val aSystem = ActorSystem.create("AuthSystem")

fun main(args: Array<String>) {

    if (!startDB()) {
        return
    }

    val server = TCPServer("localhost", 3724)
    server.childHandler(AuthHandlersInitializer())
    server.start()
}

private fun startDB(): Boolean {
    return try {
        Database.connect(DataBaseConfig.dataSource())
        true
    } catch (ex: Exception) {
        false
    }
}