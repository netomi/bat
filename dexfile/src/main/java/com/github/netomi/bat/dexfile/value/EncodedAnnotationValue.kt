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
package com.github.netomi.bat.dexfile.value

import com.github.netomi.bat.dexfile.DexContent
import com.github.netomi.bat.dexfile.DexFile
import com.github.netomi.bat.dexfile.NO_INDEX
import com.github.netomi.bat.dexfile.io.DexDataInput
import com.github.netomi.bat.dexfile.io.DexDataOutput
import com.github.netomi.bat.dexfile.value.visitor.AnnotationElementVisitor
import com.github.netomi.bat.dexfile.value.visitor.EncodedValueVisitor
import com.github.netomi.bat.dexfile.visitor.PropertyAccessor
import com.github.netomi.bat.dexfile.visitor.ReferencedIDVisitor
import kotlin.collections.ArrayList

/**
 * A class representing an annotation value (TypeID + AnnotationElements) inside a dex file.
 */
data class EncodedAnnotationValue
    private constructor(private var _typeIndex: Int                          = NO_INDEX,
                        private val elements:   ArrayList<AnnotationElement> = ArrayList(0)): EncodedValue(), Sequence<AnnotationElement> {

    val typeIndex: Int
        get() = _typeIndex

    override val valueType: EncodedValueType
        get() = EncodedValueType.ANNOTATION

    fun getType(dexFile: DexFile): String {
        return dexFile.getTypeID(typeIndex).getType(dexFile)
    }

    val annotationElementCount: Int
        get() = elements.size

    fun getAnnotationElement(index: Int): AnnotationElement {
        return elements[index]
    }

    override fun iterator(): Iterator<AnnotationElement> {
        return elements.iterator()
    }

    override fun readValue(input: DexDataInput, valueArg: Int) {
        _typeIndex = input.readUleb128()
        val size = input.readUleb128()
        elements.clear()
        elements.ensureCapacity(size)
        for (i in 0 until size) {
            val element = AnnotationElement.readContent(input)
            elements.add(element)
        }
    }

    override fun writeType(output: DexDataOutput): Int {
        return writeType(output, 0)
    }

    override fun writeValue(output: DexDataOutput, valueArg: Int) {
        output.writeUleb128(typeIndex)
        output.writeUleb128(elements.size)
        for (element in elements) {
            element.write(output)
        }
    }

    override fun accept(dexFile: DexFile, visitor: EncodedValueVisitor) {
        visitor.visitAnnotationValue(dexFile, this)
    }

    override fun referencedIDsAccept(dexFile: DexFile, visitor: ReferencedIDVisitor) {
        visitor.visitTypeID(dexFile, PropertyAccessor({ _typeIndex }, { _typeIndex = it }))
        elements.forEach { it.referencedIDsAccept(dexFile, visitor) }
    }

    fun annotationElementsAccept(dexFile: DexFile, visitor: AnnotationElementVisitor) {
        for (element in elements) {
            element.accept(dexFile, visitor)
        }
    }

    override fun toString(): String {
        return "EncodedAnnotationValue[typeIndex=${typeIndex},elements=${elements.size} items]"
    }

    companion object {
        internal fun empty(): EncodedAnnotationValue {
            return EncodedAnnotationValue()
        }

        fun of(typeIndex: Int, elements: Collection<AnnotationElement>): EncodedAnnotationValue {
            return EncodedAnnotationValue(typeIndex, ArrayList(elements))
        }

        fun of(typeIndex: Int, vararg elements: AnnotationElement): EncodedAnnotationValue {
            return EncodedAnnotationValue(typeIndex, ArrayList(elements.toList()))
        }
    }
}

/**
 * A class representing an annotation element inside a dex file.
 */
data class AnnotationElement private constructor(private var _nameIndex: Int          = NO_INDEX,
                                                 private var _value:     EncodedValue = EncodedNullValue) : DexContent() {

    val nameIndex: Int
        get() = _nameIndex

    val value: EncodedValue
        get() = _value

    fun getName(dexFile: DexFile): String {
        return dexFile.getStringID(nameIndex).stringValue
    }

    override fun read(input: DexDataInput) {
        _nameIndex = input.readUleb128()
        _value     = EncodedValue.read(input)
    }

    override fun write(output: DexDataOutput) {
        output.writeUleb128(nameIndex)
        value.write(output)
    }

    fun accept(dexFile: DexFile, visitor: AnnotationElementVisitor) {
        visitor.visitAnnotationElement(dexFile, this)
    }

    internal fun referencedIDsAccept(dexFile: DexFile, visitor: ReferencedIDVisitor) {
        visitor.visitStringID(dexFile, PropertyAccessor({ _nameIndex }, { _nameIndex = it }))
        value.referencedIDsAccept(dexFile, visitor)
    }

    override fun toString(): String {
        return "AnnotationElement[nameIndex=${nameIndex},value=${value}]"
    }

    companion object {
        fun of(nameIndex: Int, value: EncodedValue): AnnotationElement {
            require(nameIndex >= 0) { "nameIndex must not be negative" }
            return AnnotationElement(nameIndex, value)
        }

        fun readContent(input: DexDataInput): AnnotationElement {
            val annotationElement = AnnotationElement()
            annotationElement.read(input)
            return annotationElement
        }
    }
}