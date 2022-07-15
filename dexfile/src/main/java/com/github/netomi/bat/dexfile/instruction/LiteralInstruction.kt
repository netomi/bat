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
import com.github.netomi.bat.dexfile.visitor.InstructionVisitor

class LiteralInstruction internal constructor(opcode: DexOpCode, _literal: Long = 0, vararg registers: Int) : DexInstruction(opcode, *registers) {

    var value: Long = _literal
        private set

    val valueAsFloat: Float
        get() = java.lang.Float.intBitsToFloat(value.toInt())

    val valueAsDouble: Double
        get() = java.lang.Double.longBitsToDouble(value)

    override fun read(instructions: ShortArray, offset: Int) {
        super.read(instructions, offset)

        value = when (opcode.format) {
            DexInstructionFormat.FORMAT_11n -> (instructions[offset].toInt() shr 12).toLong()

            DexInstructionFormat.FORMAT_21s -> instructions[offset + 1].toLong()

            DexInstructionFormat.FORMAT_31i -> {
                (instructions[offset + 1].toInt() and 0xffff or
                (instructions[offset + 2].toInt() shl 16)).toLong()
            }

            DexInstructionFormat.FORMAT_21h -> {
                if (opcode.targetsWideRegister()) {
                    instructions[offset + 1].toLong() shl 48
                } else {
                    (instructions[offset + 1].toInt() shl 16).toLong()
                }
            }

            DexInstructionFormat.FORMAT_51l -> {
                (instructions[offset + 1].toInt() and 0xffff).toLong()          or
                ((instructions[offset + 2].toInt() and 0xffff).toLong() shl 16) or
                ((instructions[offset + 3].toInt() and 0xffff).toLong() shl 32) or
                ((instructions[offset + 4].toInt() and 0xffff).toLong() shl 48)
            }

            else -> throw IllegalStateException("unexpected format for opcode " + opcode.mnemonic)
        }
    }

    override fun writeData(): ShortArray {
        val data = super.writeData()

        when (opcode.format) {
            DexInstructionFormat.FORMAT_11n -> data[0] = (data[0].toInt() or (value shl 12).toInt()).toShort()

            DexInstructionFormat.FORMAT_21s -> data[1] = value.toShort()

            DexInstructionFormat.FORMAT_31i -> {
                data[1] = value.toShort()
                data[2] = (value shr 16).toShort()
            }

            DexInstructionFormat.FORMAT_21h -> {
                if (opcode.targetsWideRegister()) {
                    data[1] = (value shr 48).toShort()
                } else {
                    data[1] = (value shr 16).toShort()
                }
            }

            DexInstructionFormat.FORMAT_51l -> {
                data[1] = value.toShort()
                data[2] = (value shr 16).toShort()
                data[3] = (value shr 32).toShort()
                data[4] = (value shr 48).toShort()
            }

            else -> throw IllegalStateException("unexpected format for opcode " + opcode.mnemonic)
        }

        return data
    }

    override fun accept(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, visitor: InstructionVisitor) {
        visitor.visitLiteralInstruction(dexFile, classDef, method, code, offset, this)
    }

    companion object {
        fun of(opcode: DexOpCode, value: Long, vararg registers: Int): LiteralInstruction {
            return LiteralInstruction(opcode, value, *registers)
        }

        @JvmStatic
        fun create(opCode: DexOpCode, ident: Byte): LiteralInstruction {
            return LiteralInstruction(opCode)
        }
    }
}