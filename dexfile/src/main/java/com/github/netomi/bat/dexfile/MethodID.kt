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
import com.github.netomi.bat.dexfile.visitor.*
import com.google.common.base.Preconditions
import java.util.*

/**
 * A class representing a method id item inside a dex file.
 *
 * @see [method id item @ dex format](https://source.android.com/devices/tech/dalvik/dex-format.method-id-item)
 */
@DataItemAnn(
    type          = TYPE_METHOD_ID_ITEM,
    dataAlignment = 4,
    dataSection   = false)
class MethodID private constructor(_classIndex: Int = NO_INDEX, _nameIndex:  Int = NO_INDEX, _protoIndex: Int = NO_INDEX) : DataItem() {

    var classIndex: Int = _classIndex
        internal set

    var nameIndex: Int = _nameIndex
        internal set

    var protoIndex: Int = _protoIndex
        internal set

    fun getClassTypeID(dexFile: DexFile): TypeID {
        return dexFile.getTypeID(classIndex)
    }

    fun getClassType(dexFile: DexFile): String {
        return getClassTypeID(dexFile).getType(dexFile)
    }

    fun getProtoID(dexFile: DexFile): ProtoID {
        return dexFile.getProtoID(protoIndex)
    }

    fun getName(dexFile: DexFile): String {
        return dexFile.getStringID(nameIndex).stringValue
    }

    fun getParameterTypes(dexFile: DexFile): List<String> {
        return getProtoID(dexFile).getParameterTypes(dexFile)
    }

    fun getReturnType(dexFile: DexFile): String {
        return getProtoID(dexFile).getReturnType(dexFile)
    }

    fun getShortyType(dexFile: DexFile): String {
        return getProtoID(dexFile).getShorty(dexFile)
    }

    override val isEmpty: Boolean
        get() = classIndex == NO_INDEX

    override fun read(input: DexDataInput) {
        classIndex = input.readUnsignedShort()
        protoIndex = input.readUnsignedShort()
        nameIndex  = input.readInt()
    }

    override fun write(output: DexDataOutput) {
        output.writeUnsignedShort(classIndex)
        output.writeUnsignedShort(protoIndex)
        output.writeInt(nameIndex)
    }

    fun accept(dexFile: DexFile, visitor: EncodedMethodVisitor) {
        val classType = getClassType(dexFile)
        val classDef  = dexFile.getClassDefByType(classType)

        classDef?.methodsAccept(dexFile, filterMethodsByNameAndProtoID(getName(dexFile), getProtoID(dexFile), visitor))
    }

    internal fun referencedIDsAccept(dexFile: DexFile, visitor: ReferencedIDVisitor) {
        visitor.visitTypeID(dexFile, PropertyAccessor({ classIndex }, { classIndex = it }))
        visitor.visitStringID(dexFile, PropertyAccessor({ nameIndex }, { nameIndex = it }))
        visitor.visitProtoID(dexFile, PropertyAccessor({ protoIndex }, { protoIndex = it }))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MethodID

        return classIndex  == other.classIndex &&
               nameIndex   == other.nameIndex  &&
               protoIndex  == other.protoIndex
    }

    override fun hashCode(): Int {
        return Objects.hash(classIndex, nameIndex, protoIndex)
    }

    override fun toString(): String {
        return "MethodID[classIdx=${classIndex},nameIdx=${nameIndex},protoIdx=${protoIndex}]"
    }

    companion object {
        fun of(classIndex: Int, nameIndex: Int, protoIndex: Int): MethodID {
            Preconditions.checkArgument(classIndex >= 0, "class index must not be negative")
            Preconditions.checkArgument(nameIndex >= 0, "name index must not be negative")
            Preconditions.checkArgument(protoIndex >= 0, "proto index must not be negative")
            return MethodID(classIndex, nameIndex, protoIndex)
        }

        fun readContent(input: DexDataInput): MethodID {
            val methodID = MethodID()
            methodID.read(input)
            return methodID
        }
    }
}