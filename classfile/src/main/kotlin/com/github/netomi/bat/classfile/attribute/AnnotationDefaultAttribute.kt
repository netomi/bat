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
package com.github.netomi.bat.classfile.attribute

import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.Field
import com.github.netomi.bat.classfile.Method
import com.github.netomi.bat.classfile.annotation.ClassElementValue
import com.github.netomi.bat.classfile.annotation.ElementValue
import com.github.netomi.bat.classfile.annotation.visitor.ElementValueVisitor
import com.github.netomi.bat.classfile.attribute.visitor.FieldAttributeVisitor
import com.github.netomi.bat.classfile.attribute.visitor.MethodAttributeVisitor
import com.github.netomi.bat.classfile.constant.Constant
import com.github.netomi.bat.classfile.constant.visitor.ConstantVisitor
import java.io.DataInput
import java.io.DataOutput
import java.io.IOException

/**
 * A class representing an AnnotationDefault attribute in a class file.
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se18/html/jvms-4.html#jvms-4.7.22">AnnotationDefault Attribute</a>
 */
data class AnnotationDefaultAttribute
    private constructor(override val attributeNameIndex:  Int,
                         // Pick an arbitrary empty element value as default, will be modified during read
                         private var _elementValue:       ElementValue = ClassElementValue.empty())
    : Attribute(attributeNameIndex), AttachedToMethod {

    override val type: AttributeType
        get() = AttributeType.ANNOTATION_DEFAULT

    val elementValue: ElementValue
        get() = _elementValue

    override val dataSize: Int
        get() = TODO("implement")

    @Throws(IOException::class)
    override fun readAttributeData(input: DataInput, classFile: ClassFile) {
        val length = input.readInt()
        _elementValue = ElementValue.read(input)
    }

    @Throws(IOException::class)
    override fun writeAttributeData(output: DataOutput) {
        output.write(dataSize)
        elementValue.write(output)
    }

    override fun accept(classFile: ClassFile, method: Method, visitor: MethodAttributeVisitor) {
        visitor.visitAnnotationDefaultAttribute(classFile, method, this)
    }

    fun elementValueAccept(classFile: ClassFile, visitor: ElementValueVisitor) {
        elementValue.accept(classFile, visitor)
    }

    companion object {
        internal fun empty(attributeNameIndex: Int): AnnotationDefaultAttribute {
            return AnnotationDefaultAttribute(attributeNameIndex)
        }
    }
}