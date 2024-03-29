/*
 *  Copyright (c) 2020-2022 Thomas Neidhart.
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

import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.test.Test
import kotlin.test.assertEquals

class DexDataInputTest {

    @Test
    fun testSkipBytes() {
        val data = ByteArray(100) { index -> index.toByte() }
        val input = DexDataInput(data.inputStream())

        assertEquals(0, input.offset)
        input.skipBytes(10)
        assertEquals(10, input.offset)
    }

    @Test
    fun testReadByte() {
        val data = byteArrayOf(0x01, 0x02, Byte.MIN_VALUE, Byte.MAX_VALUE, -0x01)
        val input = DexDataInput(data.inputStream())

        assertEquals(0x01, input.readByte())
        assertEquals(0x02, input.readByte())
        assertEquals(Byte.MIN_VALUE, input.readByte())
        assertEquals(Byte.MAX_VALUE, input.readByte())
        assertEquals(-0x01, input.readByte())
    }

    @Test
    fun testReadUnsignedByte() {
        val data = byteArrayOf(0x01, 0x02, -0x01)
        val input = DexDataInput(data.inputStream())

        assertEquals(0x01, input.readUnsignedByte())
        assertEquals(0x02, input.readUnsignedByte())
        assertEquals(0xff, input.readUnsignedByte())
    }

    @Test
    fun testReadShort() {
        val bArr = ByteArray(100)
        val data = ByteBuffer.wrap(bArr)
        data.order(ByteOrder.LITTLE_ENDIAN)

        data.putShort(0)
        data.putShort(1)
        data.putShort(-1)
        data.putShort(Short.MIN_VALUE)
        data.putShort(Short.MAX_VALUE)

        val input = DexDataInput(bArr.inputStream())

        assertEquals(0x00, input.readShort())
        assertEquals(0x01, input.readShort())
        assertEquals(-0x01, input.readShort())
        assertEquals(Short.MIN_VALUE, input.readShort())
        assertEquals(Short.MAX_VALUE, input.readShort())
    }

    @Test
    fun testReadShortWithBytes() {
        val bArr = ByteArray(100)
        val data = ByteBuffer.wrap(bArr)
        data.order(ByteOrder.LITTLE_ENDIAN)

        data.put(0)
        data.put(1)
        data.put((-1).toByte())
        data.putShort(1)
        data.putShort(-1)
        data.putShort(Short.MIN_VALUE)
        data.putShort(Short.MAX_VALUE)

        val input = DexDataInput(bArr.inputStream())

        assertEquals( 0, input.readShort(1))
        assertEquals( 1, input.readShort(1))
        assertEquals(-1, input.readShort(1))
        assertEquals( 1, input.readShort(2))
        assertEquals(-1, input.readShort(2))
        assertEquals(Short.MIN_VALUE, input.readShort(2))
        assertEquals(Short.MAX_VALUE, input.readShort(2))
        assertEquals(11, input.offset)
    }

    @Test
    fun testReadUnsignedShort() {
        val bArr = ByteArray(100)
        val data = ByteBuffer.wrap(bArr)
        data.order(ByteOrder.LITTLE_ENDIAN)

        data.putShort(0)
        data.putShort(1)
        data.putShort(UShort.MIN_VALUE.toShort())
        data.putShort(UShort.MAX_VALUE.toShort())

        val input = DexDataInput(bArr.inputStream())

        assertEquals(0x00, input.readUnsignedShort())
        assertEquals(0x01, input.readUnsignedShort())
        assertEquals(UShort.MIN_VALUE.toInt(), input.readUnsignedShort())
        assertEquals(UShort.MAX_VALUE.toInt(), input.readUnsignedShort())
    }

    @Test
    fun testReadInt() {
        val bArr = ByteArray(100)
        val data = ByteBuffer.wrap(bArr)
        data.order(ByteOrder.LITTLE_ENDIAN)

        data.putInt(0)
        data.putInt(1)
        data.putInt(-1)
        data.putInt(Int.MIN_VALUE)
        data.putInt(Int.MAX_VALUE)

        val input = DexDataInput(bArr.inputStream())

        assertEquals(0x00, input.readInt())
        assertEquals(0x01, input.readInt())
        assertEquals(-0x01, input.readInt())
        assertEquals(Int.MIN_VALUE, input.readInt())
        assertEquals(Int.MAX_VALUE, input.readInt())
    }

    @Test
    fun testReadIntWithBytes() {
        val bArr = ByteArray(100)
        val data = ByteBuffer.wrap(bArr)
        data.order(ByteOrder.LITTLE_ENDIAN)

        data.put(0)
        data.put((-1).toByte())
        data.putInt(0)
        data.putInt(1)
        data.putInt(-1)
        data.putInt(Int.MIN_VALUE)
        data.putInt(Int.MAX_VALUE)

        val input = DexDataInput(bArr.inputStream())

        assertEquals( 0, input.readInt(1))
        assertEquals(-1, input.readInt(1))
        assertEquals( 0, input.readInt(4))
        assertEquals( 1, input.readInt(4))
        assertEquals(-1, input.readInt(4))
        assertEquals(Int.MIN_VALUE, input.readInt(4))
        assertEquals(Int.MAX_VALUE, input.readInt(4))
        assertEquals(22, input.offset)
    }

    @Test
    fun testReadUnsignedInt() {
        val bArr = ByteArray(100)
        val data = ByteBuffer.wrap(bArr)
        data.order(ByteOrder.LITTLE_ENDIAN)

        data.putInt(0)
        data.putInt(1)
        data.putInt(UInt.MIN_VALUE.toInt())
        data.putInt(UInt.MAX_VALUE.toInt())

        val input = DexDataInput(bArr.inputStream())

        assertEquals(0x00L, input.readUnsignedInt())
        assertEquals(0x01L, input.readUnsignedInt())
        assertEquals(UInt.MIN_VALUE.toLong(), input.readUnsignedInt())
        assertEquals(UInt.MAX_VALUE.toLong(), input.readUnsignedInt())
    }

    @Test
    fun testSleb128() {
        val bArr = ByteArray(100)
        val data = ByteBuffer.wrap(bArr)
        data.order(ByteOrder.LITTLE_ENDIAN)

        // 0
        data.put(0x00)

        // 1
        data.put(0x01)

        // -1
        data.put(0x7f)

        // -128
        data.put(0x80.toByte())
        data.put(0x7f)

        val input = DexDataInput(bArr.inputStream())
        assertEquals(   0, input.readSleb128())
        assertEquals(   1, input.readSleb128())
        assertEquals(  -1, input.readSleb128())
        assertEquals(-128, input.readSleb128())
        assertEquals(5, input.offset)
    }

    @Test
    fun testUleb128() {
        val bArr = ByteArray(100)
        val data = ByteBuffer.wrap(bArr)
        data.order(ByteOrder.LITTLE_ENDIAN)

        // 0
        data.put(0x00)

        // 1
        data.put(0x01)

        // -1
        data.put(0x7f)

        // -128
        data.put(0x80.toByte())
        data.put(0x7f)

        val input = DexDataInput(bArr.inputStream())
        assertEquals(    0, input.readUleb128())
        assertEquals(    1, input.readUleb128())
        assertEquals(  127, input.readUleb128())
        assertEquals(16256, input.readUleb128())
        assertEquals(5, input.offset)
    }

    @Test
    fun testUleb128p1() {
        val bArr = ByteArray(100)
        val data = ByteBuffer.wrap(bArr)
        data.order(ByteOrder.LITTLE_ENDIAN)

        // 0
        data.put(0x00)

        // 1
        data.put(0x01)

        // -1
        data.put(0x7f)

        // -128
        data.put(0x80.toByte())
        data.put(0x7f)

        val input = DexDataInput(bArr.inputStream())
        assertEquals(   -1, input.readUleb128p1())
        assertEquals(    0, input.readUleb128p1())
        assertEquals(  126, input.readUleb128p1())
        assertEquals(16255, input.readUleb128p1())
        assertEquals(5, input.offset)
    }
}