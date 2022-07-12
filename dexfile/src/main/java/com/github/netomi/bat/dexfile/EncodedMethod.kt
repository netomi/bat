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
import com.github.netomi.bat.dexfile.visitor.CodeVisitor
import com.github.netomi.bat.dexfile.visitor.DataItemVisitor
import com.google.common.base.Preconditions
import java.util.*

/**
 * A class representing an encoded method inside a dex file.
 *
 * @see [encoded method @ dex format](https://source.android.com/devices/tech/dalvik/dex-format.encoded-method-format)
 */
class EncodedMethod private constructor(_methodIndex: Int = NO_INDEX, _accessFlags: Int = 0): DexContent() {

    private var deltaMethodIndex = 0

    var methodIndex: Int = _methodIndex
        private set

    var accessFlags: Int = _accessFlags
        private set

    val visibility: Visibility
        get() = Visibility.of(accessFlags)

    val modifiers: EnumSet<MethodModifier>
        get() = MethodModifier.setOf(accessFlags)

    var codeOffset = 0
        private set

    var code: Code? = null

    fun getMethodID(dexFile: DexFile): MethodID {
        return dexFile.getMethodID(methodIndex)
    }

    fun getProtoID(dexFile: DexFile): ProtoID {
        return getMethodID(dexFile).getProtoID(dexFile)
    }

    fun getClassType(dexFile: DexFile): String {
        return dexFile.getMethodID(methodIndex).getClassTypeID(dexFile).getType(dexFile)
    }

    fun getName(dexFile: DexFile): String {
        return getMethodID(dexFile).getName(dexFile)
    }

    fun getShortyType(dexFile: DexFile): String {
        return getMethodID(dexFile).getProtoID(dexFile).getShorty(dexFile)
    }

    fun getDescriptor(dexFile: DexFile): String {
        val protoID = getProtoID(dexFile)
        return protoID.getDescriptor(dexFile)
    }

    val isStatic: Boolean
        get() = accessFlags and ACC_STATIC != 0

    val isPrivate: Boolean
        get() = accessFlags and ACC_PRIVATE != 0

    val isConstructor: Boolean
        get() = accessFlags and ACC_CONSTRUCTOR != 0

    val isDirectMethod: Boolean
        get() = isStatic || isPrivate || isConstructor

    override fun read(input: DexDataInput) {
        deltaMethodIndex = input.readUleb128()
        accessFlags      = input.readUleb128()
        codeOffset       = input.readUleb128()
    }

    override fun readLinkedDataItems(input: DexDataInput) {
        if (codeOffset != 0) {
            input.offset = codeOffset
            code = Code.readItem(input)
            code?.readLinkedDataItems(input)
        }
    }

    private fun updateMethodIndex(lastIndex: Int) {
        methodIndex = deltaMethodIndex + lastIndex
    }

    private fun updateDeltaMethodIndex(lastIndex: Int) {
        deltaMethodIndex = methodIndex - lastIndex
    }

    override fun updateOffsets(dataItemMap: DataItem.Map) {
        codeOffset = dataItemMap.getOffset(code)
    }

    fun write(output: DexDataOutput, lastIndex: Int): Int {
        updateDeltaMethodIndex(lastIndex)
        write(output)
        return methodIndex
    }

    override fun write(output: DexDataOutput) {
        output.writeUleb128(deltaMethodIndex)
        output.writeUleb128(accessFlags)
        output.writeUleb128(codeOffset)
    }

    fun codeAccept(dexFile: DexFile, classDef: ClassDef, visitor: CodeVisitor) {
        if (code != null) {
            visitor.visitCode(dexFile, classDef, this, code)
        }
    }

    override fun dataItemsAccept(dexFile: DexFile, visitor: DataItemVisitor) {
        if (code != null) {
            visitor.visitCode(dexFile, this, code!!)
            code!!.dataItemsAccept(dexFile, visitor)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val other = other as EncodedMethod

        return methodIndex == other.methodIndex &&
               accessFlags == other.accessFlags &&
               code        == other.code
    }

    override fun hashCode(): Int {
        return Objects.hash(methodIndex, accessFlags, code)
    }

    override fun toString(): String {
        return "EncodedMethod[methodIndex=%d,accessFlags=%04x,code=%d]".format(methodIndex, accessFlags, code?.insSize)
    }

    companion object {
        fun of(methodIndex: Int, visibility: Visibility, vararg modifiers: MethodModifier): EncodedMethod {
            Preconditions.checkArgument(methodIndex >= 0, "methodIndex must not be negative")
            var accessFlags = visibility.flagValue
            for (modifier in modifiers) {
                accessFlags = accessFlags or modifier.flagValue
            }
            return EncodedMethod(methodIndex, accessFlags)
        }

        fun of(methodIndex: Int, accessFlags: Int): EncodedMethod {
            Preconditions.checkArgument(methodIndex >= 0, "methodIndex must not be negative")
            return EncodedMethod(methodIndex, accessFlags)
        }

        @JvmStatic
        fun readContent(input: DexDataInput, lastIndex: Int): EncodedMethod {
            val encodedMethod = EncodedMethod()
            encodedMethod.read(input)
            encodedMethod.updateMethodIndex(lastIndex)
            return encodedMethod
        }
    }
}