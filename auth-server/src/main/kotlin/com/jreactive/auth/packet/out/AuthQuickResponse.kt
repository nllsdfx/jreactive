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

package com.jreactive.auth.packet.out

import com.jreactive.auth.messages.AuthResult
import com.jreactive.auth.server.AuthStatus
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled

object AuthQuickResponse : AuthSendablePacket(-1) {

    fun banned(): ByteBuf {
        val b = Unpooled.directBuffer()
        wui8(AuthStatus.STATUS_CHALLENGE.ordinal, b)
        wui8(0x00, b)
        wui8(AuthResult.WOW_FAIL_BANNED.code, b)
        return b
    }

    fun wrongPassword(): ByteBuf {
        val b = Unpooled.directBuffer()
        wui8(AuthStatus.STATUS_LOGON_PROOF.ordinal, b)
        wui8(AuthResult.WOW_FAIL_INCORRECT_PASSWORD.code, b)
        return b
    }

    override fun write(): ByteBuf {
        throw NotImplementedError()
    }

}