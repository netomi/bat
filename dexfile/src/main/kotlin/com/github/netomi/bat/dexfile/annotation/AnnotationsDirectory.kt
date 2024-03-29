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
import com.github.netomi.bat.dexfile.annotation.visitor.AnnotationSetVisitor
import com.github.netomi.bat.dexfile.visitor.DataItemVisitor
import com.github.netomi.bat.dexfile.visitor.ReferencedIDVisitor
import com.github.netomi.bat.util.mutableListOfCapacity
import java.util.*

@DataItemAnn(
    type          = TYPE_ANNOTATIONS_DIRECTORY_ITEM,
    dataAlignment = 4,
    dataSection   = true)
internal class AnnotationsDirectory
    private constructor(classAnnotations:     AnnotationSet                    = AnnotationSet.empty(),
                        fieldAnnotations:     MutableList<FieldAnnotation>     = mutableListOfCapacity(0),
                        methodAnnotations:    MutableList<MethodAnnotation>    = mutableListOfCapacity(0),
                        parameterAnnotations: MutableList<ParameterAnnotation> = mutableListOfCapacity(0)) : DataItem() {

    var classAnnotationsOffset = 0
        private set

    internal var classAnnotations: AnnotationSet = classAnnotations
        private set

    internal var fieldAnnotations: MutableList<FieldAnnotation> = fieldAnnotations
        private set

    internal var methodAnnotations: MutableList<MethodAnnotation> = methodAnnotations
        private set

    internal var parameterAnnotations: MutableList<ParameterAnnotation> = parameterAnnotations
        private set

    override val isEmpty: Boolean
        get() = classAnnotations.isEmpty    &&
                fieldAnnotations.isEmpty()  &&
                methodAnnotations.isEmpty() &&
                parameterAnnotations.isEmpty()

    internal fun sort() {
        fieldAnnotations.sortWith(compareBy { it.fieldIndex })
        methodAnnotations.sortWith(compareBy { it.methodIndex })
        parameterAnnotations.sortWith(compareBy { it.methodIndex })
    }

    override fun read(input: DexDataInput) {
        classAnnotationsOffset = input.readInt()

        val fieldAnnotationsSize    = input.readInt()
        val methodAnnotationsSize   = input.readInt()
        val parameterAnnotationSize = input.readInt()

        fieldAnnotations = mutableListOfCapacity(fieldAnnotationsSize)
        for (i in 0 until fieldAnnotationsSize) {
            val fieldAnnotation = FieldAnnotation.read(input)
            fieldAnnotations.add(fieldAnnotation)
        }

        methodAnnotations = mutableListOfCapacity(methodAnnotationsSize)
        for (i in 0 until methodAnnotationsSize) {
            val methodAnnotation = MethodAnnotation.read(input)
            methodAnnotations.add(methodAnnotation)
        }

        parameterAnnotations = mutableListOfCapacity(parameterAnnotationSize)
        for (i in 0 until parameterAnnotationSize) {
            val parameterAnnotation = ParameterAnnotation.read(input)
            parameterAnnotations.add(parameterAnnotation)
        }
    }

    override fun readLinkedDataItems(input: DexDataInput) {
        if (classAnnotationsOffset != 0) {
            input.offset = classAnnotationsOffset
            classAnnotations = AnnotationSet.read(input)
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
        visitor.visitClassAnnotationSet(dexFile, classDef, classAnnotations)
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
                                     "methodAnnotationCount=%d,parameterAnnotationCount=%d]").format(classAnnotations.size,
                                                                                                     fieldAnnotations.size,
                                                                                                     methodAnnotations.size,
                                                                                                     parameterAnnotations.size)
    }

    companion object {
        internal fun empty(): AnnotationsDirectory {
            return AnnotationsDirectory()
        }

        internal fun read(input: DexDataInput): AnnotationsDirectory {
            val annotationsDirectory = AnnotationsDirectory()
            annotationsDirectory.read(input)
            return annotationsDirectory
        }
    }
}