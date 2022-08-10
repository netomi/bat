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
 * @see [proto id item @ dex format](https://source.android.com/devices/tech/dalvik/dex-format.proto-id-item)
 */
@DataItemAnn(
    type          = TYPE_PROTO_ID_ITEM,
    dataAlignment = 4,
    dataSection   = false)
class ProtoID private constructor(            shortyIndex:     Int      = NO_INDEX,
                                              returnTypeIndex: Int      = NO_INDEX,
                                  private var _parameters:     TypeList = TypeList.empty()) : DataItem() {

    var shortyIndex: Int = shortyIndex
        internal set

    var returnTypeIndex: Int = returnTypeIndex
        internal set

    val parameters: TypeList
        get() = _parameters

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
            _parameters  = TypeList.readContent(input)
        }
    }

    override fun updateOffsets(dataItemMap: Map) {
        parametersOffset = if (!_parameters.isEmpty) dataItemMap.getOffset(_parameters) else 0
    }

    override fun write(output: DexDataOutput) {
        output.writeInt(shortyIndex)
        output.writeInt(returnTypeIndex)
        output.writeInt(parametersOffset)
    }

    fun parameterTypesAccept(dexFile: DexFile, visitor: TypeVisitor) {
        for (i in 0 until _parameters.typeCount) {
            visitor.visitType(dexFile, _parameters, i, _parameters.getTypeIndex(i), _parameters.getType(dexFile, i))
        }
    }

    override fun dataItemsAccept(dexFile: DexFile, visitor: DataItemVisitor) {
        visitor.visitParameterTypes(dexFile, this, _parameters)
    }

    internal fun referencedIDsAccept(dexFile: DexFile, visitor: ReferencedIDVisitor) {
        visitor.visitStringID(dexFile, PropertyAccessor({ shortyIndex }, { shortyIndex = it }))
        visitor.visitTypeID(dexFile, PropertyAccessor({ returnTypeIndex }, { returnTypeIndex = it }))
        _parameters.referencedIDsAccept(dexFile, visitor)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ProtoID

        return shortyIndex     == other.shortyIndex     &&
               returnTypeIndex == other.returnTypeIndex &&
               _parameters     == other._parameters
    }

    override fun hashCode(): Int {
        return Objects.hash(shortyIndex, returnTypeIndex, _parameters)
    }

    override fun toString(): String {
        return "ProtoID[shortyIdx=${shortyIndex},returnTypeIdx=${returnTypeIndex},parameters=${_parameters}]"
    }

    companion object {
        fun of(shortyIndex: Int, returnTypeIndex: Int, vararg parameterTypeIndices: Int): ProtoID {
            require(shortyIndex >= 0) { "shorty index must not be negative" }
            require(returnTypeIndex >= 0) { "return type index must not be negative" }

            val protoID = ProtoID(shortyIndex, returnTypeIndex)
            for (index in parameterTypeIndices) {
                require(index >= 0) { "parameter type index must not be negative" }
                protoID.parameters.addType(index)
            }
            return protoID
        }

        fun readContent(input: DexDataInput): ProtoID {
            val protoID = ProtoID()
            protoID.read(input)
            return protoID
        }
    }
}