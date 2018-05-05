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

class AUTH_LOGON_PROOF_C(
    val a: ByteArray,
    val m1: ByteArray,
    val crc_hash: ByteArray,
    val numberOfKeys: Int,
    val securityFlags: Int
) : ReadablePacket(id = 0x01)

object AUTH_LOGON_PROOF_READER : PacketReader() {
    override fun readPacket(b: ByteBuf): AUTH_LOGON_PROOF_C {
        return AUTH_LOGON_PROOF_C(
                a = rb(b, 32),
                m1 = rb(b, 20),
                crc_hash = rb(b, 20),
                numberOfKeys = rui8(b),
                securityFlags = rui8(b)
        )
    }

}