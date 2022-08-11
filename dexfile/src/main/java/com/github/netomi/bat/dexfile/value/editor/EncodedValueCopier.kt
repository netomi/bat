/*
 *  Copyright (c) 2020-2022 Thomas Neidhart.
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

package com.github.netomi.bat.dexfile.value.editor

import com.github.netomi.bat.dexfile.DexFile
import com.github.netomi.bat.dexfile.MethodHandle
import com.github.netomi.bat.dexfile.editor.DexEditor
import com.github.netomi.bat.dexfile.value.*
import com.github.netomi.bat.dexfile.value.visitor.EncodedValueVisitor

internal fun EncodedValue.copyTo(originDexFile: DexFile, targetDexEditor: DexEditor): EncodedValue {
    var result: EncodedValue? = null
    accept(originDexFile, EncodedValueCopier(targetDexEditor) { _, value -> result = value })
    return result ?: throw IllegalStateException("$this could not be copied")
}

private class EncodedValueCopier constructor(private val targetDexEditor: DexEditor,
                                             private val visitor:         EncodedValueVisitor): EncodedValueVisitor {

    private val targetDexFile = targetDexEditor.dexFile

    override fun visitAnyValue(dexFile: DexFile, value: EncodedValue) {
        throw IllegalStateException("unexpected visiting of visitAnyValue()")
    }

    override fun visitAnnotationValue(dexFile: DexFile, value: EncodedAnnotationValue) {
        val type = value.getType(dexFile)

        val targetTypeIndex = targetDexEditor.addOrGetTypeIDIndex(type)
        val targetElements = mutableListOf<AnnotationElement>()

        for (annotationElement in value.elements) {
            val name = annotationElement.getName(dexFile)

            val targetNameIndex = targetDexEditor.addOrGetStringIDIndex(name)
            targetElements.add(AnnotationElement.of(targetNameIndex, annotationElement.value.copyTo(dexFile, targetDexEditor)))
        }

        val copy = EncodedAnnotationValue.of(targetTypeIndex, targetElements)
        copy.accept(targetDexFile, visitor)
    }

    override fun visitArrayValue(dexFile: DexFile, array: EncodedArrayValue) {
        val targetValues = mutableListOf<EncodedValue>()
        for (arrayValue in array) {
            targetValues.add(arrayValue.copyTo(dexFile, targetDexEditor))
        }

        val copy = EncodedArrayValue.of(targetValues)
        copy.accept(targetDexFile, visitor)
    }

    override fun visitBooleanValue(dexFile: DexFile, value: EncodedBooleanValue) {
        val copy = EncodedBooleanValue.of(value.value)
        copy.accept(targetDexFile, visitor)
    }

    override fun visitByteValue(dexFile: DexFile, value: EncodedByteValue) {
        val copy = EncodedByteValue.of(value.value)
        copy.accept(targetDexFile, visitor)
    }

    override fun visitCharValue(dexFile: DexFile, value: EncodedCharValue) {
        val copy = EncodedCharValue.of(value.value)
        copy.accept(targetDexFile, visitor)
    }

    override fun visitDoubleValue(dexFile: DexFile, value: EncodedDoubleValue) {
        val copy = EncodedDoubleValue.of(value.value)
        copy.accept(targetDexFile, visitor)
    }

    override fun visitEnumValue(dexFile: DexFile, value: EncodedEnumValue) {
        val targetFieldIDIndex = targetDexEditor.addOrGetFieldIDIndex(dexFile, value.getFieldID(dexFile))
        val copy = EncodedEnumValue.of(targetFieldIDIndex)
        copy.accept(targetDexFile, visitor)
    }

    override fun visitFieldValue(dexFile: DexFile, value: EncodedFieldValue) {
        val targetFieldIDIndex = targetDexEditor.addOrGetFieldIDIndex(dexFile, value.getFieldID(dexFile))
        val copy = EncodedFieldValue.of(targetFieldIDIndex)
        copy.accept(targetDexFile, visitor)
    }

    override fun visitFloatValue(dexFile: DexFile, value: EncodedFloatValue) {
        val copy = EncodedFloatValue.of(value.value)
        copy.accept(targetDexFile, visitor)
    }

    override fun visitIntValue(dexFile: DexFile, value: EncodedIntValue) {
        val copy = EncodedIntValue.of(value.value)
        copy.accept(targetDexFile, visitor)
    }

    override fun visitLongValue(dexFile: DexFile, value: EncodedLongValue) {
        val copy = EncodedLongValue.of(value.value)
        copy.accept(targetDexFile, visitor)
    }

    override fun visitMethodHandleValue(dexFile: DexFile, value: EncodedMethodHandleValue) {
        val methodHandle = value.getMethodHandle(dexFile)
        val methodHandleType = methodHandle.methodHandleType

        val targetMethodHandle = if (methodHandleType.targetsField) {
            val targetFieldIDIndex = targetDexEditor.addOrGetFieldIDIndex(dexFile, methodHandle.getFieldID(dexFile)!!)
            MethodHandle.of(methodHandleType, targetFieldIDIndex)
        } else {
            val targetMethodIDIndex = targetDexEditor.addOrGetMethodIDIndex(dexFile, methodHandle.getMethodID(dexFile)!!)
            MethodHandle.of(methodHandleType, targetMethodIDIndex)
        }

        val targetMethodHandleIndex = targetDexEditor.addOrGetMethodHandleIndex(targetMethodHandle)

        val copy = EncodedMethodHandleValue.of(targetMethodHandleIndex)
        copy.accept(targetDexFile, visitor)
    }

    override fun visitMethodTypeValue(dexFile: DexFile, value: EncodedMethodTypeValue) {
        val targetProtoIDIndex = targetDexEditor.addOrGetProtoIDIndex(dexFile, value.getProtoID(dexFile))
        val copy = EncodedMethodTypeValue.of(targetProtoIDIndex)
        copy.accept(targetDexFile, visitor)
    }

    override fun visitMethodValue(dexFile: DexFile, value: EncodedMethodValue) {
        val targetMethodIDIndex = targetDexEditor.addOrGetMethodIDIndex(dexFile, value.getMethodID(dexFile))
        val copy = EncodedMethodValue.of(targetMethodIDIndex)
        copy.accept(targetDexFile, visitor)
    }

    override fun visitNullValue(dexFile: DexFile, value: EncodedNullValue) {
        EncodedNullValue.accept(targetDexFile, visitor)
    }

    override fun visitShortValue(dexFile: DexFile, value: EncodedShortValue) {
        val copy = EncodedShortValue.of(value.value)
        copy.accept(targetDexFile, visitor)
    }

    override fun visitStringValue(dexFile: DexFile, value: EncodedStringValue) {
        val targetStringIndex = targetDexEditor.addOrGetStringIDIndex(value.getStringValue(dexFile))
        val copy = EncodedStringValue.of(targetStringIndex)
        copy.accept(targetDexFile, visitor)
    }

    override fun visitTypeValue(dexFile: DexFile, value: EncodedTypeValue) {
        val targetTypeIndex = targetDexEditor.addOrGetTypeIDIndex(value.getType(dexFile))
        val copy = EncodedTypeValue.of(targetTypeIndex)
        copy.accept(targetDexFile, visitor)
    }
}