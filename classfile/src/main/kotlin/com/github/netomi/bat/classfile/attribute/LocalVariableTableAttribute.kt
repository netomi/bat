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
import com.github.netomi.bat.classfile.attribute.visitor.CodeAttributeVisitor
import com.github.netomi.bat.util.mutableListOfCapacity
import java.io.DataInput
import java.io.DataOutput

data class LocalVariableTableAttribute
    private constructor(override val attributeNameIndex: Int,
                         private var _localVariableTable:   MutableList<LocalVariableElement> = mutableListOfCapacity(0))
    : Attribute(attributeNameIndex), AttachedToCodeAttribute {

    override val type: AttributeType
        get() = AttributeType.LOCAL_VARIABLE_TABLE

    override val dataSize: Int
        get() = 2 + localVariableTable.size * LocalVariableElement.DATA_SIZE

    val localVariableTable: List<LocalVariableElement>
        get() = _localVariableTable

    override fun readAttributeData(input: DataInput, classFile: ClassFile) {
        val length = input.readInt()
        val localVariavbleTableLength = input.readUnsignedShort()
        _localVariableTable = mutableListOfCapacity(localVariavbleTableLength)
        for (i in 0 until localVariavbleTableLength) {
            _localVariableTable.add(LocalVariableElement.read(input))
        }
    }

    override fun writeAttributeData(output: DataOutput) {
        output.writeInt(dataSize)
        output.writeShort(localVariableTable.size)
        for (element in localVariableTable) {
            element.write(output)
        }
    }

    override fun accept(classFile: ClassFile, method: Method, code: CodeAttribute, visitor: CodeAttributeVisitor) {
        visitor.visitLocalVariableTableAttribute(classFile, method, code, this)
    }

    companion object {
        internal fun empty(attributeNameIndex: Int): LocalVariableTableAttribute {
            return LocalVariableTableAttribute(attributeNameIndex)
        }
    }
}

data class LocalVariableElement
    private constructor(private var _startPC:         Int = -1,
                        private var _length:          Int = -1,
                        private var _nameIndex:       Int = -1,
                        private var _descriptorIndex: Int = -1,
                        private var _variableIndex:   Int = -1) {

    val startPC: Int
        get() = _startPC

    val length: Int
        get() = _length

    val nameIndex: Int
        get() = _nameIndex

    val descriptorIndex: Int
        get() = _descriptorIndex

    val variableIndex: Int
        get() = _variableIndex

    fun getName(classFile: ClassFile): String {
        return classFile.getString(nameIndex)
    }

    fun getDescriptor(classFile: ClassFile): String {
        return classFile.getString(descriptorIndex)
    }

    private fun read(input: DataInput) {
        _startPC         = input.readUnsignedShort()
        _length          = input.readUnsignedShort()
        _nameIndex       = input.readUnsignedShort()
        _descriptorIndex = input.readUnsignedShort()
        _variableIndex   = input.readUnsignedShort()
    }

    internal fun write(output: DataOutput) {
        output.writeShort(startPC)
        output.writeShort(length)
        output.writeShort(nameIndex)
        output.writeShort(descriptorIndex)
        output.writeShort(variableIndex)
    }

    companion object {
        internal const val DATA_SIZE = 10

        internal fun read(input: DataInput): LocalVariableElement {
            val element = LocalVariableElement()
            element.read(input)
            return element
        }
    }
}