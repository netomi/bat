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
import com.github.netomi.bat.classfile.attribute.visitor.ClassAttributeVisitor
import com.github.netomi.bat.classfile.attribute.visitor.FieldAttributeVisitor
import com.github.netomi.bat.classfile.attribute.visitor.MethodAttributeVisitor
import com.github.netomi.bat.classfile.io.ClassDataInput
import com.github.netomi.bat.classfile.io.ClassDataOutput
import java.io.IOException

/**
 * A class representing a Deprecated attribute in a class file.
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se17/html/jvms-4.html#jvms-4.7.15">Deprecated Attribute</a>
 */
data class DeprecatedAttribute private constructor(override val attributeNameIndex: Int)
    : Attribute(attributeNameIndex), AttachedToClass, AttachedToField, AttachedToMethod {

    override val type: AttributeType
        get() = AttributeType.DEPRECATED

    override val dataSize: Int
        get() = ATTRIBUTE_LENGTH

    @Throws(IOException::class)
    override fun readAttributeData(input: ClassDataInput, length: Int) {
        assert(length == ATTRIBUTE_LENGTH)
    }

    @Throws(IOException::class)
    override fun writeAttributeData(output: ClassDataOutput) {}

    override fun accept(classFile: ClassFile, visitor: ClassAttributeVisitor) {
        visitor.visitDeprecated(classFile, this)
    }

    override fun accept(classFile: ClassFile, field: Field, visitor: FieldAttributeVisitor) {
        visitor.visitDeprecated(classFile, field, this)
    }

    override fun accept(classFile: ClassFile, method: Method, visitor: MethodAttributeVisitor) {
        visitor.visitDeprecated(classFile, method, this)
    }

    companion object {
        private const val ATTRIBUTE_LENGTH = 0

        internal fun empty(attributeNameIndex: Int): DeprecatedAttribute {
            return DeprecatedAttribute(attributeNameIndex)
        }
    }
}