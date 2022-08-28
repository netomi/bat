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

/**
 * A class representing a LocalVariableTypeTable attribute in a class file.
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se18/html/jvms-4.html#jvms-4.7.14">LocalVariableTypeTable Attribute</a>
 */
data class LocalVariableTypeTableAttribute
    private constructor(override val attributeNameIndex:     Int,
                         private var localVariableTypeTable: MutableList<LocalVariableTypeElement> = mutableListOfCapacity(0))
    : Attribute(attributeNameIndex), AttachedToCodeAttribute, Sequence<LocalVariableTypeElement> {

    override val type: AttributeType
        get() = AttributeType.LOCAL_VARIABLE_TABLE

    override val dataSize: Int
        get() = 2 + size * LocalVariableTypeElement.DATA_SIZE

    val size: Int
        get() = localVariableTypeTable.size

    operator fun get(index: Int): LocalVariableTypeElement {
        return localVariableTypeTable[index]
    }

    override fun iterator(): Iterator<LocalVariableTypeElement> {
        return localVariableTypeTable.iterator()
    }

    override fun readAttributeData(input: DataInput, classFile: ClassFile) {
        @Suppress("UNUSED_VARIABLE")
        val length = input.readInt()
        val localVariableTableLength = input.readUnsignedShort()
        localVariableTypeTable = mutableListOfCapacity(localVariableTableLength)
        for (i in 0 until localVariableTableLength) {
            localVariableTypeTable.add(LocalVariableTypeElement.read(input))
        }
    }

    override fun writeAttributeData(output: DataOutput) {
        output.writeInt(dataSize)
        output.writeShort(localVariableTypeTable.size)
        for (element in localVariableTypeTable) {
            element.write(output)
        }
    }

    override fun accept(classFile: ClassFile, method: Method, code: CodeAttribute, visitor: CodeAttributeVisitor) {
        visitor.visitLocalVariableTypeTableAttribute(classFile, method, code, this)
    }

    companion object {
        internal fun empty(attributeNameIndex: Int): LocalVariableTypeTableAttribute {
            return LocalVariableTypeTableAttribute(attributeNameIndex)
        }
    }
}

data class LocalVariableTypeElement
    private constructor(private var _startPC:        Int = -1,
                        private var _length:         Int = -1,
                        private var _nameIndex:      Int = -1,
                        private var _signatureIndex: Int = -1,
                        private var _variableIndex:  Int = -1) {

    val startPC: Int
        get() = _startPC

    val length: Int
        get() = _length

    val nameIndex: Int
        get() = _nameIndex

    val signatureIndex: Int
        get() = _signatureIndex

    val variableIndex: Int
        get() = _variableIndex

    fun getName(classFile: ClassFile): String {
        return classFile.getString(nameIndex)
    }

    fun getSignature(classFile: ClassFile): String {
        return classFile.getString(signatureIndex)
    }

    private fun read(input: DataInput) {
        _startPC        = input.readUnsignedShort()
        _length         = input.readUnsignedShort()
        _nameIndex      = input.readUnsignedShort()
        _signatureIndex = input.readUnsignedShort()
        _variableIndex  = input.readUnsignedShort()
    }

    internal fun write(output: DataOutput) {
        output.writeShort(startPC)
        output.writeShort(length)
        output.writeShort(nameIndex)
        output.writeShort(signatureIndex)
        output.writeShort(variableIndex)
    }

    companion object {
        internal const val DATA_SIZE = 10

        internal fun read(input: DataInput): LocalVariableTypeElement {
            val element = LocalVariableTypeElement()
            element.read(input)
            return element
        }
    }
}