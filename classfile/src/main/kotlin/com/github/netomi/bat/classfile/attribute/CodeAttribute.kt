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
import com.github.netomi.bat.classfile.attribute.visitor.MethodAttributeVisitor
import com.github.netomi.bat.util.mutableListOfCapacity
import java.io.DataInput
import java.io.DataOutput
import java.io.IOException
import java.util.*

/**
 * A class representing a Code attribute in a class file.
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se17/html/jvms-4.html#jvms-4.7.3">Code Attribute</a>
 */
data class CodeAttribute
    private constructor(override val attributeNameIndex: Int,
                         private var _maxStack:          Int                           = 0,
                         private var _maxLocals:         Int                           = 0,
                         private var _code:              ByteArray                     = ByteArray(0),
                         private var _exceptionTable:    MutableList<ExceptionElement> = mutableListOfCapacity(0),
                         private var _attributes:        MutableList<Attribute>        = mutableListOfCapacity(0))
    : Attribute(attributeNameIndex), AttachedToMethod {

    override val type: AttributeType
        get() = AttributeType.CODE

    val maxStack: Int
        get() = _maxStack

    val maxLocals: Int
        get() = _maxLocals

    val codeLength: Int
        get() = code.size

    val code: ByteArray
        get() = _code

    val exceptionTable: List<ExceptionElement>
        get() = _exceptionTable

    val attributes: List<Attribute>
        get() = _attributes

    override val dataSize: Int
        get() = TODO("Not yet implemented")

    @Throws(IOException::class)
    override fun readAttributeData(input: DataInput, classFile: ClassFile) {
        val length = input.readInt()
        _maxStack  = input.readUnsignedShort()
        _maxLocals = input.readUnsignedShort()

        val codeLength = input.readInt()
        _code = ByteArray(codeLength)
        input.readFully(_code)

        val exceptionTableLength = input.readUnsignedShort()
        _exceptionTable = mutableListOfCapacity(exceptionTableLength)
        for (i in 0 until exceptionTableLength) {
            _exceptionTable.add(ExceptionElement.read(input))
        }

        val attributesCount = input.readUnsignedShort()
        _attributes = mutableListOfCapacity(attributesCount)
        for (i in 0 until attributesCount) {
            _attributes.add(readAttribute(input, classFile))
        }
    }

    @Throws(IOException::class)
    override fun writeAttributeData(output: DataOutput) {
        output.writeInt(dataSize)

        output.writeShort(maxStack)
        output.writeShort(maxLocals)

        output.writeInt(codeLength)
        output.write(code)

        output.writeShort(exceptionTable.size)
        for (exceptionElement in exceptionTable) {
            exceptionElement.write(output)
        }

        output.writeShort(attributes.size)
        for (attribute in attributes) {
            attribute.writeAttribute(output)
        }
    }

    override fun accept(classFile: ClassFile, method: Method, visitor: MethodAttributeVisitor) {
        visitor.visitCodeAttribute(classFile, method, this)
    }

    fun attributesAccept(classFile: ClassFile, method: Method, visitor: CodeAttributeVisitor) {
        for (attribute in attributes.filterIsInstance(AttachedToCodeAttribute::class.java)) {
            attribute.accept(classFile, method, this, visitor)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CodeAttribute) return false

        return attributeNameIndex == other.attributeNameIndex &&
               maxStack           == other.maxStack           &&
               maxLocals          == other.maxLocals          &&
               exceptionTable     == other.exceptionTable     &&
               attributes         == other.attributes         &&
               code.contentEquals(other.code)
    }

    override fun hashCode(): Int {
        return Objects.hash(attributeNameIndex, maxStack, maxLocals, code.contentHashCode(), exceptionTable, attributes)
    }

    companion object {
        internal fun empty(attributeNameIndex: Int): CodeAttribute {
            return CodeAttribute(attributeNameIndex)
        }
    }
}

data class ExceptionElement private constructor(private var _startPC:   Int = -1,
                                                private var _endPC:     Int = -1,
                                                private var _handlerPC: Int = -1,
                                                private var _catchType: Int = -1) {

    val startPC: Int
        get() = _startPC

    val endPC: Int
        get() = _endPC

    val handlerPC: Int
        get() = _handlerPC

    val catchType: Int
        get() = _catchType

    private fun read(input: DataInput) {
        _startPC   = input.readUnsignedShort()
        _endPC     = input.readUnsignedShort()
        _handlerPC = input.readUnsignedShort()
        _catchType = input.readUnsignedShort()
    }

    internal fun write(output: DataOutput) {
        output.writeShort(startPC)
        output.writeShort(endPC)
        output.writeShort(handlerPC)
        output.writeShort(catchType)
    }

    companion object {
        internal const val SIZE = 8

        internal fun read(input: DataInput): ExceptionElement {
            val element = ExceptionElement()
            element.read(input)
            return element
        }
    }
}