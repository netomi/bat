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

interface DexDataOutput {
    fun order(): ByteOrder
    fun order(byteOrder: ByteOrder)
    var offset: Int
    fun writePadding(bytes: Int)
    fun writeAlignmentPadding(alignment: Int)
    fun writeBytes(bytes: ByteArray)
    fun writeByte(b: Byte)
    fun writeUnsignedByte(b: Short)
    fun writeShort(value: Short)
    fun writeShort(value: Short, bytes: Int)
    fun writeUnsignedShort(value: Int)
    fun writeChar(value: Char, bytes: Int)
    fun writeInt(value: Int)
    fun writeInt(value: Int, bytes: Int)
    fun writeUnsignedInt(value: Long)
    fun writeLong(value: Long, bytes: Int)
    fun writeFloat(value: Float, bytes: Int)
    fun writeDouble(value: Double, bytes: Int)
    fun writeUleb128(value: Int)
    fun writeUleb128p1(value: Int)
    fun writeSleb128(value: Int)
}