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

class BranchInstruction internal constructor(opcode: DexOpCode, _branchOffset: Int = 0, vararg registers: Int) : DexInstruction(opcode, *registers) {

    var branchOffset = _branchOffset
        private set

    override fun read(instructions: ShortArray, offset: Int) {
        super.read(instructions, offset)
        branchOffset = when (opcode.format) {
            DexInstructionFormat.FORMAT_10t -> instructions[offset].toInt() shr 8

            DexInstructionFormat.FORMAT_20t,
            DexInstructionFormat.FORMAT_21t,
            DexInstructionFormat.FORMAT_22t -> instructions[offset + 1].toInt()

            DexInstructionFormat.FORMAT_30t,
            DexInstructionFormat.FORMAT_31t -> instructions[offset + 1].toInt() and 0xffff or (instructions[offset + 2].toInt() shl 16)

            else -> throw IllegalStateException("unexpected format for opcode " + opcode.mnemonic)
        }
    }

    override fun writeData(): ShortArray {
        val data = super.writeData()

        when (opcode.format) {
            DexInstructionFormat.FORMAT_10t -> data[0] = (data[0].toInt() or (branchOffset shl 8)).toShort()

            DexInstructionFormat.FORMAT_20t,
            DexInstructionFormat.FORMAT_21t,
            DexInstructionFormat.FORMAT_22t -> data[1] = branchOffset.toShort()

            DexInstructionFormat.FORMAT_30t,
            DexInstructionFormat.FORMAT_31t -> {
                data[1] = (branchOffset and 0xffff).toShort()
                data[2] = (branchOffset shr 16).toShort()
            }

            else -> {}
        }
        return data
    }

    override fun accept(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, visitor: InstructionVisitor) {
        visitor.visitBranchInstruction(dexFile, classDef, method, code, offset, this)
    }

    companion object {
        fun of(opCode: DexOpCode, branchOffset: Int, vararg registers: Int): BranchInstruction {
            return BranchInstruction(opCode, branchOffset, *registers)
        }

        @JvmStatic
        fun create(opCode: DexOpCode, ident: Byte): BranchInstruction {
            return BranchInstruction(opCode)
        }
    }
}