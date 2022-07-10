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
import com.github.netomi.bat.dexfile.util.DexClasses
import com.github.netomi.bat.dexfile.visitor.AllEncodedMethodsVisitor
import com.github.netomi.bat.dexfile.visitor.EncodedMethodVisitor
import com.github.netomi.bat.dexfile.visitor.MethodNameAndProtoFilter
import com.google.common.base.Preconditions
import dev.ahmedmourad.nocopy.annotations.NoCopy

/**
 * A class representing a method id item inside a dex file.
 *
 * @see [method id item @ dex format](https://source.android.com/devices/tech/dalvik/dex-format.method-id-item)
 */
@DataItemAnn(
    type          = DexConstants.TYPE_METHOD_ID_ITEM,
    dataAlignment = 4,
    dataSection   = false)
@NoCopy
data class MethodID private constructor(
    private var classIndex_: Int = NO_INDEX,
    private var nameIndex_:  Int = NO_INDEX,
    private var protoIndex_: Int = NO_INDEX) : DataItem() {

    val classIndex: Int
        get() = classIndex_

    val nameIndex: Int
        get() = nameIndex_

    val protoIndex: Int
        get() = protoIndex_

    fun getClassTypeID(dexFile: DexFile): TypeID {
        return dexFile.getTypeID(classIndex_)
    }

    fun getClassType(dexFile: DexFile): String {
        return getClassTypeID(dexFile).getType(dexFile)
    }

    fun getProtoID(dexFile: DexFile): ProtoID {
        return dexFile.getProtoID(protoIndex_)
    }

    fun getName(dexFile: DexFile): String {
        return dexFile.getStringID(nameIndex_).stringValue
    }

    fun getShortyType(dexFile: DexFile): String {
        return dexFile.getProtoID(protoIndex_).getShorty(dexFile)
    }

    override fun read(input: DexDataInput) {
        input.skipAlignmentPadding(dataAlignment)
        classIndex_ = input.readUnsignedShort()
        protoIndex_ = input.readUnsignedShort()
        nameIndex_ = input.readInt()
    }

    override fun write(output: DexDataOutput) {
        output.writeAlignmentPadding(dataAlignment)
        output.writeUnsignedShort(classIndex_)
        output.writeUnsignedShort(protoIndex_)
        output.writeInt(nameIndex_)
    }

    fun accept(dexFile: DexFile, visitor: EncodedMethodVisitor) {
        val className = DexClasses.internalClassNameFromType(getClassType(dexFile))
        val classDef  = dexFile.getClassDef(className)

        classDef?.classDataAccept(dexFile,
            AllEncodedMethodsVisitor(
            MethodNameAndProtoFilter(getName(dexFile), getProtoID(dexFile), visitor)))
    }

    override fun toString(): String {
        return "MethodID[classIdx=${classIndex_},nameIdx=${nameIndex_},protoIdx=${protoIndex_}]"
    }

    companion object {
        @JvmStatic
        fun of(classIndex: Int, nameIndex: Int, protoIndex: Int): MethodID {
            Preconditions.checkArgument(classIndex >= 0, "class index must be non negative")
            Preconditions.checkArgument(nameIndex >= 0, "name index must be non negative")
            Preconditions.checkArgument(protoIndex >= 0, "proto index must be non negative")
            return MethodID(classIndex, nameIndex, protoIndex)
        }

        @JvmStatic
        fun readContent(input: DexDataInput): MethodID {
            val methodID = MethodID()
            methodID.read(input)
            return methodID
        }
    }
}