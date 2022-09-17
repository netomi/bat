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
import com.github.netomi.bat.util.toSignedHexString
import com.github.netomi.bat.util.toSignedHexStringWithPrefix

open class LiteralInstruction: DexInstruction {

    var literal: Long = 0
        protected set

    val literalAsFloat: Float
        get() = Float.fromBits(literal.toInt())

    val literalAsDouble: Double
        get() = Double.fromBits(literal)

    protected constructor(opCode: DexOpCode): super(opCode)

    protected constructor(opCode: DexOpCode, literal: Long, vararg registers: Int): super(opCode, *registers) {
        when (opCode.format) {
            FORMAT_11n -> checkRange(literal, -0x8, 0x7, opCode)
            FORMAT_21s -> checkRange(literal, -0x8000, 0x7fff, opCode)
            FORMAT_31i -> checkRange(literal, -0x80000000, 0x7fffffff, opCode)
            FORMAT_21h -> checkRangeHigh16(literal, -0x8000, 0x7fff, opCode)
            FORMAT_51l -> {} // no need to check as it conforms to the range [Long.MIN_VALUE, Long.MAX_VALUE]
            else -> {}
        }

        this.literal = literal
    }

    override fun read(instructions: ShortArray, offset: Int) {
        super.read(instructions, offset)

        literal = when (opCode.format) {
            FORMAT_11n -> (instructions[offset].toInt() shr 12).toLong()

            FORMAT_21s -> instructions[offset + 1].toLong()

            FORMAT_22b -> instructions[offset + 1].toLong() shr 8

            FORMAT_22s -> instructions[offset + 1].toLong()

            FORMAT_31i -> {
                (instructions[offset + 1].toInt() and 0xffff or
                (instructions[offset + 2].toInt() shl 16)).toLong()
            }

            FORMAT_21h -> {
                if (opCode.targetsWideRegister) {
                    instructions[offset + 1].toLong() shl 48
                } else {
                    instructions[offset + 1].toLong() shl 16
                }
            }

            FORMAT_51l -> {
                (instructions [offset + 1].toInt() and 0xffff).toLong()          or
                ((instructions[offset + 2].toInt() and 0xffff).toLong() shl 16) or
                ((instructions[offset + 3].toInt() and 0xffff).toLong() shl 32) or
                ((instructions[offset + 4].toInt() and 0xffff).toLong() shl 48)
            }

            else -> error("unexpected format '${opCode.format}' for opcode '${opCode.mnemonic}'")
        }
    }

    override fun writeData(): ShortArray {
        val data = super.writeData()

        when (opCode.format) {
            FORMAT_11n -> data[0] = (data[0].toInt() or (literal shl 12).toInt()).toShort()

            FORMAT_21s -> data[1] = literal.toShort()

            FORMAT_22b -> data[1] = (data[1].toInt() or (literal.toInt() shl 8)).toShort()

            FORMAT_22s -> data[1] = literal.toShort()

            FORMAT_31i -> {
                data[1] = literal.toShort()
                data[2] = (literal shr 16).toShort()
            }

            FORMAT_21h -> {
                if (opCode.targetsWideRegister) {
                    data[1] = (literal shr 48).toShort()
                } else {
                    data[1] = (literal shr 16).toShort()
                }
            }

            FORMAT_51l -> {
                data[1] = literal.toShort()
                data[2] = (literal shr 16).toShort()
                data[3] = (literal shr 32).toShort()
                data[4] = (literal shr 48).toShort()
            }

            else -> error("unexpected format '${opCode.format}' for opcode '${opCode.mnemonic}'")
        }

        return data
    }

    override fun accept(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, visitor: InstructionVisitor) {
        visitor.visitLiteralInstruction(dexFile, classDef, method, code, offset, this)
    }

    override fun toString(): String {
        return super.toString() + " #${toSignedHexString(literal)}"
    }

    companion object {
        private fun checkRange(value: Long, minValue: Long, maxValue: Long, opCode: DexOpCode) {
            if (value < minValue || value > maxValue) {
                throw IllegalArgumentException("literal value '%s' exceeds allowed range [%s, %s] for opcode '%s'"
                        .format(toSignedHexStringWithPrefix(value),
                                toSignedHexStringWithPrefix(minValue),
                                toSignedHexStringWithPrefix(maxValue),
                                opCode.mnemonic))
            }
        }

        private fun checkRangeHigh16(value: Long, minValue: Long, maxValue: Long, opCode: DexOpCode) {
            val shift = if (opCode.targetsWideRegister) 48 else 16
            if (((value shr shift) shl shift) != value) {
                throw IllegalArgumentException("lower %d bits must be cleared in literal value '%s' for opcode '%s'"
                        .format(shift, toSignedHexStringWithPrefix(value), opCode.mnemonic))
            }

            val shiftedValue = value shr shift
            if (shiftedValue < minValue || shiftedValue > maxValue) {
                val shiftedMinValue = minValue shl shift
                val shiftedMaxValue = maxValue shl shift
                throw IllegalArgumentException("literal value '%s' exceeds allowed range [%s, %s] for opcode '%s'"
                        .format(toSignedHexStringWithPrefix(value),
                                toSignedHexStringWithPrefix(shiftedMinValue),
                                toSignedHexStringWithPrefix(shiftedMaxValue),
                                opCode.mnemonic))
            }
        }

        fun of(opcode: DexOpCode, value: Long, register: Int): LiteralInstruction {
            return LiteralInstruction(opcode, value, register)
        }

        internal fun create(opCode: DexOpCode): LiteralInstruction {
            return LiteralInstruction(opCode)
        }
    }
}