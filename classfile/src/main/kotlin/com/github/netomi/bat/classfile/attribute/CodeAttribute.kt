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
import com.github.netomi.bat.classfile.attribute.visitor.ExceptionVisitor
import com.github.netomi.bat.classfile.attribute.visitor.MethodAttributeVisitor
import com.github.netomi.bat.classfile.constant.visitor.PropertyAccessor
import com.github.netomi.bat.classfile.constant.visitor.ReferencedConstantVisitor
import com.github.netomi.bat.classfile.instruction.JvmInstruction
import com.github.netomi.bat.classfile.instruction.editor.OffsetMap
import com.github.netomi.bat.classfile.instruction.visitor.InstructionVisitor
import com.github.netomi.bat.classfile.io.*
import com.github.netomi.bat.classfile.io.ClassDataInput
import com.github.netomi.bat.classfile.io.ClassDataOutput
import com.github.netomi.bat.util.JvmClassName
import com.github.netomi.bat.util.mutableListOfCapacity
import java.io.IOException
import java.util.*

/**
 * A class representing a Code attribute in a class file.
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se17/html/jvms-4.html#jvms-4.7.3">Code Attribute</a>
 */
class CodeAttribute
    private constructor(override var attributeNameIndex: Int,
                         private var _exceptionTable:    MutableList<ExceptionEntry> = mutableListOfCapacity(0),
                        internal var attributeMap:       AttributeMap                = AttributeMap.empty())
    : Attribute(attributeNameIndex), AttachedToMethod {

    override val type: AttributeType
        get() = AttributeType.CODE

    override val dataSize: Int
        get() = 8 + codeLength + exceptionTable.contentSize() + attributeMap.contentSize

    var maxStack: Int = 0
        internal set

    var maxLocals: Int = 0
        internal set

    var code: ByteArray = ByteArray(0)
        internal set

    val codeLength: Int
        get() = code.size

    val exceptionTable: List<ExceptionEntry>
        get() = _exceptionTable

    val attributes: Sequence<Attribute>
        get() = attributeMap

    internal fun addAttribute(attribute: Attribute) {
        require(attribute is AttachedToCodeAttribute) { "trying to add an attribute of type '${attribute.type}' to a code attribute"}
        attributeMap.addAttribute(attribute)
    }

    internal fun setExceptionTable(exceptionList: List<ExceptionEntry>) {
        _exceptionTable = exceptionList.toMutableList()
    }

    @Throws(IOException::class)
    override fun readAttributeData(input: ClassDataInput, length: Int) {
        maxStack  = input.readUnsignedShort()
        maxLocals = input.readUnsignedShort()

        val codeLength = input.readInt()
        code = ByteArray(codeLength)
        input.readFully(code)

        _exceptionTable = input.readContentList(ExceptionEntry::read)
        attributeMap    = input.readAttributes()
    }

    @Throws(IOException::class)
    override fun writeAttributeData(output: ClassDataOutput) {
        output.writeShort(maxStack)
        output.writeShort(maxLocals)

        output.writeInt(codeLength)
        output.write(code)

        output.writeContentList(exceptionTable)
        attributeMap.write(output)
    }

    override fun accept(classFile: ClassFile, method: Method, visitor: MethodAttributeVisitor) {
        visitor.visitCode(classFile, method, this)
    }

    fun attributesAccept(classFile: ClassFile, method: Method, visitor: CodeAttributeVisitor) {
        for (attribute in attributes.filterIsInstance(AttachedToCodeAttribute::class.java)) {
            attribute.accept(classFile, method, this, visitor)
        }
    }

    fun exceptionsAccept(classFile: ClassFile, method: Method, visitor: ExceptionVisitor) {
        for (entry in exceptionTable) {
            visitor.visitException(classFile, method, this, entry)
        }
    }

    fun instructionsAccept(classFile: ClassFile, method: Method, visitor: InstructionVisitor) {
        var offset = 0
        while (offset < codeLength) {
            val instruction = JvmInstruction.create(code, offset)
            instruction.accept(classFile, method, this, offset, visitor)
            offset += instruction.getLength(offset)
        }
    }

    override fun referencedConstantsAccept(classFile: ClassFile, visitor: ReferencedConstantVisitor) {
        super.referencedConstantsAccept(classFile, visitor)

        for (entry in _exceptionTable) {
            entry.referencedConstantsAccept(classFile, visitor)
        }
        for (attribute in attributeMap) {
            attribute.referencedConstantsAccept(classFile, visitor)
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
        return Objects.hash(attributeNameIndex,
                            maxStack,
                            maxLocals,
                            code.contentHashCode(),
                            exceptionTable,
                            attributes)
    }

    companion object {
        internal fun empty(attributeNameIndex: Int): CodeAttribute {
            return CodeAttribute(attributeNameIndex)
        }
    }
}

data class ExceptionEntry private constructor(private var _startPC:      Int = -1,
                                              private var _startLabel:   String? = null,
                                              private var _endPC:        Int = -1,
                                              private var _endLabel:     String? = null,
                                              private var _handlerPC:    Int = -1,
                                              private var _handlerLabel: String? = null,
                                              private var _catchType:    Int = -1): ClassFileContent() {

    override val contentSize: Int
        get() = 8

    val startPC: Int
        get() = _startPC

    val endPC: Int
        get() = _endPC

    val handlerPC: Int
        get() = _handlerPC

    val catchType: Int
        get() = _catchType

    fun getCaughtExceptionClassName(classFile: ClassFile): JvmClassName? {
        return if (catchType > 0) classFile.getClassName(catchType) else null
    }

    internal fun updateOffsets(offsetMap: OffsetMap) {
        if (_startLabel != null) {
            requireNotNull(_endLabel)
            requireNotNull(_handlerLabel)

            _startPC   = offsetMap.getOffset(_startLabel!!)
            _endPC     = offsetMap.getOffset(_endLabel!!)
            _handlerPC = offsetMap.getOffset(_handlerLabel!!)
        } else {
            _startPC   = offsetMap.getNewOffset(_startPC)
            _endPC     = offsetMap.getNewOffset(_endPC)
            _handlerPC = offsetMap.getNewOffset(_handlerPC)
        }
    }

    private fun read(input: ClassDataInput) {
        _startPC   = input.readUnsignedShort()
        _endPC     = input.readUnsignedShort()
        _handlerPC = input.readUnsignedShort()
        _catchType = input.readUnsignedShort()
    }

    override fun write(output: ClassDataOutput) {
        output.writeShort(_startPC)
        output.writeShort(_endPC)
        output.writeShort(_handlerPC)
        output.writeShort(_catchType)
    }

    fun referencedConstantsAccept(classFile: ClassFile, visitor: ReferencedConstantVisitor) {
        if (_catchType > 0) {
            visitor.visitClassConstant(classFile, this, PropertyAccessor(::_catchType))
        }
    }

    companion object {
        internal fun read(input: ClassDataInput): ExceptionEntry {
            val element = ExceptionEntry()
            element.read(input)
            return element
        }

        fun of(startPC: Int, endPC: Int, handlerPC: Int, catchType: Int): ExceptionEntry {
            return ExceptionEntry(startPC, null, endPC, null, handlerPC, null, catchType)
        }

        fun of(startLabel: String, endLabel: String, handlerLabel: String, catchType: Int = 0): ExceptionEntry {
            return ExceptionEntry(-1, startLabel, -1, endLabel, -1, handlerLabel, catchType)
        }
    }
}