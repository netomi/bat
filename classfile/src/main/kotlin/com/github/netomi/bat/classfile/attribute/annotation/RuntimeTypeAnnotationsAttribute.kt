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
import com.github.netomi.bat.classfile.io.ClassDataInput
import com.github.netomi.bat.classfile.io.ClassDataOutput
import com.github.netomi.bat.classfile.io.dataSize
import java.io.IOException

/**
 * A base class representing a Runtime*TypeAnnotations attribute in a class file.
 */
abstract class RuntimeTypeAnnotationsAttribute
    protected constructor(      override val attributeNameIndex: Int,
                          protected open var typeAnnotations:    MutableList<TypeAnnotation>)
    : Attribute(attributeNameIndex), Sequence<TypeAnnotation> {

    override val dataSize: Int
        get() = typeAnnotations.dataSize()

    val size: Int
        get() = typeAnnotations.size

    operator fun get(index: Int): TypeAnnotation {
        return typeAnnotations[index]
    }

    override fun iterator(): Iterator<TypeAnnotation> {
        return typeAnnotations.iterator()
    }

    @Throws(IOException::class)
    override fun readAttributeData(input: ClassDataInput) {
        @Suppress("UNUSED_VARIABLE")
        val length = input.readInt()
        typeAnnotations = input.readContentList(TypeAnnotation.Companion::readTypeAnnotation)
    }

    @Throws(IOException::class)
    override fun writeAttributeData(output: ClassDataOutput) {
        output.writeInt(dataSize)
        output.writeContentList(typeAnnotations)
    }

    fun typeAnnotationsAccept(classFile: ClassFile, visitor: AnnotationVisitor) {
        for (annotation in typeAnnotations) {
            visitor.visitTypeAnnotation(classFile, annotation)
        }
    }

    fun typeAnnotationsAcceptIndexed(classFile: ClassFile, visitor: AnnotationVisitorIndexed) {
        typeAnnotations.forEachIndexed { index, annotation -> visitor.visitTypeAnnotation(classFile, index, annotation) }
    }
}