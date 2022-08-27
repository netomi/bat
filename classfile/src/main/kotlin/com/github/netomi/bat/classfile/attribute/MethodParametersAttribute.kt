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

package com.github.netomi.bat.classfile.attribute

import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.Method
import com.github.netomi.bat.classfile.attribute.visitor.MethodAttributeVisitor
import com.github.netomi.bat.util.mutableListOfCapacity
import java.io.DataInput
import java.io.DataOutput

/**
 * A class representing a MethodParameters attribute in a class file.
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se17/html/jvms-4.html#jvms-4.7.24">MethodParameters Attribute</a>
 */
data class MethodParametersAttribute
    private constructor(override val attributeNameIndex: Int,
                         private var _parameters:        MutableList<ParameterElement> = mutableListOfCapacity(0)
    ): Attribute(attributeNameIndex), AttachedToMethod {

    override val type: AttributeType
        get() = AttributeType.METHOD_PARAMETERS

    override val dataSize: Int
        get() = 1 + parameters.size * ParameterElement.DATA_SIZE

    val parameters: List<ParameterElement>
        get() = _parameters

    override fun readAttributeData(input: DataInput, classFile: ClassFile) {
        val length = input.readInt()
        val parametersCount = input.readByte().toInt()
        _parameters = mutableListOfCapacity(parametersCount)
        for (i in 0 until parametersCount) {
            _parameters.add(ParameterElement.read(input))
        }
    }

    override fun writeAttributeData(output: DataOutput) {
        output.write(dataSize)
        output.writeByte(parameters.size)
        for (element in parameters) {
            element.write(output)
        }
    }

    override fun accept(classFile: ClassFile, method: Method, visitor: MethodAttributeVisitor) {
        visitor.visitMethodParametersAttribute(classFile, method, this)
    }

    companion object {
        internal fun empty(attributeNameIndex: Int): MethodParametersAttribute {
            return MethodParametersAttribute(attributeNameIndex)
        }
    }
}

data class ParameterElement private constructor(private var _nameIndex:   Int = -1,
                                                private var _accessFlags: Int =  0) {

    val nameIndex: Int
        get() = _nameIndex

    val accessFlags: Int
        get() = _accessFlags

    fun getName(classFile: ClassFile): String {
        return classFile.getString(nameIndex)
    }

    private fun read(input: DataInput) {
        _nameIndex   = input.readUnsignedShort()
        _accessFlags = input.readUnsignedShort()
    }

    internal fun write(output: DataOutput) {
        output.writeShort(nameIndex)
        output.writeShort(accessFlags)
    }

    companion object {
        internal const val DATA_SIZE = 4

        internal fun read(input: DataInput): ParameterElement {
            val element = ParameterElement()
            element.read(input)
            return element
        }
    }
}