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
package com.jreactive.commons.util

import java.math.BigInteger
import java.security.SecureRandom

/**
 * The Class BigNumber.
 */
class BigNumber {

    /** The big integer.  */
    private var bigInteger: BigInteger? = null

    /**
     * Instantiates a new big number.
     */
    constructor() {

        this.bigInteger = BigInteger.ZERO
    }

    /**
     * Instantiates a new big number.
     *
     * @param bigInteger
     * the big integer
     */
    constructor(bigInteger: BigInteger) {

        this.bigInteger = bigInteger.abs()
    }

    /**
     * Instantiates a new big number.
     *
     * @param str
     * the str
     */
    constructor(str: String) {

        this.bigInteger = BigInteger(str, 16)
    }

    /**
     * Instantiates a new big number.
     *
     * @param array
     * the array
     */
    constructor(array: ByteArray) {
        var array = array

        // Add the first byte indicates the sign of the BigInteger
        if (array[0] < 0) {
            val tmp = ByteArray(array.size + 1)
            System.arraycopy(array, 0, tmp, 1, array.size)
            array = tmp
        }
        this.bigInteger = BigInteger(array)
    }

    /**
     * Adds the.
     *
     * @param val
     * the val
     * @return the big number
     */
    fun add(`val`: BigNumber): BigNumber {

        return BigNumber(this.bigInteger!!.add(`val`.getBigInteger()))
    }

    /**
     * Multiply.
     *
     * @param val
     * the val
     * @return the big number
     */
    fun multiply(`val`: BigNumber): BigNumber {

        return BigNumber(this.bigInteger!!.multiply(`val`.getBigInteger()))
    }

    /**
     * Sets the hex str.
     *
     * @param str
     * the new hex str
     */
    fun setHexStr(str: String) {

        this.bigInteger = BigInteger(str, 16)
    }

    /**
     * Sets the rand.
     *
     * @param numBytes
     * the new rand
     */
    fun setRand(numBytes: Int) {

        val random = SecureRandom()
        val array = random.generateSeed(numBytes)
        this.bigInteger = BigInteger(1, array)
    }

    /**
     * Sets the binary.
     *
     * @param array
     * the new binary
     */
    fun setBinary(array: ByteArray) {
        var array = array

        // Reverse array
        val length = array.size
        for (i in 0 until length / 2) {
            val j = array[i]
            array[i] = array[length - 1 - i]
            array[length - 1 - i] = j
        }

        // Add the first byte indicates the sign of the BigInteger
        if (array[0] < 0) {
            val tmp = ByteArray(array.size + 1)
            System.arraycopy(array, 0, tmp, 1, array.size)
            array = tmp
        }

        this.bigInteger = BigInteger(array)
    }

    /**
     * Mod.
     *
     * @param m
     * the m
     * @return the big number
     */
    operator fun mod(m: BigNumber): BigNumber {

        return BigNumber(this.bigInteger!!.mod(m.getBigInteger()))
    }

    fun isZero() : Boolean {
        return this.bigInteger!!.compareTo(BigInteger.ZERO) == 0
    }


    /**
     * Mod pow.
     *
     * @param exponent
     * the exponent
     * @param m
     * the m
     * @return the big number
     */
    fun modPow(exponent: BigNumber, m: BigNumber): BigNumber {

        return BigNumber(this.bigInteger!!.modPow(exponent.getBigInteger(), m.getBigInteger()))
    }

    /**
     * As byte array.
     *
     * @param minSize
     * the min size
     * @return the byte[]
     */
    fun asByteArray(minSize: Int): ByteArray {

        // Remove the first byte that indicates the sign of a BigInteger
        var array = this.bigInteger!!.toByteArray()
        if (array[0].toInt() == 0) {
            val tmp = ByteArray(array.size - 1)
            System.arraycopy(array, 1, tmp, 0, tmp.size)
            array = tmp
        }

        // Reverse array
        val length = array.size
        for (i in 0 until length / 2) {
            val j = array[i]
            array[i] = array[length - 1 - i]
            array[length - 1 - i] = j
        }

        // If we need more bytes than length of BigNumber set the rest to 0
        if (minSize > length) {
            val newArray = ByteArray(minSize)
            System.arraycopy(array, 0, newArray, 0, length)

            return newArray
        }

        return array
    }

    /**
     * As hex str.
     *
     * @return the string
     */
    fun asHexStr(): String {

        return this.bigInteger!!.toString(16).toUpperCase()
    }

    /**
     * Gets the big integer.
     *
     * @return the big integer
     */
    fun getBigInteger(): BigInteger {

        return this.bigInteger!!.abs()
    }
}
