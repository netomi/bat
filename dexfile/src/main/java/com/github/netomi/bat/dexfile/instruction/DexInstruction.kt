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
import com.github.netomi.bat.dexfile.instruction.editor.InstructionWriter
import com.github.netomi.bat.dexfile.instruction.editor.LabelMap

abstract class DexInstruction protected constructor(val opCode: DexOpCode, vararg _registers: Int) {

    var registers: IntArray = _registers
        private set

    open val length: Int
        get() = opCode.length

    val mnemonic: String
        get() = opCode.mnemonic

    open fun read(instructions: ShortArray, offset: Int) {
        registers = when (opCode.format) {
            FORMAT_00x,
            FORMAT_10x,
            FORMAT_10t,
            FORMAT_20t,
            FORMAT_30t ->
                EMPTY_REGISTERS

            FORMAT_11n ->
                intArrayOf(
                    instructions[offset].toInt() ushr 8 and 0xf
                )

            FORMAT_12x,
            FORMAT_22c,
            FORMAT_22s,
            FORMAT_22t ->
                intArrayOf(
                    instructions[offset].toInt() ushr  8 and 0xf,
                    instructions[offset].toInt() ushr 12 and 0xf
                )

            FORMAT_11x,
            FORMAT_21c,
            FORMAT_21t,
            FORMAT_21s,
            FORMAT_21h,
            FORMAT_31c,
            FORMAT_31i,
            FORMAT_31t,
            FORMAT_51l ->
                intArrayOf(
                    instructions[offset].toInt() ushr 8 and 0xff
                )

            FORMAT_22b ->
                intArrayOf(
                    instructions[offset].toInt() ushr 8 and 0xff,
                    instructions[offset + 1].toInt()    and 0xff
                )

            FORMAT_22x ->
                intArrayOf(
                    instructions[offset].toInt() ushr 8 and 0xff,
                    instructions[offset + 1].toInt()    and 0xffff
                )

            FORMAT_23x ->
                intArrayOf(
                    instructions[offset].toInt() ushr 8     and 0xff,
                    instructions[offset + 1].toInt()        and 0xff,
                    instructions[offset + 1].toInt() ushr 8 and 0xff
                )

            FORMAT_32x ->
                intArrayOf(
                    instructions[offset + 1].toInt() and 0xffff,
                    instructions[offset + 2].toInt() and 0xffff
                )

            FORMAT_35c -> {
                val registerCount = instructions[offset].toInt() ushr 12 and 0xf
                when (registerCount) {
                    0 -> EMPTY_REGISTERS
                    1 -> intArrayOf(
                        instructions[offset + 2].toInt()        and 0xf
                    )
                    2 -> intArrayOf(
                        instructions[offset + 2].toInt()        and 0xf,
                        instructions[offset + 2].toInt() ushr 4 and 0xf
                    )
                    3 -> intArrayOf(
                        instructions[offset + 2].toInt()        and 0xf,
                        instructions[offset + 2].toInt() ushr 4 and 0xf,
                        instructions[offset + 2].toInt() ushr 8 and 0xf
                    )
                    4 -> intArrayOf(
                        instructions[offset + 2].toInt()         and 0xf,
                        instructions[offset + 2].toInt() ushr  4 and 0xf,
                        instructions[offset + 2].toInt() ushr  8 and 0xf,
                        instructions[offset + 2].toInt() ushr 12 and 0xf
                    )
                    5 -> intArrayOf(
                        instructions[offset + 2].toInt()         and 0xf,
                        instructions[offset + 2].toInt() ushr  4 and 0xf,
                        instructions[offset + 2].toInt() ushr  8 and 0xf,
                        instructions[offset + 2].toInt() ushr 12 and 0xf,
                        instructions[offset].toInt()     ushr  8 and 0xf
                    )
                    else -> throw IllegalStateException("unexpected register count when reading instruction with opcode $opCode")
                }
            }

            FORMAT_3rc,
            FORMAT_4rcc -> {
                val registerCount  = instructions[offset].toInt() ushr 8 and 0xff
                var register       = instructions[offset + 2].toInt()    and 0xffff
                val rangeRegisters = IntArray(registerCount)
                var i = 0
                while (i < registerCount) {
                    rangeRegisters[i] = register++
                    i++
                }
                rangeRegisters
            }

            FORMAT_45cc -> {
                val registerCount = instructions[offset].toInt() ushr 12 and 0xf
                when (registerCount) {
                    1 -> intArrayOf(
                        instructions[offset + 2].toInt() and 0xf
                    )
                    2 -> intArrayOf(
                        instructions[offset + 2].toInt()        and 0xf,
                        instructions[offset + 2].toInt() ushr 4 and 0xf
                    )
                    3 -> intArrayOf(
                        instructions[offset + 2].toInt()        and 0xf,
                        instructions[offset + 2].toInt() ushr 4 and 0xf,
                        instructions[offset + 2].toInt() ushr 8 and 0xf
                    )
                    4 -> intArrayOf(
                        instructions[offset + 2].toInt()         and 0xf,
                        instructions[offset + 2].toInt() ushr  4 and 0xf,
                        instructions[offset + 2].toInt() ushr  8 and 0xf,
                        instructions[offset + 2].toInt() ushr 12 and 0xf
                    )
                    5 -> intArrayOf(
                        instructions[offset + 2].toInt()         and 0xf,
                        instructions[offset + 2].toInt() ushr  4 and 0xf,
                        instructions[offset + 2].toInt() ushr  8 and 0xf,
                        instructions[offset + 2].toInt() ushr 12 and 0xf,
                        instructions[offset].toInt()     ushr  8 and 0xf
                    )
                    else -> throw IllegalStateException("unexpected register count when reading instruction with opcode $opCode")
                }
            }

            else -> throw IllegalStateException("unsupported format ${opCode.format} for opCode $opCode encountered")
        }
    }

    fun write(writer: InstructionWriter, offset: Int, labelOffsetMap: LabelMap? = null) {
        var currOffset = offset

        if (labelOffsetMap != null) {
            updateOffsets(offset, labelOffsetMap)
        }

        val instructionData = writeData()
        for (instructionDatum in instructionData) {
            writer.write(currOffset++, instructionDatum)
        }
    }

    protected open fun updateOffsets(offset: Int, labelOffsetMap: LabelMap) {}

    protected open fun writeData(): ShortArray {
        val data = ShortArray(length)

        data[0] = (opCode.opCode and 0xff).toShort()

        when (opCode.format) {
            FORMAT_00x,
            FORMAT_10x,
            FORMAT_10t,
            FORMAT_20t,
            FORMAT_30t -> {}

            FORMAT_11n -> {
                val a = registers[0] and 0xf
                if (a != registers[0]) {
                    throw IllegalStateException("register number " + registers[0] + " too big for opcode format")
                }
                data[0] = (data[0].toInt() or (a shl 8)).toShort()
            }

            FORMAT_12x,
            FORMAT_22c,
            FORMAT_22s,
            FORMAT_22t -> {
                val a = registers[0] and 0xf
                val b = registers[1] and 0xf
                if (a != registers[0]) {
                    throw IllegalStateException("register number " + registers[0] + " too big for opcode format")
                }
                if (b != registers[1]) {
                    throw IllegalStateException("register number " + registers[1] + " too big for opcode format")
                }
                data[0] = (data[0].toInt() or (a shl 8 or (b shl 12))).toShort()
            }

            FORMAT_11x,
            FORMAT_21c,
            FORMAT_21t,
            FORMAT_21s,
            FORMAT_21h,
            FORMAT_31c,
            FORMAT_31i,
            FORMAT_31t,
            FORMAT_51l -> {
                val a = registers[0] and 0xff
                if (a != registers[0]) {
                    throw IllegalStateException("register number " + registers[0] + " too big for opcode format")
                }
                data[0] = (data[0].toInt() or (a shl 8)).toShort()
            }

            FORMAT_22b -> {
                val a = registers[0] and 0xff
                val b = registers[1] and 0xff
                if (a != registers[0]) {
                    throw IllegalStateException("register number " + registers[0] + " too big for opcode format")
                }
                if (b != registers[1]) {
                    throw IllegalStateException("register number " + registers[1] + " too big for opcode format")
                }
                data[0] = (data[0].toInt() or (a shl 8)).toShort()
                data[1] = (data[1].toInt() or b).toShort()
            }

            FORMAT_22x -> {
                val a = registers[0] and 0xff
                val b = registers[1] and 0xffff
                if (a != registers[0]) {
                    throw IllegalStateException("register number " + registers[0] + " too big for opcode format")
                }
                if (b != registers[1]) {
                    throw IllegalStateException("register number " + registers[1] + " too big for opcode format")
                }
                data[0] = (data[0].toInt() or (a shl 8)).toShort()
                data[1] = (data[1].toInt() or b).toShort()
            }

            FORMAT_23x -> {
                val a = registers[0] and 0xff
                val b = registers[1] and 0xff
                val c = registers[2] and 0xff
                if (a != registers[0]) {
                    throw IllegalStateException("register number " + registers[0] + " too big for opcode format")
                }
                if (b != registers[1]) {
                    throw IllegalStateException("register number " + registers[1] + " too big for opcode format")
                }
                if (c != registers[2]) {
                    throw IllegalStateException("register number " + registers[2] + " too big for opcode format")
                }
                data[0] = (data[0].toInt() or (a shl 8)).toShort()
                data[1] = (data[1].toInt() or (b or (c shl 8))).toShort()
            }

            FORMAT_32x -> {
                val a = registers[0] and 0xffff
                val b = registers[1] and 0xffff
                if (a != registers[0]) {
                    throw IllegalStateException("register number " + registers[0] + " too big for opcode format")
                }
                if (b != registers[1]) {
                    throw IllegalStateException("register number " + registers[1] + " too big for opcode format")
                }
                data[1] = (data[1].toInt() or a).toShort()
                data[2] = (data[2].toInt() or b).toShort()
            }

            FORMAT_35c -> {
                when (registers.size) {
                    0 -> {}
                    1 -> {
                        data[0] = (data[0].toInt() or (1 shl 12)).toShort()
                        data[2] = (data[2].toInt() or registers[0]).toShort()
                    }
                    2 -> {
                        data[0] = (data[0].toInt() or (2 shl 12)).toShort()
                        data[2] = (data[2].toInt() or (registers[0] or (registers[1] shl 4))).toShort()
                    }
                    3 -> {
                        data[0] = (data[0].toInt() or (3 shl 12)).toShort()
                        data[2] = (data[2].toInt() or (registers[0] or (registers[1] shl 4) or (registers[2] shl 8))).toShort()
                    }
                    4 -> {
                        data[0] = (data[0].toInt() or (4 shl 12)).toShort()
                        data[2] = (data[2].toInt() or (registers[0] or (registers[1] shl 4) or (registers[2] shl 8) or (registers[3] shl 12))).toShort()
                    }
                    5 -> {
                        data[0] = (data[0].toInt() or (4 shl 12 or (registers[4] shl 8))).toShort()
                        data[2] = (data[2].toInt() or (registers[0] or (registers[1] shl 4) or (registers[2] shl 8) or (registers[3] shl 12))).toShort()
                    }
                    else -> throw IllegalStateException("unsupported register count when writing instruction with opcode $opCode")
                }
            }

            FORMAT_3rc,
            FORMAT_4rcc -> {
                data[0] = (data[0].toInt() or (registers.size shl 8)).toShort()
                data[2] = (data[2].toInt() or registers[0]).toShort()
            }

            FORMAT_45cc -> {
                data[0] = (data[0].toInt() or (registers.size shl 12)).toShort()
                when (registers.size) {
                    1 -> data[2] = (data[2].toInt() or registers[0]).toShort()
                    2 -> data[2] = (data[2].toInt() or (registers[0] or (registers[1] shl 4))).toShort()
                    3 -> data[2] = (data[2].toInt() or (registers[0] or (registers[1] shl 4) or (registers[2] shl 8))).toShort()
                    4 -> data[2] = (data[2].toInt() or (registers[0] or (registers[1] shl 4) or (registers[2] shl 8) or (registers[3] shl 12))).toShort()
                    5 -> {
                        data[2] = (data[2].toInt() or (registers[0] or (registers[1] shl 4) or (registers[2] shl 8) or (registers[3] shl 12))).toShort()
                        data[0] = (data[0].toInt() or (registers[4] shl 8)).toShort()
                    }
                    else -> throw IllegalStateException("unsupported register count when writing instruction with opcode $opCode")
                }
            }

            else -> {}
        }
        return data
    }

    abstract fun accept(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, visitor: InstructionVisitor)

    override fun toString(): String {
        return buildString {
            append(opCode.mnemonic)

            if (registers.isNotEmpty()) {
                append(" ${registers.joinToString(separator = ",", transform = { "v$it" } )}")
            }
        }
    }

    companion object {
        private val EMPTY_REGISTERS = IntArray(0)

        fun create(instructions: ShortArray, offset: Int): DexInstruction {
            val opcode = (instructions[offset].toInt() and 0xff).toByte()
            val ident  = (instructions[offset].toInt() ushr 8 and 0xff).toByte()
            val opCode = DexOpCode[opcode]

            val instruction = when (opCode) {
                DexOpCode.NOP -> {
                    if (ident.toInt() == PackedSwitchPayload.IDENT) {
                        PackedSwitchPayload.empty()
                    } else if (ident.toInt() == SparseSwitchPayload.IDENT) {
                        SparseSwitchPayload.empty()
                    } else if (ident.toInt() == FillArrayPayload.IDENT) {
                        FillArrayPayload.empty()
                    } else {
                        opCode.createInstruction()
                    }
                }
                else -> {
                    opCode.createInstruction()
                }
            }

            instruction.read(instructions, offset)
            return instruction
        }
    }
}