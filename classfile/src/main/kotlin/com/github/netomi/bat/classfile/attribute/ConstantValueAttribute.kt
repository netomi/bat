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
import com.github.netomi.bat.classfile.attribute.visitor.AttributeVisitor
import com.github.netomi.bat.classfile.constant.Constant
import com.github.netomi.bat.classfile.constant.visitor.ConstantVisitor
import java.io.DataInput
import java.io.DataOutput
import java.io.IOException

/**
 * A class representing a ConstantValue attribute in a class file.
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se17/html/jvms-4.html#jvms-4.7.2">ConstantValue Attribute</a>
 */
data class ConstantValueAttribute internal constructor(override val attributeNameIndex:  Int,
                                                        private var _constantValueIndex: Int = -1) : Attribute(attributeNameIndex) {

    override val type: AttributeType
        get() = AttributeType.CONSTANT_VALUE

    val constantValueIndex: Int
        get() = _constantValueIndex

    fun getConstantValue(classFile: ClassFile): Constant {
        return classFile.getConstant(constantValueIndex)
    }

    @Throws(IOException::class)
    override fun readAttributeData(input: DataInput) {
        val length = input.readInt()
        assert(length == ATTRIBUTE_LENGTH)
        _constantValueIndex = input.readUnsignedShort()
    }

    @Throws(IOException::class)
    override fun writeAttributeData(output: DataOutput) {
        output.write(ATTRIBUTE_LENGTH)
        output.writeShort(constantValueIndex)
    }

    override fun accept(classFile: ClassFile, visitor: AttributeVisitor) {
        visitor.visitConstantValueAttribute(classFile, this)
    }

    fun constantValueAccept(classFile: ClassFile, visitor: ConstantVisitor) {
        classFile.getConstant(constantValueIndex).accept(classFile, visitor)
    }

    companion object {
        private const val ATTRIBUTE_LENGTH = 2

        internal fun empty(attributeNameIndex: Int): ConstantValueAttribute {
            return ConstantValueAttribute(attributeNameIndex)
        }
    }
}