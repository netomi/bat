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
package com.github.netomi.bat.classfile.attribute.annotations

import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.attribute.Attribute
import com.github.netomi.bat.util.mutableListOfCapacity
import java.io.DataInput
import java.io.DataOutput
import java.io.IOException

/**
 * A base class representing a Runtime*Annotations attributes in a class file.
 */
abstract class RuntimeAnnotationsAttribute
    protected constructor(      override val attributeNameIndex: Int,
                          protected open var _annotations:       MutableList<Annotation>) : Attribute(attributeNameIndex) {

    val annotations: List<Annotation>
        get() = _annotations

    override val dataSize: Int
        get() = TODO("implement")

    @Throws(IOException::class)
    override fun readAttributeData(input: DataInput, classFile: ClassFile) {
        @Suppress("UNUSED_VARIABLE")
        val length = input.readInt()

        val annotationCount = input.readUnsignedShort()
        _annotations = mutableListOfCapacity(annotationCount)
        for (i in 0 until annotationCount) {
            _annotations.add(Annotation.readAnnotation(input))
        }
    }

    @Throws(IOException::class)
    override fun writeAttributeData(output: DataOutput) {
        output.writeInt(dataSize)

        output.writeShort(annotations.size)
        for (annotation in annotations) {
            annotation.write(output)
        }
    }
}