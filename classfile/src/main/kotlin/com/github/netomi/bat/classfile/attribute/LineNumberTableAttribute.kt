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
 * A class representing a LineNumberTable attribute in a class file.
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se17/html/jvms-4.html#jvms-4.7.12">LineNumberTable Attribute</a>
 */
data class LineNumberTableAttribute
    private constructor(override val attributeNameIndex: Int,
                         private var _lineNumberTable:   MutableList<LineNumberElement> = mutableListOfCapacity(0))
    : Attribute(attributeNameIndex), AttachedToCodeAttribute {

    override val type: AttributeType
        get() = AttributeType.LINE_NUMBER_TABLE

    val lineNumberTable: List<LineNumberElement>
        get() = _lineNumberTable

    override val dataSize: Int
        get() = 2 + lineNumberTable.size * LineNumberElement.DATA_SIZE

    override fun readAttributeData(input: DataInput, classFile: ClassFile) {
        @Suppress("UNUSED_VARIABLE")
        val length = input.readInt()
        val lineNumberTableLength = input.readUnsignedShort()
        _lineNumberTable = mutableListOfCapacity(lineNumberTableLength)
        for (i in 0 until lineNumberTableLength) {
            _lineNumberTable.add(LineNumberElement.read(input))
        }
    }

    override fun writeAttributeData(output: DataOutput) {
        output.writeInt(dataSize)
        output.writeShort(lineNumberTable.size)
        for (element in lineNumberTable) {
            element.write(output)
        }
    }

    override fun accept(classFile: ClassFile, method: Method, code: CodeAttribute, visitor: CodeAttributeVisitor) {
        visitor.visitLineNumberTableAttribute(classFile, method, code, this)
    }

    companion object {
        internal fun empty(attributeNameIndex: Int): LineNumberTableAttribute {
            return LineNumberTableAttribute(attributeNameIndex)
        }
    }
}

data class LineNumberElement private constructor(private var _startPC:    Int = -1,
                                                 private var _lineNumber: Int = -1) {

    val startPC
        get() = _startPC

    val lineNumber
        get() = _lineNumber

    private fun read(input: DataInput) {
        _startPC    = input.readUnsignedShort()
        _lineNumber = input.readUnsignedShort()
    }

    internal fun write(output: DataOutput) {
        output.writeShort(startPC)
        output.writeShort(lineNumber)
    }

    companion object {
        internal const val DATA_SIZE = 4

        internal fun read(input: DataInput): LineNumberElement {
            val element = LineNumberElement()
            element.read(input)
            return element
        }
    }
}