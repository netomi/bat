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

import com.github.netomi.bat.dexfile.DexConstants.NO_INDEX
import com.github.netomi.bat.dexfile.io.DexDataInput
import com.github.netomi.bat.dexfile.io.DexDataOutput
import com.github.netomi.bat.util.Preconditions
import dev.ahmedmourad.nocopy.annotations.NoCopy

/**
 * A class representing a type id item inside a dex file.
 *
 * @see [type id item @ dex format](https://source.android.com/devices/tech/dalvik/dex-format.type-id-item)
 */
@DataItemAnn(
    type          = DexConstants.TYPE_TYPE_ID_ITEM,
    dataAlignment = 4,
    dataSection   = false)
@NoCopy
data class TypeID private constructor(private var descriptorIndex_: Int = NO_INDEX) : DataItem() {

    val descriptorIndex: Int
        get() = descriptorIndex_

    fun getType(dexFile: DexFile): String {
        return dexFile.getStringID(descriptorIndex_).stringValue
    }

    override fun read(input: DexDataInput) {
        input.skipAlignmentPadding(dataAlignment)
        descriptorIndex_ = input.readInt()
    }

    override fun write(output: DexDataOutput) {
        output.writeAlignmentPadding(dataAlignment)
        output.writeInt(descriptorIndex_)
    }

    override fun toString(): String {
        return "TypeID[descriptorIdx=${descriptorIndex_}]"
    }

    companion object {
        @JvmStatic
        fun of(descriptorIndex: Int): TypeID {
            Preconditions.checkArgument(descriptorIndex >= 0, "descriptor index must not be negative")
            return TypeID(descriptorIndex)
        }

        @JvmStatic
        fun readContent(input: DexDataInput): TypeID {
            val typeID = TypeID()
            typeID.read(input)
            return typeID
        }
    }
}