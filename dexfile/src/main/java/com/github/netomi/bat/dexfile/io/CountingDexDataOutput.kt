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

import java.nio.ByteOrder

class CountingDexDataOutput : DexDataOutput {
    override var offset = 0

    override fun order(): ByteOrder {
        return ByteOrder.LITTLE_ENDIAN
    }

    override fun order(byteOrder: ByteOrder) {}

    override fun writePadding(bytes: Int) {
        offset += bytes
    }

    override fun writeAlignmentPadding(alignment: Int) {
        val currentAlignment = offset % alignment
        val padding = (alignment - currentAlignment) % alignment
        offset += padding
    }

    override fun writeBytes(bytes: ByteArray) {
        offset += bytes.size
    }

    override fun writeByte(b: Byte) {
        offset++
    }

    override fun writeUnsignedByte(b: Short) {
        offset++
    }

    override fun writeShort(value: Short) {
        offset += 2
    }

    override fun writeShort(value: Short, bytes: Int) {
        offset += bytes
    }

    override fun writeUnsignedShort(value: Int) {
        offset += 2
    }

    override fun writeChar(value: Char, bytes: Int) {
        offset += bytes
    }

    override fun writeInt(value: Int) {
        offset += 4
    }

    override fun writeInt(value: Int, bytes: Int) {
        offset += bytes
    }

    override fun writeUnsignedInt(value: Long) {
        offset += 4
    }

    override fun writeLong(value: Long, bytes: Int) {
        offset += bytes
    }

    override fun writeFloat(value: Float, bytes: Int) {
        offset += bytes
    }

    override fun writeDouble(value: Double, bytes: Int) {
        offset += bytes
    }

    override fun writeUleb128(value: Int) {
        var currValue = value
        var bytesWritten = 0
        do {
            currValue = currValue shr 7
            bytesWritten++
        } while (currValue != 0)
        offset += bytesWritten
    }

    override fun writeUleb128p1(value: Int) {
        writeUleb128(value + 1)
    }

    override fun writeSleb128(value: Int) {
        var currValue = value
        var bytesWritten = 0
        while (true) {
            val b = (currValue and 0x7f).toByte()
            currValue = currValue shr 7
            if (currValue ==  0 && b.toInt() and 0x40 == 0 ||
                currValue == -1 && b.toInt() and 0x40 != 0
            ) {
                bytesWritten++
                break
            }
            bytesWritten++
        }
        offset += bytesWritten
    }
}