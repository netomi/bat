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
import com.github.netomi.bat.classfile.io.ClassDataInput
import com.github.netomi.bat.classfile.io.ClassDataOutput
import com.github.netomi.bat.classfile.io.ClassFileContent
import com.github.netomi.bat.classfile.io.contentSize
import com.github.netomi.bat.util.mutableListOfCapacity

/**
 * A class representing a MethodParameters attribute in a class file.
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se17/html/jvms-4.html#jvms-4.7.24">MethodParameters Attribute</a>
 */
data class MethodParametersAttribute
    private constructor(override var attributeNameIndex: Int,
                         private var parameters:         MutableList<MethodParameterEntry> = mutableListOfCapacity(0)
    ): Attribute(attributeNameIndex), AttachedToMethod, Sequence<MethodParameterEntry> {

    override val type: AttributeType
        get() = AttributeType.METHOD_PARAMETERS

    override val dataSize: Int
        get() = parameters.contentSize()

    val size: Int
        get() = parameters.size

    operator fun get(index: Int): MethodParameterEntry {
        return parameters[index]
    }

    override fun iterator(): Iterator<MethodParameterEntry> {
        return parameters.iterator()
    }

    override fun readAttributeData(input: ClassDataInput, length: Int) {
        val parametersCount = input.readUnsignedByte()
        parameters = mutableListOfCapacity(parametersCount)
        for (i in 0 until parametersCount) {
            parameters.add(MethodParameterEntry.read(input))
        }
    }

    override fun writeAttributeData(output: ClassDataOutput) {
        output.writeByte(parameters.size)
        for (element in parameters) {
            element.write(output)
        }
    }

    override fun accept(classFile: ClassFile, method: Method, visitor: MethodAttributeVisitor) {
        visitor.visitMethodParameters(classFile, method, this)
    }

    companion object {
        internal fun empty(attributeNameIndex: Int): MethodParametersAttribute {
            return MethodParametersAttribute(attributeNameIndex)
        }
    }
}

data class MethodParameterEntry private constructor(private var _nameIndex:   Int = -1,
                                                    private var _accessFlags: Int =  0): ClassFileContent() {

    override val contentSize: Int
        get() = 4

    val nameIndex: Int
        get() = _nameIndex

    val accessFlags: Int
        get() = _accessFlags

    fun getName(classFile: ClassFile): String {
        return classFile.getString(nameIndex)
    }

    private fun read(input: ClassDataInput) {
        _nameIndex   = input.readUnsignedShort()
        _accessFlags = input.readUnsignedShort()
    }

    override fun write(output: ClassDataOutput) {
        output.writeShort(nameIndex)
        output.writeShort(accessFlags)
    }

    companion object {
        internal fun read(input: ClassDataInput): MethodParameterEntry {
            val element = MethodParameterEntry()
            element.read(input)
            return element
        }
    }
}