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

package com.jreactive.commons.packet

import io.netty.buffer.ByteBuf

abstract class ReadablePacket(id: Int) : Packet(id) {

    /**
     * Reads contend of the buffer and tries
     * to create packet.
     */
    abstract fun read(b: ByteBuf)

    /**
     * Reads unsigned int 8.
     */
    protected fun rui8(b: ByteBuf): Int {
        return b.readByte().toInt() and 0xFF
    }

    /**
     * Reads unsigned int 16
     */
    protected fun rui16(b: ByteBuf): Int {
        return b.readShortLE().toInt()
    }

    /**
     * Reads bytes of given length
     */
    protected fun rb(b: ByteBuf, len: Int): ByteArray {
        val bs = ByteArray(len)
        b.readBytes(bs)
        return bs
    }

    /**
     * Reads unsigned int 32
     */
    protected fun rui32(b: ByteBuf): Long {
        return Integer.toUnsignedLong(b.readIntLE())
    }


}