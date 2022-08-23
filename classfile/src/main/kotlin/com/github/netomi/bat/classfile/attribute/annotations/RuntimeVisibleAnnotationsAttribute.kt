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
import com.github.netomi.bat.classfile.attribute.AttributeType
import com.github.netomi.bat.classfile.attribute.visitor.AttributeVisitor
import java.io.DataInput
import java.io.DataOutput
import java.io.IOException

/**
 * A class representing a RuntimeVisibleAnnotations attribute in a class file.
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se13/html/jvms-4.html#jvms-4.7.16">RuntimeVisibleAnnotations Attribute</a>
 */
data class RuntimeVisibleAnnotationsAttribute internal constructor(
    override val attributeNameIndex: Int,
             var annotations: MutableList<Annotation> = mutableListOf()) : Attribute(attributeNameIndex) {

    override val type: AttributeType
        get() = AttributeType.RUNTIME_VISIBLE_ANNOTATIONS

    @Throws(IOException::class)
    override fun readAttributeData(input: DataInput) {
        val length = input.readInt()

        val annotationCount = input.readUnsignedShort()
        for (i in 0 until annotationCount) {
            annotations.add(Annotation.readAnnotation(input))
        }
    }

    @Throws(IOException::class)
    override fun writeAttributeData(output: DataOutput) {
        output.writeInt(0)
    }

    override fun accept(classFile: ClassFile, visitor: AttributeVisitor) {
        visitor.visitRuntimeVisibleAnnotationsAttribute(classFile, this)
    }

    companion object {
        internal fun empty(attributeNameIndex: Int): RuntimeVisibleAnnotationsAttribute {
            return RuntimeVisibleAnnotationsAttribute(attributeNameIndex)
        }
    }
}