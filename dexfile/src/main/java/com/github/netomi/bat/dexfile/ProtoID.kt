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
import com.github.netomi.bat.dexfile.util.DexClasses
import com.github.netomi.bat.dexfile.visitor.DataItemVisitor
import com.github.netomi.bat.dexfile.visitor.PropertyAccessor
import com.github.netomi.bat.dexfile.visitor.ReferencedIDVisitor
import com.github.netomi.bat.dexfile.visitor.TypeVisitor
import com.google.common.base.Preconditions
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
class ProtoID private constructor(_shortyIndex: Int = NO_INDEX, _returnTypeIndex: Int = NO_INDEX, private var _parameters: TypeList = TypeList.empty()) : DataItem() {

    var shortyIndex: Int = _shortyIndex
        internal set

    var returnTypeIndex: Int = _returnTypeIndex
        internal set

    val parameters: TypeList
        get() = _parameters

    var parametersOffset = 0
        private set

    fun getShorty(dexFile: DexFile): String {
        return dexFile.getStringID(shortyIndex).stringValue
    }

    fun getDescriptor(dexFile: DexFile): String {
        val sb = StringBuilder()
        sb.append('(')
        for (type in parameters.getTypes(dexFile)) {
            sb.append(type)
        }
        sb.append(')')
        sb.append(getReturnType(dexFile))
        return sb.toString()
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
        input.skipAlignmentPadding(dataAlignment)
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
        parametersOffset = if (!_parameters.isEmpty) { dataItemMap.getOffset(_parameters) } else { 0 }
    }

    override fun write(output: DexDataOutput) {
        output.writeAlignmentPadding(dataAlignment)
        output.writeInt(shortyIndex)
        output.writeInt(returnTypeIndex)
        output.writeInt(parametersOffset)
    }

    fun parameterTypesAccept(dexFile: DexFile, visitor: TypeVisitor) {
        for (i in 0 until _parameters.typeCount) {
            visitor.visitType(dexFile, _parameters, i, _parameters.getType(dexFile, i))
        }
    }

    override fun dataItemsAccept(dexFile: DexFile, visitor: DataItemVisitor) {
        if (!_parameters.isEmpty) {
            visitor.visitParameterTypes(dexFile, this, _parameters)
        }
    }

    internal fun referencedIDsAccept(dexFile: DexFile, visitor: ReferencedIDVisitor) {
        visitor.visitStringID(dexFile, PropertyAccessor(this::shortyIndex))
        visitor.visitTypeID(dexFile, PropertyAccessor(this::returnTypeIndex))
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
            Preconditions.checkArgument(shortyIndex >= 0, "shorty index must not be negative")
            Preconditions.checkArgument(returnTypeIndex >= 0, "return type index must not be negative")

            val protoID = ProtoID(shortyIndex, returnTypeIndex)
            for (index in parameterTypeIndices) {
                Preconditions.checkArgument(index >= 0, "parameter type index must not be negative")
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