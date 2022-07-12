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
 * A class representing a method annotation format inside a dex file.
 *
 * @see [method annotation format @ dex format](https://source.android.com/devices/tech/dalvik/dex-format.method-annotation)
 */
class MethodAnnotation private constructor(_methodIndex: Int = NO_INDEX, _annotationSet: AnnotationSet = AnnotationSet.empty()) : DexContent() {

    var methodIndex: Int = _methodIndex
        private set

    var annotationsOffset = 0
        private set

    var annotationSet: AnnotationSet = _annotationSet
        private set

    fun getMethodID(dexFile: DexFile): MethodID {
        return dexFile.getMethodID(methodIndex)
    }

    override fun read(input: DexDataInput) {
        methodIndex = input.readInt()
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
        output.writeInt(methodIndex)
        output.writeInt(annotationsOffset)
    }

    fun accept(dexFile: DexFile?, classDef: ClassDef?, visitor: AnnotationSetVisitor) {
        visitor.visitMethodAnnotationSet(dexFile, classDef, this, annotationSet)
    }

    override fun dataItemsAccept(dexFile: DexFile, visitor: DataItemVisitor) {
        visitor.visitMethodAnnotations(dexFile, this, annotationSet)
    }

    internal fun referencedIDsAccept(dexFile: DexFile, visitor: ReferencedIDVisitor) {
        visitor.visitMethodID(dexFile, PropertyAccessor(this::methodIndex))
        annotationSet.referencedIDsAccept(dexFile, visitor)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val o = other as MethodAnnotation

        return methodIndex   == o.methodIndex &&
               annotationSet == o.annotationSet
    }

    override fun hashCode(): Int {
        return Objects.hash(methodIndex, annotationSet)
    }

    override fun toString(): String {
        return "MethodAnnotation[methodIdx=${methodIndex},annotationSet=${annotationSet.annotationCount}}]"
    }

    companion object {
        @JvmStatic
        fun of(methodIndex: Int, annotationSet: AnnotationSet): MethodAnnotation {
            return MethodAnnotation(methodIndex, annotationSet)
        }

        @JvmStatic
        fun readContent(input: DexDataInput): MethodAnnotation {
            val methodAnnotation = MethodAnnotation()
            methodAnnotation.read(input)
            return methodAnnotation
        }
    }
}