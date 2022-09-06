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
import com.github.netomi.bat.util.mutableListOfCapacity

/**
 * A class representing a MethodParameters attribute in a class file.
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se17/html/jvms-4.html#jvms-4.7.24">MethodParameters Attribute</a>
 */
data class MethodParametersAttribute
    private constructor(override val attributeNameIndex: Int,
                         private var parameters:         MutableList<ParameterElement> = mutableListOfCapacity(0)
    ): Attribute(attributeNameIndex), AttachedToMethod, Sequence<ParameterElement> {

    override val type: AttributeType
        get() = AttributeType.METHOD_PARAMETERS

    override val dataSize: Int
        get() = 1 + size * ParameterElement.DATA_SIZE

    val size: Int
        get() = parameters.size

    operator fun get(index: Int): ParameterElement {
        return parameters[index]
    }

    override fun iterator(): Iterator<ParameterElement> {
        return parameters.iterator()
    }

    override fun readAttributeData(input: ClassDataInput) {
        @Suppress("UNUSED_VARIABLE")
        val length = input.readInt()
        parameters = input.readContentList(ParameterElement.Companion::read)
    }

    override fun writeAttributeData(output: ClassDataOutput) {
        output.writeInt(dataSize)
        output.writeContentList(parameters)
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
                                                private var _accessFlags: Int =  0): ClassFileContent() {

    override val dataSize: Int
        get() = DATA_SIZE

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
        internal const val DATA_SIZE = 4

        internal fun read(input: ClassDataInput): ParameterElement {
            val element = ParameterElement()
            element.read(input)
            return element
        }
    }
}