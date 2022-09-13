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

import com.github.netomi.bat.dexfile.annotation.Annotation
import com.github.netomi.bat.dexfile.annotation.visitor.AnnotationSetVisitor
import com.github.netomi.bat.dexfile.annotation.visitor.allAnnotations
import com.github.netomi.bat.dexfile.annotation.visitor.annotationCollector
import com.github.netomi.bat.dexfile.io.DexDataInput
import com.github.netomi.bat.dexfile.io.DexDataOutput
import com.github.netomi.bat.dexfile.util.DexType
import com.github.netomi.bat.dexfile.visitor.CodeVisitor
import com.github.netomi.bat.dexfile.visitor.DataItemVisitor
import com.github.netomi.bat.dexfile.visitor.PropertyAccessor
import com.github.netomi.bat.dexfile.visitor.ReferencedIDVisitor
import java.util.*

/**
 * A class representing an encoded method inside a dex file.
 *
 * @see [encoded method @ dex format](https://source.android.com/devices/tech/dalvik/dex-format.encoded-method-format)
 */
class EncodedMethod private constructor(methodIndex: Int = NO_INDEX,
                                        accessFlags: Int = 0,
                                        code       : Code = Code.empty()): EncodedMember(accessFlags) {

    private var deltaMethodIndex = 0

    var methodIndex: Int = methodIndex
        private set

    var modifiers: Set<MethodModifier> = MethodModifier.setOf(accessFlags)
        private set

    override fun updateModifiers(accessFlags: Int) {
        modifiers = MethodModifier.setOf(accessFlags)
    }

    var codeOffset = 0
        private set

    var code: Code = code
        internal set

    fun getMethodID(dexFile: DexFile): MethodID {
        return dexFile.getMethodID(methodIndex)
    }

    fun getProtoID(dexFile: DexFile): ProtoID {
        return getMethodID(dexFile).getProtoID(dexFile)
    }

    fun getClassType(dexFile: DexFile): DexType {
        return dexFile.getMethodID(methodIndex).getClassTypeID(dexFile).getType(dexFile)
    }

    override fun getName(dexFile: DexFile): String {
        return getMethodID(dexFile).getName(dexFile)
    }

    fun getShortyType(dexFile: DexFile): String {
        return getMethodID(dexFile).getProtoID(dexFile).getShorty(dexFile)
    }

    fun getParameterTypes(dexFile: DexFile): List<DexType> {
        return getProtoID(dexFile).getParameterTypes(dexFile)
    }

    fun getReturnType(dexFile: DexFile): DexType {
        return getProtoID(dexFile).getReturnType(dexFile)
    }

    fun getDescriptor(dexFile: DexFile): String {
        val protoID = getProtoID(dexFile)
        return protoID.getDescriptor(dexFile)
    }

    val isStatic: Boolean
        get() = modifiers.contains(MethodModifier.STATIC)

    val isAbstract: Boolean
        get() = modifiers.contains(MethodModifier.ABSTRACT)

    val isNative: Boolean
        get() = modifiers.contains(MethodModifier.NATIVE)

    val isPrivate: Boolean
        get() = visibility == Visibility.PRIVATE

    val isConstructor: Boolean
        get() = modifiers.contains(MethodModifier.CONSTRUCTOR)

    val isDirectMethod: Boolean
        get() = isStatic || isPrivate || isConstructor

    internal fun sort() {
        code.sort()
    }

    override fun read(input: DexDataInput) {
        deltaMethodIndex = input.readUleb128()
        accessFlags      = input.readUleb128()
        codeOffset       = input.readUleb128()
    }

    override fun readLinkedDataItems(input: DexDataInput) {
        if (codeOffset != 0) {
            input.offset = codeOffset
            code = Code.read(input)
            code.readLinkedDataItems(input)
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
        if (!isAbstract) {
            visitor.visitCode(dexFile, classDef, this, code)
        }
    }

    fun annotations(dexFile: DexFile, classDef: ClassDef): List<Annotation> {
        val collector = annotationCollector()
        annotationSetAccept(dexFile, classDef, allAnnotations(collector))
        return collector.items()
    }

    fun annotationSetAccept(dexFile: DexFile, classDef: ClassDef, visitor: AnnotationSetVisitor) {
        classDef.methodAnnotationSetAccept(dexFile, classDef, this, visitor)
    }

    override fun dataItemsAccept(dexFile: DexFile, visitor: DataItemVisitor) {
        visitor.visitCode(dexFile, this, code)
        code.dataItemsAccept(dexFile, visitor)
    }

    internal fun referencedIDsAccept(dexFile: DexFile, visitor: ReferencedIDVisitor)
    {
        visitor.visitMethodID(dexFile, PropertyAccessor({ methodIndex }, { methodIndex = it }))
        code.referencedIDsAccept(dexFile, visitor)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val o = other as EncodedMethod

        return methodIndex == o.methodIndex &&
               accessFlags == o.accessFlags &&
               code        == o.code
    }

    override fun hashCode(): Int {
        return Objects.hash(methodIndex, accessFlags, code)
    }

    override fun toString(): String {
        return "EncodedMethod[methodIndex=%d,accessFlags=%04x,code=%d units]".format(methodIndex, accessFlags, code.insSize)
    }

    companion object {
        fun of(methodIndex: Int, visibility: Visibility, vararg modifiers: MethodModifier): EncodedMethod {
            require(methodIndex >= 0) { "methodIndex must not be negative" }
            var accessFlags = visibility.flagValue
            for (modifier in modifiers) {
                accessFlags = accessFlags or modifier.flagValue
            }
            return EncodedMethod(methodIndex, accessFlags)
        }

        fun of(methodIndex: Int, accessFlags: Int): EncodedMethod {
            require(methodIndex >= 0) { "methodIndex must not be negative" }
            return EncodedMethod(methodIndex, accessFlags)
        }

        internal fun read(input: DexDataInput, lastIndex: Int): EncodedMethod {
            val encodedMethod = EncodedMethod()
            encodedMethod.read(input)
            encodedMethod.updateMethodIndex(lastIndex)
            return encodedMethod
        }
    }
}