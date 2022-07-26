/*
 *  Copyright (c) 2020 Thomas Neidhart.
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
package com.github.netomi.bat.dexfile.instruction

import com.github.netomi.bat.dexfile.ClassDef
import com.github.netomi.bat.dexfile.Code
import com.github.netomi.bat.dexfile.DexFile
import com.github.netomi.bat.dexfile.EncodedMethod
import com.github.netomi.bat.dexfile.instruction.InstructionFormat.*
import com.github.netomi.bat.dexfile.instruction.visitor.InstructionVisitor

class ArithmeticLiteralInstruction internal constructor(opCode: DexOpCode, _literal: Int = 0, vararg registers: Int) : ArithmeticInstruction(opCode, *registers) {

    var literal = _literal
        private set

    override fun read(instructions: ShortArray, offset: Int) {
        super.read(instructions, offset)

        literal = when (opCode.format) {
            FORMAT_22b -> instructions[offset + 1].toInt() shr 8
            FORMAT_22s -> instructions[offset + 1].toInt()

            else -> throw IllegalStateException("unexpected format ${opCode.format} for opcode ${opCode.mnemonic}")
        }
    }

    override fun writeData(): ShortArray {
        val data = super.writeData()

        when (opCode.format) {
            FORMAT_22b -> data[1] = (data[1].toInt() or (literal shl 8)).toShort()
            FORMAT_22s -> data[1] = literal.toShort()

            else -> {}
        }
        return data
    }

    override fun accept(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, visitor: InstructionVisitor) {
        visitor.visitArithmeticLiteralInstruction(dexFile, classDef, method, code, offset, this)
    }

    companion object {
        fun of(opCode: DexOpCode, literal: Int, vararg registers: Int): ArithmeticLiteralInstruction {
            return ArithmeticLiteralInstruction(opCode, literal, *registers)
        }

        fun create(opCode: DexOpCode): ArithmeticLiteralInstruction {
            return ArithmeticLiteralInstruction(opCode)
        }
    }
}