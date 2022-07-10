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
import com.google.common.base.Preconditions
import dev.ahmedmourad.nocopy.annotations.NoCopy

/**
 * A class representing a field id item inside a dex file.
 *
 * @see [field id item @ dex format](https://source.android.com/devices/tech/dalvik/dex-format.field-id-item)
 */
@DataItemAnn(
    type          = DexConstants.TYPE_FIELD_ID_ITEM,
    dataAlignment = 4,
    dataSection   = false)
@NoCopy
data class FieldID private constructor(
    private var classIndex_: Int = NO_INDEX, // ushort
    private var nameIndex_:  Int = NO_INDEX, // uint
    private var typeIndex_:  Int = NO_INDEX  // ushort
) : DataItem() {

    val classIndex: Int
        get() = classIndex_

    val nameIndex: Int
        get() = nameIndex_

    val typeIndex: Int
        get() = typeIndex_

    fun getClassTypeID(dexFile: DexFile): TypeID {
        return dexFile.getTypeID(classIndex_)
    }

    fun getClassType(dexFile: DexFile): String {
        return getClassTypeID(dexFile).getType(dexFile)
    }

    fun getType(dexFile: DexFile): String {
        return dexFile.getTypeID(typeIndex_).getType(dexFile)
    }

    fun getName(dexFile: DexFile): String {
        return dexFile.getStringID(nameIndex_).stringValue
    }

    override fun read(input: DexDataInput) {
        input.skipAlignmentPadding(dataAlignment)
        classIndex_ = input.readUnsignedShort()
        typeIndex_ = input.readUnsignedShort()
        nameIndex_ = input.readInt()
    }

    override fun write(output: DexDataOutput) {
        output.writeAlignmentPadding(dataAlignment)
        output.writeUnsignedShort(classIndex_)
        output.writeUnsignedShort(typeIndex_)
        output.writeInt(nameIndex_)
    }

    override fun toString(): String {
        return "FieldID[classIdx=${classIndex_},nameIdx=${nameIndex_},typeIdx=${typeIndex_}]"
    }

    companion object {
        @JvmStatic
        fun of(classIndex: Int, nameIndex: Int, typeIndex: Int): FieldID {
            Preconditions.checkArgument(classIndex >= 0, "class index must not be negative")
            Preconditions.checkArgument(nameIndex >= 0, "name index must not be negative")
            Preconditions.checkArgument(typeIndex >= 0, "type index must not be negative")
            return FieldID(classIndex, nameIndex, typeIndex)
        }

        @JvmStatic
        fun readContent(input: DexDataInput): FieldID {
            val fieldID = FieldID()
            fieldID.read(input)
            return fieldID
        }
    }
}