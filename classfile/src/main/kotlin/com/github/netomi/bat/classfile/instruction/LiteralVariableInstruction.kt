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

class LiteralVariableInstruction: VariableInstruction {

    var value: Int = 0
        private set

    private constructor(opCode: JvmOpCode, wide: Boolean): super(opCode, wide)

    private constructor(opCode: JvmOpCode, variable: Int, value: Int): super(opCode, variable) {
        this.value = value
    }

    override fun getLength(offset: Int): Int {
        return if (wide) {
            super.getLength(offset) + 1
        } else {
            super.getLength(offset)
        }
    }

    override fun read(instructions: ByteArray, offset: Int) {
        super.read(instructions, offset)

        value = if (wide) {
            getLiteral(instructions[offset + 4], instructions[offset + 5])
        } else {
            instructions[offset + 2].toInt()
        }
    }

    override fun writeData(writer: InstructionWriter, offset: Int) {
        super.writeData(writer, offset)

        if (wide) {
            writeLiteral(writer, offset + 4, value)
        } else {
            writer.write(offset + 2, value.toByte())
        }
    }

    override fun accept(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, visitor: InstructionVisitor) {
        visitor.visitLiteralVariableInstruction(classFile, method, code, offset, this)
    }

    companion object {
        internal fun create(opCode: JvmOpCode, wide: Boolean): LiteralVariableInstruction {
            return LiteralVariableInstruction(opCode, wide)
        }

        fun of(opCode: JvmOpCode, variable: Int, value: Int): LiteralVariableInstruction {
            return LiteralVariableInstruction(opCode, variable, value)
        }
    }
}