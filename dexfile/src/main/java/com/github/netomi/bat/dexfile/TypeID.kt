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
import com.github.netomi.bat.dexfile.visitor.PropertyAccessor
import com.github.netomi.bat.dexfile.visitor.ReferencedIDVisitor
import com.google.common.base.Preconditions
import java.util.*

/**
 * A class representing a type id item inside a dex file.
 *
 * @see [type id item @ dex format](https://source.android.com/devices/tech/dalvik/dex-format.type-id-item)
 */
@DataItemAnn(
    type          = TYPE_TYPE_ID_ITEM,
    dataAlignment = 4,
    dataSection   = false)
class TypeID private constructor(_descriptorIndex: Int = NO_INDEX) : DataItem() {

    var descriptorIndex: Int = _descriptorIndex
        internal set

    fun getType(dexFile: DexFile): String {
        return dexFile.getStringID(descriptorIndex).stringValue
    }

    override val isEmpty: Boolean
        get() = descriptorIndex == NO_INDEX

    override fun read(input: DexDataInput) {
        descriptorIndex = input.readInt()
    }

    override fun write(output: DexDataOutput) {
        output.writeInt(descriptorIndex)
    }

    internal fun referencedIDsAccept(dexFile: DexFile, visitor: ReferencedIDVisitor) {
        visitor.visitStringID(dexFile, PropertyAccessor({ descriptorIndex }, { descriptorIndex = it }))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TypeID

        return descriptorIndex == other.descriptorIndex
    }

    override fun hashCode(): Int {
        return Objects.hash(descriptorIndex)
    }

    override fun toString(): String {
        return "TypeID[descriptorIdx=${descriptorIndex}]"
    }

    companion object {
        fun of(descriptorIndex: Int): TypeID {
            Preconditions.checkArgument(descriptorIndex >= 0, "descriptor index must not be negative")
            return TypeID(descriptorIndex)
        }

        fun readContent(input: DexDataInput): TypeID {
            val typeID = TypeID()
            typeID.read(input)
            return typeID
        }
    }
}