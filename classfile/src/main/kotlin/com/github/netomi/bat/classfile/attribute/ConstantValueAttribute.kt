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
import com.github.netomi.bat.classfile.attribute.visitor.FieldAttributeVisitor
import com.github.netomi.bat.classfile.constant.visitor.ConstantVisitor
import com.github.netomi.bat.classfile.constant.visitor.PropertyAccessor
import com.github.netomi.bat.classfile.constant.visitor.ReferencedConstantAdapter
import com.github.netomi.bat.classfile.constant.visitor.ReferencedConstantVisitor
import com.github.netomi.bat.classfile.io.ClassDataInput
import com.github.netomi.bat.classfile.io.ClassDataOutput
import java.io.IOException

/**
 * A class representing a ConstantValue attribute in a class file.
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se17/html/jvms-4.html#jvms-4.7.2">ConstantValue Attribute</a>
 */
data class ConstantValueAttribute
    private constructor(override var attributeNameIndex:  Int,
                         private var _constantValueIndex: Int = -1)
    : Attribute(attributeNameIndex), AttachedToField {

    override val type: AttributeType
        get() = AttributeType.CONSTANT_VALUE

    override val dataSize: Int
        get() = ATTRIBUTE_LENGTH

    val constantValueIndex: Int
        get() = _constantValueIndex

    @Throws(IOException::class)
    override fun readAttributeData(input: ClassDataInput, length: Int) {
        assert(length == ATTRIBUTE_LENGTH)
        _constantValueIndex = input.readUnsignedShort()
    }

    @Throws(IOException::class)
    override fun writeAttributeData(output: ClassDataOutput) {
        output.writeShort(constantValueIndex)
    }

    override fun accept(classFile: ClassFile, field: Field, visitor: FieldAttributeVisitor) {
        visitor.visitConstantValue(classFile, field, this)
    }

    fun constantValueAccept(classFile: ClassFile, visitor: ConstantVisitor) {
        classFile.constantAccept(constantValueIndex, visitor)
    }

    override fun referencedConstantsAccept(classFile: ClassFile, visitor: ReferencedConstantVisitor) {
        super.referencedConstantsAccept(classFile, visitor)
        constantValueAccept(classFile,
                            ReferencedConstantAdapter(this, PropertyAccessor(::_constantValueIndex), visitor))
    }

    companion object {
        private const val ATTRIBUTE_LENGTH = 2

        internal fun empty(attributeNameIndex: Int): ConstantValueAttribute {
            return ConstantValueAttribute(attributeNameIndex)
        }
    }
}