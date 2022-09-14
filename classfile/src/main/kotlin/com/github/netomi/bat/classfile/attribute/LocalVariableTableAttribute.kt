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
import com.github.netomi.bat.classfile.io.*
import com.github.netomi.bat.classfile.io.ClassDataInput
import com.github.netomi.bat.classfile.io.ClassDataOutput
import com.github.netomi.bat.util.mutableListOfCapacity

/**
 * A class representing a LocalVariableTable attribute in a class file.
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se17/html/jvms-4.html#jvms-4.7.13">LocalVariableTable Attribute</a>
 */
data class LocalVariableTableAttribute
    private constructor(override val attributeNameIndex: Int,
                         private var localVariableTable: MutableList<LocalVariableEntry> = mutableListOfCapacity(0))
    : Attribute(attributeNameIndex), AttachedToCodeAttribute, Sequence<LocalVariableEntry> {

    override val type: AttributeType
        get() = AttributeType.LOCAL_VARIABLE_TABLE

    override val dataSize: Int
        get() = localVariableTable.contentSize()

    val size: Int
        get() = localVariableTable.size

    operator fun get(index: Int): LocalVariableEntry {
        return localVariableTable[index]
    }

    override fun iterator(): Iterator<LocalVariableEntry> {
        return localVariableTable.iterator()
    }

    override fun readAttributeData(input: ClassDataInput, length: Int) {
        localVariableTable = input.readContentList(LocalVariableEntry::read)
    }

    override fun writeAttributeData(output: ClassDataOutput) {
        output.writeContentList(localVariableTable)
    }

    override fun accept(classFile: ClassFile, method: Method, code: CodeAttribute, visitor: CodeAttributeVisitor) {
        visitor.visitLocalVariableTable(classFile, method, code, this)
    }

    companion object {
        internal fun empty(attributeNameIndex: Int): LocalVariableTableAttribute {
            return LocalVariableTableAttribute(attributeNameIndex)
        }
    }
}

data class LocalVariableEntry
    private constructor(private var _startPC:         Int = -1,
                        private var _length:          Int = -1,
                        private var _nameIndex:       Int = -1,
                        private var _descriptorIndex: Int = -1,
                        private var _variableIndex:   Int = -1): ClassFileContent() {

    override val contentSize: Int
        get() = 10

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

    private fun read(input: ClassDataInput) {
        _startPC         = input.readUnsignedShort()
        _length          = input.readUnsignedShort()
        _nameIndex       = input.readUnsignedShort()
        _descriptorIndex = input.readUnsignedShort()
        _variableIndex   = input.readUnsignedShort()
    }

    override fun write(output: ClassDataOutput) {
        output.writeShort(startPC)
        output.writeShort(length)
        output.writeShort(nameIndex)
        output.writeShort(descriptorIndex)
        output.writeShort(variableIndex)
    }

    companion object {
        internal fun read(input: ClassDataInput): LocalVariableEntry {
            val element = LocalVariableEntry()
            element.read(input)
            return element
        }
    }
}