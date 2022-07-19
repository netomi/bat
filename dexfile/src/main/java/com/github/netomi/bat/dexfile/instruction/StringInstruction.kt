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

import com.github.netomi.bat.dexfile.*
import com.github.netomi.bat.dexfile.visitor.InstructionVisitor

class StringInstruction internal constructor(opcode: DexOpCode, _stringIndex: Int = NO_INDEX, vararg registers: Int) : DexInstruction(opcode, *registers) {

    var stringIndex: Int = _stringIndex
        private set

    fun getString(dexFile: DexFile): String {
        return dexFile.getStringID(stringIndex).stringValue
    }

    override fun read(instructions: ShortArray, offset: Int) {
        super.read(instructions, offset)

        stringIndex = when (opcode.format) {
            DexInstructionFormat.FORMAT_21c -> instructions[offset + 1].toInt() and 0xffff

            DexInstructionFormat.FORMAT_31c -> (instructions[offset + 1].toInt() and 0xffff) or
                                               (instructions[offset + 2].toInt() shl 16)

            else -> throw IllegalStateException("unexpected format for opcode " + opcode.mnemonic)
        }
    }

    override fun writeData(): ShortArray {
        val data = super.writeData()

        when (opcode.format) {
            DexInstructionFormat.FORMAT_21c -> data[1] = stringIndex.toShort()

            DexInstructionFormat.FORMAT_31c -> {
                data[1] = stringIndex.toShort()
                data[2] = (stringIndex shr 16).toShort()
            }

            else -> {}
        }

        return data
    }

    override fun accept(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, visitor: InstructionVisitor) {
        visitor.visitStringInstruction(dexFile, classDef, method, code, offset, this)
    }

    companion object {
        fun of(opCode: DexOpCode, stringIndex: Int, vararg registers: Int): StringInstruction {
            return StringInstruction(opCode, stringIndex, *registers)
        }

        @JvmStatic
        fun create(opCode: DexOpCode, ident: Byte): StringInstruction {
            return StringInstruction(opCode)
        }
    }
}