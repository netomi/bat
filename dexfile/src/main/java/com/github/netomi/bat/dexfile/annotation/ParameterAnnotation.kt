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
import com.github.netomi.bat.dexfile.visitor.*
import com.github.netomi.bat.dexfile.visitor.PropertyAccessor
import com.github.netomi.bat.dexfile.visitor.ReferencedIDVisitor
import java.util.*

/**
 * A class representing a parameter annotation format inside a dex file.
 *
 * @see [parameter annotation format @ dex format](https://source.android.com/devices/tech/dalvik/dex-format.parameter-annotation)
 */
class ParameterAnnotation private constructor(
    _methodIndex:          Int                  = NO_INDEX,
    _annotationSetRefList: AnnotationSetRefList = AnnotationSetRefList.empty()) : DexContent() {

    var annotationsOffset = 0
        private set

    var methodIndex: Int = _methodIndex
        private set

    var annotationSetRefList: AnnotationSetRefList = _annotationSetRefList
        private set

    fun getMethodID(dexFile: DexFile): MethodID {
        return dexFile.getMethodID(methodIndex)
    }

    override fun read(input: DexDataInput) {
        methodIndex       = input.readInt()
        annotationsOffset = input.readInt()
    }

    override fun readLinkedDataItems(input: DexDataInput) {
        input.offset = annotationsOffset
        annotationSetRefList = AnnotationSetRefList.readContent(input)
    }

    override fun updateOffsets(dataItemMap: DataItem.Map) {
        annotationsOffset = dataItemMap.getOffset(annotationSetRefList)
    }

    override fun write(output: DexDataOutput) {
        output.writeInt(methodIndex)
        output.writeInt(annotationsOffset)
    }

    fun accept(dexFile: DexFile?, classDef: ClassDef?, visitor: AnnotationSetVisitor) {
        visitor.visitParameterAnnotationSet(dexFile, classDef, this, annotationSetRefList)
    }

    fun accept(dexFile: DexFile, classDef: ClassDef, parameterIndex: Int, visitor: AnnotationVisitor) {
        if (parameterIndex in 0 until annotationSetRefList.annotationSetRefCount) {
            val annotationSetRef = annotationSetRefList.getAnnotationSetRef(parameterIndex)
            annotationSetRef.accept(dexFile, classDef, visitor)
        }
    }

    override fun dataItemsAccept(dexFile: DexFile, visitor: DataItemVisitor) {
        visitor.visitParameterAnnotations(dexFile, this, annotationSetRefList)
    }

    internal fun referencedIDsAccept(dexFile: DexFile, visitor: ReferencedIDVisitor) {
        for (index in 0 until annotationSetRefList.annotationSetRefCount) {
            annotationSetRefList.getAnnotationSetRef(index).referencedIDsAccept(dexFile, visitor)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val o = other as ParameterAnnotation

        return methodIndex          == o.methodIndex &&
               annotationSetRefList == o.annotationSetRefList
    }

    override fun hashCode(): Int {
        return Objects.hash(methodIndex, annotationSetRefList)
    }

    override fun toString(): String {
        return "ParameterAnnotation[methodIndex=${methodIndex},annotationSetRefList=${annotationSetRefList.annotationSetRefCount} items]"
    }

    companion object {
        fun of(methodIndex: Int, annotationSetRefList: AnnotationSetRefList): ParameterAnnotation {
            return ParameterAnnotation(methodIndex, annotationSetRefList)
        }

        fun readContent(input: DexDataInput): ParameterAnnotation {
            val parameterAnnotation = ParameterAnnotation()
            parameterAnnotation.read(input)
            return parameterAnnotation
        }
    }
}