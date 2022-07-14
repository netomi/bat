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
import com.github.netomi.bat.dexfile.visitor.ReferencedIDVisitor
import java.util.*
import kotlin.collections.ArrayList

@DataItemAnn(
    type          = TYPE_ANNOTATIONS_DIRECTORY_ITEM,
    dataAlignment = 4,
    dataSection   = true)
class AnnotationsDirectory private constructor(
    _classAnnotations:     AnnotationSet                  = AnnotationSet.empty(),
    _fieldAnnotations:     ArrayList<FieldAnnotation>     = ArrayList(0),
    _methodAnnotations:    ArrayList<MethodAnnotation>    = ArrayList(0),
    _parameterAnnotations: ArrayList<ParameterAnnotation> = ArrayList(0)) : DataItem() {

    var classAnnotationsOffset = 0
        private set

    var classAnnotations: AnnotationSet = _classAnnotations
        private set

    val fieldAnnotations:     ArrayList<FieldAnnotation>     = _fieldAnnotations
    val methodAnnotations:    ArrayList<MethodAnnotation>    = _methodAnnotations
    val parameterAnnotations: ArrayList<ParameterAnnotation> = _parameterAnnotations

    override val isEmpty: Boolean
        get() = classAnnotations.isEmpty    &&
                fieldAnnotations.isEmpty()  &&
                methodAnnotations.isEmpty() &&
                parameterAnnotations.isEmpty()

    override fun read(input: DexDataInput) {
        input.skipAlignmentPadding(dataAlignment)
        classAnnotationsOffset = input.readInt()

        val fieldAnnotationsSize    = input.readInt()
        val methodAnnotationsSize   = input.readInt()
        val parameterAnnotationSize = input.readInt()

        fieldAnnotations.clear()
        fieldAnnotations.ensureCapacity(fieldAnnotationsSize)
        for (i in 0 until fieldAnnotationsSize) {
            val fieldAnnotation = FieldAnnotation.readContent(input)
            fieldAnnotations.add(fieldAnnotation)
        }

        methodAnnotations.clear()
        methodAnnotations.ensureCapacity(methodAnnotationsSize)
        for (i in 0 until methodAnnotationsSize) {
            val methodAnnotation = MethodAnnotation.readContent(input)
            methodAnnotations.add(methodAnnotation)
        }

        parameterAnnotations.clear()
        parameterAnnotations.ensureCapacity(parameterAnnotationSize)
        for (i in 0 until parameterAnnotationSize) {
            val parameterAnnotation = ParameterAnnotation.readContent(input)
            parameterAnnotations.add(parameterAnnotation)
        }
    }

    override fun readLinkedDataItems(input: DexDataInput) {
        if (classAnnotationsOffset != 0) {
            input.offset = classAnnotationsOffset
            classAnnotations = AnnotationSet.readContent(input)
        }

        for (fieldAnnotation in fieldAnnotations) {
            fieldAnnotation.readLinkedDataItems(input)
        }
        for (methodAnnotation in methodAnnotations) {
            methodAnnotation.readLinkedDataItems(input)
        }
        for (parameterAnnotation in parameterAnnotations) {
            parameterAnnotation.readLinkedDataItems(input)
        }
    }

    override fun updateOffsets(dataItemMap: Map) {
        classAnnotationsOffset = dataItemMap.getOffset(classAnnotations)

        for (fieldAnnotation in fieldAnnotations) {
            fieldAnnotation.updateOffsets(dataItemMap)
        }
        for (methodAnnotation in methodAnnotations) {
            methodAnnotation.updateOffsets(dataItemMap)
        }
        for (parameterAnnotation in parameterAnnotations) {
            parameterAnnotation.updateOffsets(dataItemMap)
        }
    }

    override fun write(output: DexDataOutput) {
        output.writeAlignmentPadding(dataAlignment)
        output.writeInt(classAnnotationsOffset)
        output.writeInt(fieldAnnotations.size)
        output.writeInt(methodAnnotations.size)
        output.writeInt(parameterAnnotations.size)
        for (fieldAnnotation in fieldAnnotations) {
            fieldAnnotation.write(output)
        }
        for (methodAnnotation in methodAnnotations) {
            methodAnnotation.write(output)
        }
        for (parameterAnnotation in parameterAnnotations) {
            parameterAnnotation.write(output)
        }
    }

    fun accept(dexFile: DexFile, classDef: ClassDef, visitor: AnnotationSetVisitor) {
        classAnnotationSetAccept(dexFile, classDef, visitor)
        for (fieldAnnotation in fieldAnnotations) {
            fieldAnnotation.accept(dexFile, classDef, visitor)
        }
        for (methodAnnotation in methodAnnotations) {
            methodAnnotation.accept(dexFile, classDef, visitor)
        }
        for (parameterAnnotation in parameterAnnotations) {
            parameterAnnotation.accept(dexFile, classDef, visitor)
        }
    }

    fun classAnnotationSetAccept(dexFile: DexFile, classDef: ClassDef, visitor: AnnotationSetVisitor) {
        visitor.visitClassAnnotationSet(dexFile, classDef, classAnnotations)
    }

    fun fieldAnnotationSetAccept(dexFile: DexFile, classDef: ClassDef, field: EncodedField, visitor: AnnotationSetVisitor) {
        fieldAnnotations.filter { it.fieldIndex == field.fieldIndex }.map { it.accept(dexFile, classDef, visitor) }
    }

    fun methodAnnotationSetAccept(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, visitor: AnnotationSetVisitor) {
        methodAnnotations.filter { it.methodIndex == method.methodIndex }.map { it.accept(dexFile, classDef, visitor) }
    }

    fun parameterAnnotationSetAccept(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, visitor: AnnotationSetVisitor) {
        parameterAnnotations.filter { it.methodIndex == method.methodIndex }.map { it.accept(dexFile, classDef, visitor) }
    }

    override fun dataItemsAccept(dexFile: DexFile, visitor: DataItemVisitor) {
        visitor.visitClassAnnotations(dexFile, this, classAnnotations)
        classAnnotations.dataItemsAccept(dexFile, visitor)

        for (fieldAnnotation in fieldAnnotations) {
            fieldAnnotation.dataItemsAccept(dexFile, visitor)
        }
        for (methodAnnotation in methodAnnotations) {
            methodAnnotation.dataItemsAccept(dexFile, visitor)
        }
        for (parameterAnnotation in parameterAnnotations) {
            parameterAnnotation.dataItemsAccept(dexFile, visitor)
        }
    }

    internal fun referencedIDsAccept(dexFile: DexFile, visitor: ReferencedIDVisitor) {
        classAnnotations.referencedIDsAccept(dexFile, visitor)
        fieldAnnotations.forEach { it.referencedIDsAccept(dexFile, visitor) }
        methodAnnotations.forEach { it.referencedIDsAccept(dexFile, visitor) }
        parameterAnnotations.forEach { it.referencedIDsAccept(dexFile, visitor) }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        val o = other as AnnotationsDirectory

        return classAnnotations     == o.classAnnotations  &&
               fieldAnnotations     == o.fieldAnnotations  &&
               methodAnnotations    == o.methodAnnotations &&
               parameterAnnotations == o.parameterAnnotations
    }

    override fun hashCode(): Int {
        return Objects.hash(classAnnotations,
                            fieldAnnotations,
                            methodAnnotations,
                            parameterAnnotations)
    }

    override fun toString(): String {
        return ("AnnotationsDirectory[classAnnotationCount=%d,fieldAnnotationCount=%d," +
                                     "methodAnnotationCount=%d,parameterAnnotationCount=%d]").format(classAnnotations.annotationCount,
                                                                                                     fieldAnnotations.size,
                                                                                                     methodAnnotations.size,
                                                                                                     parameterAnnotations.size)
    }

    companion object {
        fun empty(): AnnotationsDirectory {
            return AnnotationsDirectory()
        }

        fun readContent(input: DexDataInput): AnnotationsDirectory {
            val annotationsDirectory = AnnotationsDirectory()
            annotationsDirectory.read(input)
            return annotationsDirectory
        }
    }
}