/*
 *  Copyright (c) 2020 Thomas Neidhart.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.github.netomi.bat.dexfile.io

import com.google.common.hash.Hasher
import java.io.Closeable
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

class DexDataInput(inputStream: InputStream) : Closeable {
    private val byteBuffer: ByteBuffer

    init {
        byteBuffer = toByteBuffer(inputStream)
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
    }

    fun order(byteOrder: ByteOrder) {
        byteBuffer.order(byteOrder)
    }

    var offset: Int
        get() = byteBuffer.position()
        set(offset) {
            byteBuffer.position(offset)
        }

    fun skipBytes(byteToSkip: Int) {
        byteBuffer.position(byteBuffer.position() + byteToSkip)
    }

    fun skipAlignmentPadding(alignment: Int) {
        if (alignment > 1) {
            val currentAlignment = offset % alignment
            val padding = (alignment - currentAlignment) % alignment
            skipBytes(padding)
        }
    }

    fun readFully(bytes: ByteArray) {
        byteBuffer.get(bytes)
    }

    fun readByte(): Byte {
        return byteBuffer.get()
    }

    fun readUnsignedByte(): Int {
        return (byteBuffer.get().toInt() and 0xFF)
    }

    fun readShort(): Short {
        return byteBuffer.short
    }

    fun readShort(bytes: Int): Short {
        require(bytes in 1 .. 2)
        return readInt(bytes).toShort()
    }

    fun readUnsignedShort(): Int {
        return byteBuffer.short.toInt() and 0xFFFF
    }

    fun readChar(bytes: Int): Char {
        require(bytes in 1 .. 2)
        return readUnsignedInt(bytes).toChar()
    }

    fun readInt(): Int {
        return byteBuffer.int
    }

    fun readInt(bytes: Int): Int {
        require(bytes in 1 .. 4)

        var result = 0
        for (i in 0 until bytes) {
            val b = readUnsignedByte()
            result = result or (b shl (8*i))
        }

        // sign-extend
        val shift = 32 - 8 * bytes
        return if (shift == 0) result else result shl shift shr shift
    }

    fun readUnsignedInt(): Long {
        return byteBuffer.int.toLong() and 0xFFFFFFFFL
    }

    fun readUnsignedInt(bytes: Int): Int {
        require(bytes in 1 .. 4)

        var result = 0
        for (i in 0 until bytes) {
            val b = readUnsignedByte()
            result = result or (b shl (8*i))
        }

        return result
    }

    fun readLong(bytes: Int): Long {
        require(bytes in 1 .. 8)

        var result: Long = 0
        for (i in 0 until bytes) {
            val b = readUnsignedByte()
            result = result or (b.toLong() shl (8*i))
        }

        // sign-extend
        val shift = 64 - 8 * bytes
        return if (shift == 0) result else result shl shift shr shift
    }

    fun readFloat(bytes: Int): Float {
        require(bytes in 1 .. 4)

        var value = 0
        for (i in 0 until bytes) {
            val b = readUnsignedByte()
            value = value ushr 8 or (b shl 24)
        }

        return java.lang.Float.intBitsToFloat(value)
    }

    fun readDouble(bytes: Int): Double {
        require(bytes in 1 .. 8)

        var value: Long = 0
        for (i in 0 until bytes) {
            val b = readUnsignedByte().toLong()
            value = value ushr 8 or (b shl 56)
        }
        return java.lang.Double.longBitsToDouble(value)
    }

    fun readUleb128(): Int {
        var b: Int
        var value  = 0
        var length = 0

        do {
            b = readUnsignedByte()
            value = value or ((b and 0x7f) shl (length * 7))
            length++
        } while (b and 0x80 != 0)

        return value
    }

    fun readUleb128p1(): Int {
        return readUleb128() - 1
    }

    fun readSleb128(): Int {
        var b: Byte
        var value = 0
        var shift = 0

        do {
            b = byteBuffer.get()
            value = value or ((b.toInt() and 0x7f) shl shift)
            shift += 7
        } while ((b.toInt() and 0x7f.inv()) == 0x7f.inv())

        val mask = 1 shl (shift - 1)
        return (value xor mask) - mask
    }

    fun readMUTF8Bytes(len: Int): ByteArray {
        val buf = ByteArray(len * 3 + 1)
        var readBytes = 0
        while (readBytes < buf.size) {
            val b = readByte()
            buf[readBytes++] = b
            if (b.toInt() == 0x00) {
                break
            }
        }
        return buf.copyOf(readBytes)
    }

    fun update(hasher: Hasher) {
        @Suppress("UnstableApiUsage")
        hasher.putBytes(byteBuffer)
    }

    @Throws(IOException::class)
    override fun close() {}

    companion object {
        // Private utility methods.
        @Throws(IOException::class)
        private fun toByteBuffer(inputStream: InputStream): ByteBuffer {
            return ByteBuffer.wrap(inputStream.readBytes())
        }
    }
}