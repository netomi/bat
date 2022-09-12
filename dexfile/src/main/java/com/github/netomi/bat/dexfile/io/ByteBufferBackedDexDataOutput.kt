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

import java.io.IOException
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.Channels

class ByteBufferBackedDexDataOutput(bytes: Int) : DexDataOutput {
    internal val byteBuffer: ByteBuffer

    init {
        byteBuffer = ByteBuffer.allocate(bytes)
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
    }

    override var order: ByteOrder
        get() = byteBuffer.order()
        set(value) {
            byteBuffer.order(value)
        }

    override var offset: Int
        get() = byteBuffer.position()
        set(offset) {
            byteBuffer.position(offset)
        }

    override fun writePadding(bytes: Int) {
        for (i in 0 until bytes) {
            writeByte(0)
        }
    }

    override fun writeAlignmentPadding(alignment: Int) {
        val currentAlignment = offset % alignment
        val padding = (alignment - currentAlignment) % alignment
        for (i in 0 until padding) {
            writeByte(0)
        }
    }

    override fun writeBytes(bytes: ByteArray) {
        byteBuffer.put(bytes)
    }

    override fun writeByte(b: Byte) {
        byteBuffer.put(b)
    }

    override fun writeUnsignedByte(b: Short) {
        writeByte(b.toByte())
    }

    override fun writeShort(value: Short) {
        byteBuffer.putShort(value)
    }

    override fun writeShort(value: Short, bytes: Int) {
        require(bytes in 1 .. 2)
        writeInt(value.toInt(), bytes)
    }

    override fun writeUnsignedShort(value: Int) {
        writeShort(value.toShort())
    }

    override fun writeChar(value: Char, bytes: Int) {
        require(bytes in 1 .. 2)
        writeInt(value.code, bytes)
    }

    override fun writeInt(value: Int) {
        byteBuffer.putInt(value)
    }

    override fun writeInt(value: Int, bytes: Int) {
        require(bytes in 1 .. 4)
        var tmpValue = value
        for (i in 0 until bytes) {
            writeByte(tmpValue.toByte())
            tmpValue = tmpValue ushr 8
        }
    }

    override fun writeUnsignedInt(value: Long) {
        writeInt(value.toInt())
    }

    override fun writeLong(value: Long, bytes: Int) {
        require(bytes in 1 .. 8)
        var tmpValue = value
        for (i in 0 until bytes) {
            writeByte(tmpValue.toByte())
            tmpValue = tmpValue ushr 8
        }
    }

    override fun writeFloat(value: Float, bytes: Int) {
        require(bytes in 1 .. 4)
        var bits = java.lang.Float.floatToIntBits(value)
        bits = bits ushr (32 - (8 * bytes))
        for (i in 0 until bytes) {
            writeByte(bits.toByte())
            bits = bits ushr 8
        }
    }

    override fun writeDouble(value: Double, bytes: Int) {
        require(bytes in 1 .. 8)
        var bits = java.lang.Double.doubleToLongBits(value)
        bits = bits ushr (64 - (8 * bytes))
        for (i in 0 until bytes) {
            writeByte(bits.toByte())
            bits = bits ushr 8
        }
    }

    override fun writeUleb128(value: Int) {
        var count    = 0
        var tmpValue = value
        do {
            check(++count <= 5)
            var b    = tmpValue and 0x7f
            tmpValue = tmpValue ushr 7
            if (tmpValue != 0) {
                b = b or 0x80
            }
            writeByte(b.toByte())
        } while (tmpValue != 0)
    }

    override fun writeUleb128p1(value: Int) {
        writeUleb128(value + 1)
    }

    override fun writeSleb128(value: Int) {
        var count    = 0
        var tmpValue = value
        while (true) {
            check(++count <= 5)
            val b    = tmpValue and 0x7f
            tmpValue = tmpValue shr 7
            if (tmpValue ==  0 && (b and 0x40) == 0 ||
                tmpValue == -1 && (b and 0x40) != 0) {
                writeByte(b.toByte())
                break
            }
            writeByte((b or 0x80).toByte())
        }
    }

    @Throws(IOException::class)
    fun copy(output: OutputStream) {
        byteBuffer.position(0)
        val channel = Channels.newChannel(output)
        channel.write(byteBuffer)
    }

    fun toArray(): ByteArray {
        val size  = byteBuffer.position()
        val array = ByteArray(size)
        byteBuffer.position(0)
        byteBuffer.get(array)
        return array
    }
}