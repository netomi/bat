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
package com.github.netomi.bat.dexfile.annotation

import com.github.netomi.bat.dexfile.*
import com.github.netomi.bat.dexfile.io.DexDataInput
import com.github.netomi.bat.dexfile.io.DexDataOutput
import com.github.netomi.bat.dexfile.visitor.AnnotationSetVisitor
import com.github.netomi.bat.dexfile.visitor.DataItemVisitor
import com.github.netomi.bat.dexfile.visitor.PropertyAccessor
import com.github.netomi.bat.dexfile.visitor.ReferencedIDVisitor
import java.util.*

/**
 * A class representing a field annotation format inside a dex file.
 *
 * @see [field annotation format @ dex format](https://source.android.com/devices/tech/dalvik/dex-format.field-annotation)
 */
class FieldAnnotation private constructor(_fieldIndex: Int = NO_INDEX, _annotationSet: AnnotationSet = AnnotationSet.empty()): DexContent() {

    var fieldIndex: Int = _fieldIndex
        private set

    var annotationsOffset = 0
        private set

    var annotationSet: AnnotationSet = _annotationSet
        private set

    fun getFieldID(dexFile: DexFile): FieldID {
        return dexFile.getFieldID(fieldIndex)
    }

    override fun read(input: DexDataInput) {
        fieldIndex        = input.readInt()
        annotationsOffset = input.readInt()
    }

    override fun readLinkedDataItems(input: DexDataInput) {
        input.offset = annotationsOffset
        annotationSet = AnnotationSet.readContent(input)
    }

    override fun updateOffsets(dataItemMap: DataItem.Map) {
        annotationsOffset = dataItemMap.getOffset(annotationSet)
    }

    override fun write(output: DexDataOutput) {
        output.writeInt(fieldIndex)
        output.writeInt(annotationsOffset)
    }

    fun accept(dexFile: DexFile, classDef: ClassDef, visitor: AnnotationSetVisitor) {
        visitor.visitFieldAnnotationSet(dexFile, classDef, this, annotationSet)
    }

    override fun dataItemsAccept(dexFile: DexFile, visitor: DataItemVisitor) {
        visitor.visitFieldAnnotations(dexFile, this, annotationSet)
        annotationSet.dataItemsAccept(dexFile, visitor)
    }

    internal fun referencedIDsAccept(dexFile: DexFile, visitor: ReferencedIDVisitor) {
        visitor.visitFieldID(dexFile, PropertyAccessor({ fieldIndex }, { fieldIndex = it }))
        annotationSet.referencedIDsAccept(dexFile, visitor)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val o = other as FieldAnnotation

        return fieldIndex    == o.fieldIndex &&
               annotationSet == o.annotationSet
    }

    override fun hashCode(): Int {
        return Objects.hash(fieldIndex, annotationSet)
    }

    override fun toString(): String {
        return "FieldAnnotation[fieldIdx=${fieldIndex},annotationSet=${annotationSet.annotationCount} items]"
    }

    companion object {
        fun of(fieldIndex: Int, annotationSet: AnnotationSet): FieldAnnotation {
            return FieldAnnotation(fieldIndex, annotationSet)
        }

        fun readContent(input: DexDataInput): FieldAnnotation {
            val fieldAnnotation = FieldAnnotation()
            fieldAnnotation.read(input)
            return fieldAnnotation
        }
    }
}