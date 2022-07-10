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
import com.github.netomi.bat.dexfile.MethodHandleType.Companion.of
import com.github.netomi.bat.dexfile.io.DexDataInput
import com.github.netomi.bat.dexfile.io.DexDataOutput
import com.github.netomi.bat.util.Preconditions
import dev.ahmedmourad.nocopy.annotations.NoCopy
import java.util.*

/**
 * A class representing a method handle item inside a dex file.
 *
 * @see [method handle item @ dex format](https://source.android.com/devices/tech/dalvik/dex-format.method-handle-item)
 */
@DataItemAnn(
    type          = DexConstants.TYPE_METHOD_HANDLE_ITEM,
    dataAlignment = 4,
    dataSection   = false)
@NoCopy
data class MethodHandle private constructor(
    private var methodHandleTypeValue_: Int = 0,       // ushort
    private var fieldOrMethodId_: Int       = NO_INDEX // ushort
) : DataItem() {

    val methodHandleTypeValue: Int
        get() = methodHandleTypeValue_

    val fieldOrMethodId: Int
        get() = fieldOrMethodId_

    val methodHandleType: MethodHandleType
        get() = MethodHandleType.of(methodHandleTypeValue)

    fun getFieldID(dexFile: DexFile): FieldID? {
        return if (methodHandleType.targetsField) dexFile.getFieldID(fieldOrMethodId) else null
    }

    fun getMethodID(dexFile: DexFile): MethodID? {
        return if (methodHandleType.targetsField) null else dexFile.getMethodID(fieldOrMethodId)
    }

    fun getTargetClassType(dexFile: DexFile): String {
        return if (methodHandleType.targetsField) {
            val fieldID = getFieldID(dexFile)
            fieldID!!.getClassType(dexFile)
        } else {
            val methodID = getMethodID(dexFile)
            methodID!!.getClassType(dexFile)
        }
    }

    fun getTargetMemberName(dexFile: DexFile): String {
        return if (methodHandleType.targetsField) {
            val fieldID = getFieldID(dexFile)
            fieldID!!.getName(dexFile)
        } else {
            val methodID = getMethodID(dexFile)
            methodID!!.getName(dexFile)
        }
    }

    fun getTargetMemberDescriptor(dexFile: DexFile): String {
        return if (methodHandleType.targetsField) {
            val fieldID = getFieldID(dexFile)
            fieldID!!.getType(dexFile)
        } else {
            val methodID = getMethodID(dexFile)
            methodID!!.getProtoID(dexFile).getDescriptor(dexFile)
        }
    }

    fun getTargetDescriptor(dexFile: DexFile): String {
        return if (methodHandleType.targetsInstance) {
            "(%s%s".format(getTargetClassType(dexFile), getTargetMemberDescriptor(dexFile).substring(1))
        } else {
            getTargetMemberDescriptor(dexFile)
        }
    }

    override fun read(input: DexDataInput) {
        input.skipAlignmentPadding(dataAlignment)
        methodHandleTypeValue_ = input.readUnsignedShort()
        input.readUnsignedShort()
        fieldOrMethodId_ = input.readUnsignedShort()
        input.readUnsignedShort()
    }

    override fun write(output: DexDataOutput) {
        output.writeAlignmentPadding(dataAlignment)
        output.writeUnsignedShort(methodHandleTypeValue)
        output.writeUnsignedShort(0x0)
        output.writeUnsignedShort(fieldOrMethodId)
        output.writeUnsignedShort(0x0)
    }

    override fun toString(): String {
        return "MethodHandle[type=%02x,fieldOrMethodId=%d]".format(methodHandleTypeValue_, fieldOrMethodId_)
    }

    companion object {
        @JvmStatic
        fun of(methodHandleType: MethodHandleType, fieldOrMethodId: Int): MethodHandle {
            Objects.requireNonNull(methodHandleType, "methodHandleType must not be null")
            return of(methodHandleType.value, fieldOrMethodId)
        }

        @JvmStatic
        fun of(methodHandleType: Int, fieldOrMethodId: Int): MethodHandle {
            Preconditions.checkArgument(fieldOrMethodId >= 0, "fieldOrMethodId must not be negative")
            return MethodHandle(methodHandleType, fieldOrMethodId)
        }

        @JvmStatic
        fun readContent(input: DexDataInput): MethodHandle {
            val methodHandle = MethodHandle()
            methodHandle.read(input)
            return methodHandle
        }
    }
}