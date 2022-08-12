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
import com.github.netomi.bat.dexfile.visitor.PropertyAccessor
import com.github.netomi.bat.dexfile.visitor.ReferencedIDVisitor
import com.github.netomi.bat.dexfile.visitor.TypeVisitor
import java.util.*

/**
 * A class representing a proto id item inside a dex file.
 *
 * @see <a href="https://source.android.com/devices/tech/dalvik/dex-format#proto-id-item">proto id item @ dex format</a>
 */
@DataItemAnn(
    type          = TYPE_PROTO_ID_ITEM,
    dataAlignment = 4,
    dataSection   = false)
class ProtoID private constructor(shortyIndex:     Int      = NO_INDEX,
                                  returnTypeIndex: Int      = NO_INDEX,
                                  parameters:      TypeList = TypeList.empty()) : DataItem() {

    var shortyIndex: Int = shortyIndex
        private set

    var returnTypeIndex: Int = returnTypeIndex
        private set

    var parameters: TypeList = parameters
        private set

    var parametersOffset: Int = 0
        private set

    fun getShorty(dexFile: DexFile): String {
        return dexFile.getStringID(shortyIndex).stringValue
    }

    fun getDescriptor(dexFile: DexFile): String {
        return buildString {
            append('(')
            for (type in getParameterTypes(dexFile)) {
                append(type)
            }
            append(')')
            append(getReturnType(dexFile))
        }
    }

    fun getParameterTypes(dexFile: DexFile): List<String> {
        return parameters.getTypes(dexFile)
    }

    fun getReturnTypeTypeID(dexFile: DexFile): TypeID {
        return dexFile.getTypeID(returnTypeIndex)
    }

    fun getReturnType(dexFile: DexFile): String {
        return getReturnTypeTypeID(dexFile).getType(dexFile)
    }

    override val isEmpty: Boolean
        get() = shortyIndex == NO_INDEX

    override fun read(input: DexDataInput) {
        shortyIndex      = input.readInt()
        returnTypeIndex  = input.readInt()
        parametersOffset = input.readInt()
    }

    override fun readLinkedDataItems(input: DexDataInput) {
        if (parametersOffset != 0) {
            input.offset = parametersOffset
            parameters  = TypeList.read(input)
        }
    }

    override fun updateOffsets(dataItemMap: Map) {
        parametersOffset = if (!parameters.isEmpty) dataItemMap.getOffset(parameters) else 0
    }

    override fun write(output: DexDataOutput) {
        output.writeInt(shortyIndex)
        output.writeInt(returnTypeIndex)
        output.writeInt(parametersOffset)
    }

    fun parameterTypesAccept(dexFile: DexFile, visitor: TypeVisitor) {
        for (i in 0 until parameters.typeCount) {
            visitor.visitType(dexFile, parameters, i, parameters.getTypeIndex(i), parameters.getType(dexFile, i))
        }
    }

    override fun dataItemsAccept(dexFile: DexFile, visitor: DataItemVisitor) {
        visitor.visitParameterTypes(dexFile, this, parameters)
    }

    internal fun referencedIDsAccept(dexFile: DexFile, visitor: ReferencedIDVisitor) {
        visitor.visitStringID(dexFile, PropertyAccessor({ shortyIndex }, { shortyIndex = it }))
        visitor.visitTypeID(dexFile, PropertyAccessor({ returnTypeIndex }, { returnTypeIndex = it }))
        parameters.referencedIDsAccept(dexFile, visitor)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ProtoID

        return shortyIndex     == other.shortyIndex     &&
               returnTypeIndex == other.returnTypeIndex &&
               parameters      == other.parameters
    }

    override fun hashCode(): Int {
        return Objects.hash(shortyIndex, returnTypeIndex, parameters)
    }

    override fun toString(): String {
        return "ProtoID[shortyIdx=${shortyIndex},returnTypeIdx=${returnTypeIndex},parameters=${parameters}]"
    }

    companion object {
        fun of(shortyIndex: Int, returnTypeIndex: Int, vararg parameterTypeIndices: Int): ProtoID {
            require(shortyIndex >= 0) { "shorty index must not be negative" }
            require(returnTypeIndex >= 0) { "return type index must not be negative" }
            return ProtoID(shortyIndex, returnTypeIndex, TypeList.of(*parameterTypeIndices))
        }

        internal fun read(input: DexDataInput): ProtoID {
            val protoID = ProtoID()
            protoID.read(input)
            return protoID
        }
    }
}