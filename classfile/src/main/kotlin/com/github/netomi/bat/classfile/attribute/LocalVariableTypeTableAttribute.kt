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
import com.github.netomi.bat.classfile.constant.visitor.ConstantVisitor
import com.github.netomi.bat.classfile.constant.visitor.PropertyAccessor
import com.github.netomi.bat.classfile.constant.visitor.ReferencedConstantVisitor
import com.github.netomi.bat.classfile.io.*
import com.github.netomi.bat.classfile.io.ClassDataInput
import com.github.netomi.bat.classfile.io.ClassDataOutput
import com.github.netomi.bat.util.mutableListOfCapacity

/**
 * A class representing a LocalVariableTypeTable attribute in a class file.
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se18/html/jvms-4.html#jvms-4.7.14">LocalVariableTypeTable Attribute</a>
 */
data class LocalVariableTypeTableAttribute
    private constructor(override var attributeNameIndex:     Int,
                         private var localVariableTypeTable: MutableList<LocalVariableTypeEntry> = mutableListOfCapacity(0))
    : Attribute(attributeNameIndex), AttachedToCodeAttribute, Sequence<LocalVariableTypeEntry> {

    override val type: AttributeType
        get() = AttributeType.LOCAL_VARIABLE_TYPE_TABLE

    override val dataSize: Int
        get() = localVariableTypeTable.contentSize()

    val size: Int
        get() = localVariableTypeTable.size

    fun add(entry: LocalVariableTypeEntry) {
        localVariableTypeTable.add(entry)
    }

    operator fun get(index: Int): LocalVariableTypeEntry {
        return localVariableTypeTable[index]
    }

    override fun iterator(): Iterator<LocalVariableTypeEntry> {
        return localVariableTypeTable.iterator()
    }

    override fun readAttributeData(input: ClassDataInput, length: Int) {
        localVariableTypeTable = input.readContentList(LocalVariableTypeEntry::read)
    }

    override fun writeAttributeData(output: ClassDataOutput) {
        output.writeContentList(localVariableTypeTable)
    }

    override fun accept(classFile: ClassFile, method: Method, code: CodeAttribute, visitor: CodeAttributeVisitor) {
        visitor.visitLocalVariableTypeTable(classFile, method, code, this)
    }

    override fun referencedConstantsAccept(classFile: ClassFile, visitor: ReferencedConstantVisitor) {
        super.referencedConstantsAccept(classFile, visitor)
        for (entry in localVariableTypeTable) {
            entry.referencedConstantsAccept(classFile, visitor)
        }
    }

    companion object {
        internal fun empty(attributeNameIndex: Int): LocalVariableTypeTableAttribute {
            return LocalVariableTypeTableAttribute(attributeNameIndex)
        }
    }
}

data class LocalVariableTypeEntry
    private constructor(private var _startPC:        Int = -1,
                        private var _length:         Int = -1,
                        private var _nameIndex:      Int = -1,
                        private var _signatureIndex: Int = -1,
                        private var _variableIndex:  Int = -1): ClassFileContent() {

    override val contentSize: Int
        get() = 10

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

    private fun read(input: ClassDataInput) {
        _startPC        = input.readUnsignedShort()
        _length         = input.readUnsignedShort()
        _nameIndex      = input.readUnsignedShort()
        _signatureIndex = input.readUnsignedShort()
        _variableIndex  = input.readUnsignedShort()
    }

    override fun write(output: ClassDataOutput) {
        output.writeShort(startPC)
        output.writeShort(length)
        output.writeShort(nameIndex)
        output.writeShort(signatureIndex)
        output.writeShort(variableIndex)
    }

    fun signatureConstantAccept(classFile: ClassFile, visitor: ConstantVisitor) {
        classFile.constantAccept(signatureIndex, visitor)
    }

    fun referencedConstantsAccept(classFile: ClassFile, visitor: ReferencedConstantVisitor) {
        visitor.visitUtf8Constant(classFile, this, PropertyAccessor(::_nameIndex))
        visitor.visitUtf8Constant(classFile, this, PropertyAccessor(::_signatureIndex))
    }

    companion object {
        internal fun read(input: ClassDataInput): LocalVariableTypeEntry {
            val element = LocalVariableTypeEntry()
            element.read(input)
            return element
        }

        fun of(startPC: Int, length: Int, nameIndex: Int, signatureIndex: Int, variable: Int): LocalVariableTypeEntry {
            return LocalVariableTypeEntry(startPC, length, nameIndex, signatureIndex, variable)
        }
    }
}