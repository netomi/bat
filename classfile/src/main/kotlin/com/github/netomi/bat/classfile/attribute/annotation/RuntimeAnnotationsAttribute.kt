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
import com.github.netomi.bat.classfile.io.contentSize
import java.io.IOException

/**
 * A base class representing a Runtime*Annotations attribute in a class file.
 */
abstract class RuntimeAnnotationsAttribute
    protected constructor(      override val attributeNameIndex: Int,
                          protected open var annotations:        MutableList<Annotation>) : Attribute(attributeNameIndex), Sequence<Annotation> {

    override val dataSize: Int
        get() = annotations.contentSize()

    val size: Int
        get() = annotations.size

    operator fun get(index: Int): Annotation {
        return annotations[index]
    }

    override fun iterator(): Iterator<Annotation> {
        return annotations.iterator()
    }

    @Throws(IOException::class)
    override fun readAttributeData(input: ClassDataInput, length: Int) {
        annotations = input.readContentList(Annotation.Companion::readAnnotation)
    }

    @Throws(IOException::class)
    override fun writeAttributeData(output: ClassDataOutput) {
        output.writeContentList(annotations)
    }

    fun annotationsAccept(classFile: ClassFile, visitor: AnnotationVisitor) {
        for (annotation in annotations) {
            visitor.visitAnnotation(classFile, annotation)
        }
    }

    fun annotationsAcceptIndexed(classFile: ClassFile, visitor: AnnotationVisitorIndexed) {
        annotations.forEachIndexed { index, annotation -> visitor.visitAnnotation(classFile, index, annotation) }
    }
}