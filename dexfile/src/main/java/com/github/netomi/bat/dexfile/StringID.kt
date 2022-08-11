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
import com.github.netomi.bat.dexfile.visitor.DataItemVisitor
import java.util.*

/**
 * A class representing a string id item inside a dex file.
 *
 * @see [string id item @ dex format](https://source.android.com/devices/tech/dalvik/dex-format.string-item)
 */
@DataItemAnn(
    type          = TYPE_STRING_ID_ITEM,
    dataAlignment = 4,
    dataSection   = false)
class StringID private constructor(): DataItem(), Comparable<StringID> {

    lateinit var stringData: StringData
        private set

    var stringDataOffset = 0
        private set

    val stringValue: String
        get() = stringData.string

    private constructor(stringData: StringData): this() {
        this.stringData = stringData
    }

    override val isEmpty: Boolean
        get() = false

    override fun read(input: DexDataInput) {
        stringDataOffset = input.readInt()
    }

    override fun readLinkedDataItems(input: DexDataInput) {
        input.offset = stringDataOffset
        stringData = StringData.read(input)
    }

    override fun updateOffsets(dataItemMap: Map) {
        stringDataOffset = dataItemMap.getOffset(stringData)
    }

    override fun write(output: DexDataOutput) {
        output.writeInt(stringDataOffset)
    }

    override fun dataItemsAccept(dexFile: DexFile, visitor: DataItemVisitor) {
        visitor.visitStringData(dexFile, this, stringData)
    }

    override fun compareTo(other: StringID): Int {
        return stringData.compareTo(other.stringData)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val o = other as StringID

        return stringData == o.stringData
    }

    override fun hashCode(): Int {
        return Objects.hash(stringData)
    }

    override fun toString(): String {
        return "StringID[data=${stringData}]"
    }

    companion object {
        fun of(value: String): StringID {
            return StringID(StringData.of(value))
        }

        internal fun read(input: DexDataInput): StringID {
            val stringID = StringID()
            stringID.read(input)
            return stringID
        }
    }
}