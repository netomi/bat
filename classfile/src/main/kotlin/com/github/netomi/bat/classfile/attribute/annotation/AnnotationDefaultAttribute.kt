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
import com.github.netomi.bat.classfile.Method
import com.github.netomi.bat.classfile.attribute.AttachedToMethod
import com.github.netomi.bat.classfile.attribute.Attribute
import com.github.netomi.bat.classfile.attribute.AttributeType
import com.github.netomi.bat.classfile.attribute.annotation.visitor.ElementValueVisitor
import com.github.netomi.bat.classfile.attribute.visitor.MethodAttributeVisitor
import com.github.netomi.bat.classfile.constant.visitor.ReferencedConstantVisitor
import com.github.netomi.bat.classfile.io.ClassDataInput
import com.github.netomi.bat.classfile.io.ClassDataOutput
import java.io.IOException

/**
 * A class representing an AnnotationDefault attribute in a class file.
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se18/html/jvms-4.html#jvms-4.7.22">AnnotationDefault Attribute</a>
 */
data class AnnotationDefaultAttribute
    private constructor(override var attributeNameIndex: Int,
                                 var elementValue:       ElementValue = ElementValue.empty())
    : Attribute(attributeNameIndex), AttachedToMethod {

    override val type: AttributeType
        get() = AttributeType.ANNOTATION_DEFAULT

    override val dataSize: Int
        get() = elementValue.contentSize

    @Throws(IOException::class)
    override fun readAttributeData(input: ClassDataInput, length: Int) {
        elementValue = ElementValue.read(input)
    }

    @Throws(IOException::class)
    override fun writeAttributeData(output: ClassDataOutput) {
        elementValue.write(output)
    }

    override fun accept(classFile: ClassFile, method: Method, visitor: MethodAttributeVisitor) {
        visitor.visitAnnotationDefault(classFile, method, this)
    }

    fun elementValueAccept(classFile: ClassFile, visitor: ElementValueVisitor) {
        elementValue.accept(classFile, visitor)
    }

    override fun referencedConstantsAccept(classFile: ClassFile, visitor: ReferencedConstantVisitor) {
        super.referencedConstantsAccept(classFile, visitor)
        elementValue.referencedConstantsAccept(classFile, visitor)
    }

    companion object {
        internal fun empty(attributeNameIndex: Int): AnnotationDefaultAttribute {
            return AnnotationDefaultAttribute(attributeNameIndex)
        }
    }
}