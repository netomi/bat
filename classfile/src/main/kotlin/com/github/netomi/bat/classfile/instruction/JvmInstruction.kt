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

package com.github.netomi.bat.classfile.instruction

import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.Method
import com.github.netomi.bat.classfile.attribute.CodeAttribute
import com.github.netomi.bat.classfile.instruction.editor.InstructionWriter
import com.github.netomi.bat.classfile.instruction.visitor.InstructionVisitor

abstract class JvmInstruction protected constructor(val opCode: JvmOpCode) {

    val mnemonic: String
        get() = opCode.mnemonic

    open fun getLength(offset: Int): Int {
        return opCode.length
    }

    abstract fun read(instructions: ByteArray, offset: Int)
    abstract fun write(writer: InstructionWriter, offset: Int)

    protected fun writeLiteral(writer: InstructionWriter, offset: Int, literal: Int) {
        writeOffset(writer, offset, literal)
    }

    protected fun writeLiteralWide(writer: InstructionWriter, offset: Int, literal: Int) {
        writeOffsetWide(writer, offset, literal)
    }

    protected fun writeOffset(writer: InstructionWriter, offset: Int, offsetValue: Int) {
        writer.write(offset,     ((offsetValue shr 8) and 0xff).toByte())
        writer.write(offset + 1,  (offsetValue        and 0xff).toByte())
    }

    protected fun writeOffsetWide(writer: InstructionWriter, offset: Int, offsetValue: Int) {
        writer.write(offset,     ((offsetValue  shr 24) and 0xff).toByte())
        writer.write(offset + 1, ((offsetValue ushr 16) and 0xff).toByte())
        writer.write(offset + 2, ((offsetValue ushr  8) and 0xff).toByte())
        writer.write(offset + 3,  (offsetValue          and 0xff).toByte())
    }

    protected fun writeIndex(writer: InstructionWriter, offset: Int, index: Int) {
        writer.write(offset,     ((index ushr 8) and 0xff).toByte())
        writer.write(offset + 1,  (index         and 0xff).toByte())
    }

    abstract fun accept(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, visitor: InstructionVisitor)

    open fun toString(classFile: ClassFile): String {
        return toString()
    }

    override fun toString(): String {
        return buildString {
            append(opCode.mnemonic)
        }
    }

    companion object {
        fun create(instructions: ByteArray, offset: Int): JvmInstruction {
            var opcode     = instructions[offset]
            var opCode     = JvmOpCode[opcode]
            var wide       = false

            if (opCode == JvmOpCode.WIDE) {
                wide   = true
                opcode = instructions[offset + 1]
                opCode = JvmOpCode[opcode]
            }

            val instruction = opCode.createInstruction(wide)
            instruction.read(instructions, offset)
            return instruction
        }

        internal fun getLiteral(literalByte1: Byte, literalByte2: Byte): Int {
            return getOffset(literalByte1, literalByte2)
        }

        internal fun getOffset(offsetByte1: Byte, offsetByte2: Byte): Int {
            val a = offsetByte1.toInt()
            val b = offsetByte2.toInt() and 0xff
            return (a shl 8) or b
        }

        internal fun getLiteral(literalByte1: Byte, literalByte2: Byte, literalByte3: Byte, literalByte4: Byte): Int {
            return getOffset(literalByte1, literalByte2, literalByte3, literalByte4)
        }

        internal fun getOffset(offsetByte1: Byte, offsetByte2: Byte, offsetByte3: Byte, offsetByte4: Byte): Int {
            val a = offsetByte1.toInt()
            val b = offsetByte2.toInt() and 0xff
            val c = offsetByte3.toInt() and 0xff
            val d = offsetByte4.toInt() and 0xff
            return (a shl 24) or (b shl 16) or (c shl 8) or d
        }

        internal fun getIndex(indexByte1: Byte, indexByte2: Byte): Int {
            val a = indexByte1.toInt() and 0xff
            val b = indexByte2.toInt() and 0xff
            return (a shl 8) or b
        }
    }
}