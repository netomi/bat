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
package com.github.netomi.bat.classfile.attribute.annotation

import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.attribute.Attribute
import com.github.netomi.bat.classfile.attribute.annotation.visitor.AnnotationVisitor
import com.github.netomi.bat.classfile.attribute.annotation.visitor.AnnotationVisitorIndexed
import com.github.netomi.bat.classfile.constant.visitor.ReferencedConstantVisitor
import com.github.netomi.bat.classfile.io.ClassDataInput
import com.github.netomi.bat.classfile.io.ClassDataOutput
import com.github.netomi.bat.classfile.io.contentSize
import com.github.netomi.bat.util.mutableListOfCapacity
import java.io.IOException

/**
 * A base class representing a Runtime*ParameterAnnotations attribute in a class file.
 */
abstract class RuntimeParameterAnnotationsAttribute
    protected constructor(      override var attributeNameIndex:   Int,
                          protected open var parameterAnnotations: MutableList<MutableList<Annotation>>)
    : Attribute(attributeNameIndex) {

    override val dataSize: Int
        get() = parameterAnnotations.fold(1) { accParam, list -> accParam + list.contentSize() }

    val size: Int
        get() = parameterAnnotations.size

    fun getParameterAnnotationCount(parameterIndex: Int): Int {
        return if (parameterIndex in 0 until parameterAnnotations.size) {
            parameterAnnotations[parameterIndex].size
        } else {
            0
        }
    }

    @Throws(IOException::class)
    override fun readAttributeData(input: ClassDataInput, length: Int) {
        val numParameters = input.readUnsignedByte()
        parameterAnnotations = mutableListOfCapacity(numParameters)
        for (i in 0 until numParameters) {
            val annotations = input.readContentList(Annotation.Companion::readAnnotation)
            parameterAnnotations.add(annotations)
        }
    }

    @Throws(IOException::class)
    override fun writeAttributeData(output: ClassDataOutput) {
        output.writeByte(parameterAnnotations.size)
        for (annotations in parameterAnnotations) {
            output.writeContentList(annotations)
        }
    }

    fun parameterAnnotationsAccept(classFile: ClassFile, parameterIndex: Int, visitor: AnnotationVisitor) {
        if (parameterIndex in 0 until size) {
            for (annotation in parameterAnnotations[parameterIndex]) {
                visitor.visitAnnotation(classFile, annotation)
            }
        }
    }

    fun parameterAnnotationsAcceptIndexed(classFile: ClassFile, parameterIndex: Int, visitor: AnnotationVisitorIndexed) {
        if (parameterIndex in 0 until size) {
            parameterAnnotations[parameterIndex].forEachIndexed { index, annotation -> visitor.visitAnnotation(classFile, index, annotation) }
        }
    }

    override fun referencedConstantsAccept(classFile: ClassFile, visitor: ReferencedConstantVisitor) {
        super.referencedConstantsAccept(classFile, visitor)

        for (parameterIndex in 0 until size) {
            for (annotation in parameterAnnotations[parameterIndex]) {
                annotation.referencedConstantsAccept(classFile, visitor)
            }
        }
    }
}