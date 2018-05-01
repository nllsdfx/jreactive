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

package com.jreactive.auth.packet.`in`

import com.jreactive.commons.packet.ReadablePacket
import io.netty.buffer.ByteBuf

class AUTH_LOGON_CHALLENGE_C : ReadablePacket(id = 0x00) {

    private var error: Int? = null
    private var size: Int? = null
    private lateinit var gameName: String
    private var majorVersion: Int? = null
    private var midVersion: Int? = null
    private var minorVersion: Int? = null
    private var build: Int? = null
    private lateinit var platform: String
    private lateinit var os: String
    private lateinit var country: String
    private var timeZone: Long? = null
    private var ip: Long? = null
    private lateinit var login: String

    override fun read(b: ByteBuf) {
        error = rui8(b)
        size = rui16(b)
        gameName = String(rb(b, 4))
        majorVersion = rui8(b)
        midVersion = rui8(b)
        minorVersion = rui8(b)
        build = rui16(b)
        platform = String(rb(b, 4))
        os = String(rb(b, 4))
        country = String(rb(b, 4))
        timeZone = rui32(b)
        ip = rui32(b)
        val loginLen = rui8(b)
        login = String(rb(b, loginLen))

    }
}