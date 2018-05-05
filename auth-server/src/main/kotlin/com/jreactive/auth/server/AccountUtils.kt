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
package com.jreactive.auth.server

import com.jreactive.commons.util.BigNumber
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*

/**
 * The Class AccountUtils.
 */
object AccountUtils {

    /**
     * The Constant N.
     */
    val N = BigNumber(
            "894B645E89E1535BBDAD5B8B290650530801B18EBFBF5E8FAB3C82872A3E9BB7")

    /**
     * The Constant g.
     */
    val g = BigNumber("7")

    /**
     * The Constant k.
     */
    val k = BigNumber("3")

    /**
     * Sets the vs fields.
     *
     * @param Ir the ir
     * @return the hash map
     */
    fun calculateVSFields(Ir: String): HashMap<String, BigNumber> {

        val I = BigNumber(Ir)
        val res = HashMap<String, BigNumber>()
        val hash = I.asByteArray(20)

        val length = hash.size
        for (i in 0 until length / 2) {
            val j = hash[i]
            hash[i] = hash[length - 1 - i]
            hash[length - 1 - i] = j
        }
        // System.out.println("passhash:"+new BigInteger(1,
        // hash).toString(16).toUpperCase());
        val s = BigNumber()
        s.setRand(32)
        // System.out.println("s: " + s.asHexStr());

        val sha = MessageDigest.getInstance("SHA-1")

        sha!!.update(s.asByteArray(32))
        sha.update(hash)
        val x = BigNumber()
        x.setBinary(sha.digest())
        // System.out.println("x: " + x.asHexStr());
        val verifier = g.modPow(x, N)
        // System.out.println("v: " + verifier.asHexStr());
        res["v"] = verifier
        res["s"] = s
        return res
    }

    /**
     * Gets the b.
     *
     * @param v the v
     * @return the b
     */
    fun getB(v: BigNumber): BigNumber {

        val b = BigNumber()
        b.setRand(19)
        val gmod = g.modPow(b, N)
        return v.multiply(k).add(gmod).mod(N)

    }
}

enum class BufferSizes(val size: Int) {
    SRP_6_V(0x20),
    SRP_6_S(0x20)
}