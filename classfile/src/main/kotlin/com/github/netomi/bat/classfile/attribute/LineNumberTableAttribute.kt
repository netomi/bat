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
import com.github.netomi.bat.classfile.io.ClassDataInput
import com.github.netomi.bat.classfile.io.ClassDataOutput
import com.github.netomi.bat.classfile.io.ClassFileContent
import com.github.netomi.bat.classfile.io.contentSize
import com.github.netomi.bat.util.mutableListOfCapacity

/**
 * A class representing a LineNumberTable attribute in a class file.
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se17/html/jvms-4.html#jvms-4.7.12">LineNumberTable Attribute</a>
 */
data class LineNumberTableAttribute
    private constructor(override val attributeNameIndex: Int,
                         private var lineNumberTable:    MutableList<LineNumberElement> = mutableListOfCapacity(0))
    : Attribute(attributeNameIndex), AttachedToCodeAttribute, Sequence<LineNumberElement> {

    override val type: AttributeType
        get() = AttributeType.LINE_NUMBER_TABLE

    override val dataSize: Int
        get() = lineNumberTable.contentSize()

    val size: Int
        get() = lineNumberTable.size

    operator fun get(index: Int): LineNumberElement {
        return lineNumberTable[index]
    }

    override fun iterator(): Iterator<LineNumberElement> {
        return lineNumberTable.iterator()
    }

    override fun readAttributeData(input: ClassDataInput, length: Int) {
        lineNumberTable = input.readContentList(LineNumberElement.Companion::read)
    }

    override fun writeAttributeData(output: ClassDataOutput) {
        output.writeContentList(lineNumberTable)
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
                                                 private var _lineNumber: Int = -1): ClassFileContent() {

    override val contentSize: Int
        get() = 4

    val startPC
        get() = _startPC

    val lineNumber
        get() = _lineNumber

    private fun read(input: ClassDataInput) {
        _startPC    = input.readUnsignedShort()
        _lineNumber = input.readUnsignedShort()
    }

    override fun write(output: ClassDataOutput) {
        output.writeShort(startPC)
        output.writeShort(lineNumber)
    }

    companion object {
        internal fun read(input: ClassDataInput): LineNumberElement {
            val element = LineNumberElement()
            element.read(input)
            return element
        }
    }
}