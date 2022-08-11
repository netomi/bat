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
package com.github.netomi.bat.dexfile

import com.github.netomi.bat.dexfile.io.DexDataInput
import com.github.netomi.bat.dexfile.io.DexDataOutput
import com.github.netomi.bat.dexfile.util.Mutf8.decode
import com.github.netomi.bat.dexfile.util.Mutf8.encode

/**
 * A class representing a string data item inside a dex file.
 *
 * @see [string data item @ dex format](https://source.android.com/devices/tech/dalvik/dex-format.string-data-item)
 */
@DataItemAnn(
    type          = TYPE_STRING_DATA_ITEM,
    dataAlignment = 1,
    dataSection   = true)
class StringData private constructor(): DataItem(), Comparable<StringData> {

    lateinit var string: String
        private set

    private constructor(string: String): this() {
        this.string = string
    }

    override val isEmpty: Boolean
        get() = false

    override fun read(input: DexDataInput) {
        val utf16Size = input.readUleb128()
        val data = input.readMUTF8Bytes(utf16Size)
        string = decode(data, utf16Size)
    }

    override fun write(output: DexDataOutput) {
        val utf16Size = string.length
        output.writeUleb128(utf16Size)
        val data = encode(string)
        output.writeBytes(data)
        output.writeByte(0x0.toByte())
    }

    override fun compareTo(other: StringData): Int {
        return string.compareTo(other.string)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val o = other as StringData

        return string == o.string
    }

    override fun hashCode(): Int {
        return string.hashCode()
    }

    override fun toString(): String {
        return "StringData[value=${string}]"
    }

    companion object {
        fun of(value: String): StringData {
            return StringData(value)
        }

        fun readContent(input: DexDataInput): StringData {
            val stringData = StringData()
            stringData.read(input)
            return stringData
        }
    }
}