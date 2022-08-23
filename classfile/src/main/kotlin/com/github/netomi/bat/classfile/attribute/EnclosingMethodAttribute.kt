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
import java.io.DataInput
import java.io.DataOutput
import java.io.IOException

/**
 * A class representing a EnclosingMethod attribute in a class file.
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se17/html/jvms-4.html#jvms-4.7.7">Enclosing Method Attribute</a>
 */
data class EnclosingMethodAttribute internal constructor(override val attributeNameIndex: Int,
                                                                  var classIndex:         Int = -1,
                                                                  var methodIndex:        Int = -1) : Attribute(attributeNameIndex) {

    override val type: AttributeType
        get() = AttributeType.ENCLOSING_METHOD

    fun getClassName(classFile: ClassFile): String {
        return classFile.getClassName(classIndex)
    }

    fun getMethodName(classFile: ClassFile): String {
        return classFile.getNameAndType(methodIndex).getMemberName(classFile);
    }

    fun getMethodDescriptor(classFile: ClassFile): String {
        return classFile.getNameAndType(methodIndex).getDescriptor(classFile)
    }

    @Throws(IOException::class)
    override fun readAttributeData(input: DataInput) {
        val length = input.readInt()
        assert(length == ATTRIBUTE_LENGTH)
        classIndex  = input.readUnsignedShort()
        methodIndex = input.readUnsignedShort()
    }

    @Throws(IOException::class)
    override fun writeAttributeData(output: DataOutput) {
        output.writeInt(ATTRIBUTE_LENGTH)
        output.writeShort(classIndex)
        output.writeShort(methodIndex)
    }

    override fun accept(classFile: ClassFile, visitor: AttributeVisitor) {
        visitor.visitEnclosingMethodAttribute(classFile, this)
    }

    companion object {
        private const val ATTRIBUTE_LENGTH = 4

        internal fun empty(attributeNameIndex: Int): EnclosingMethodAttribute {
            return EnclosingMethodAttribute(attributeNameIndex)
        }
    }
}