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

import com.jreactive.commons.packet.PacketReader
import com.jreactive.commons.packet.ReadablePacket
import io.netty.buffer.ByteBuf

class AUTH_LOGON_CHALLENGE_C(
        val error: Int,
        val size: Int,
        val gameName: String,
        val majorVersion: Int,
        val midVersion: Int,
        val minorVersion: Int,
        val build: Int,
        val platform: String,
        val os: String,
        val country: String,
        val timeZone: Long,
        val ip: Long,
        val login: String) : ReadablePacket(id = 0x00) {
}

object AUTH_LOGON_READER : PacketReader() {
    override fun readPacket(b: ByteBuf): ReadablePacket {
        return AUTH_LOGON_CHALLENGE_C(
                error = rui8(b),
                size = rui16(b),
                gameName = String(rb(b, 4)),
                majorVersion = rui8(b),
                midVersion = rui8(b),
                minorVersion = rui8(b),
                build = rui16(b),
                platform = String(rb(b, 4)).reversed(),
                os = String(rb(b, 4)).reversed(),
                country = String(rb(b, 4)).reversed(),
                timeZone = rui32(b),
                ip = rui32(b),
                login = String(rb(b, rui8(b)))
        )
    }
}
