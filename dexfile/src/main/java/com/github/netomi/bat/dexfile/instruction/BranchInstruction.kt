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
import com.github.netomi.bat.dexfile.instruction.editor.OffsetMap
import com.github.netomi.bat.dexfile.instruction.visitor.InstructionVisitor
import com.github.netomi.bat.util.toSignedHexString

open class BranchInstruction internal constructor(opcode:           DexOpCode,
                                                  _branchOffset:    Int     = 0,
                                                  _branchLabel:     String? = null,
                                                  vararg registers: Int) : DexInstruction(opcode, *registers) {

    var branchOffset = _branchOffset
        internal set

    var branchLabel: String? = _branchLabel
        private set

    override fun read(instructions: ShortArray, offset: Int) {
        super.read(instructions, offset)
        branchOffset = when (opCode.format) {
            FORMAT_10t -> instructions[offset].toInt() shr 8

            FORMAT_20t,
            FORMAT_21t,
            FORMAT_22t -> instructions[offset + 1].toInt()

            FORMAT_30t,
            FORMAT_31t -> instructions[offset + 1].toInt() and 0xffff or (instructions[offset + 2].toInt() shl 16)

            else -> throw IllegalStateException("unexpected format ${opCode.format} for opcode ${opCode.mnemonic}")
        }
    }

    override fun updateOffsets(offset: Int, offsetMap: OffsetMap) {
        branchOffset = if (branchLabel != null) {
            offsetMap.computeDiffToTargetLabel(offset, branchLabel!!)
        } else {
            offsetMap.updateDiffToTargetOffset(offset, branchOffset)
        }
    }

    override fun writeData(): ShortArray {
        val data = super.writeData()

        when (opCode.format) {
            FORMAT_10t -> data[0] = (data[0].toInt() or (branchOffset shl 8)).toShort()

            FORMAT_20t,
            FORMAT_21t,
            FORMAT_22t -> data[1] = branchOffset.toShort()

            FORMAT_30t,
            FORMAT_31t -> {
                data[1] = (branchOffset and 0xffff).toShort()
                data[2] = (branchOffset shr 16).toShort()
            }

            else -> {}
        }
        return data
    }

    override fun toString(): String {
        val separator = if (registers.isEmpty()) " " else ", "
        val offsetString = toSignedHexString(branchOffset, 4)
        return super.toString() + separator + offsetString
    }

    override fun accept(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, visitor: InstructionVisitor) {
        visitor.visitBranchInstruction(dexFile, classDef, method, code, offset, this)
    }

    companion object {
        fun of(opCode: DexOpCode, branchOffset: Int, vararg registers: Int): BranchInstruction {
            return BranchInstruction(opCode, branchOffset, null, *registers)
        }

        fun of(opCode: DexOpCode, branchLabel: String, vararg registers: Int): BranchInstruction {
            return BranchInstruction(opCode, 0, branchLabel, *registers)
        }

        fun create(opCode: DexOpCode): BranchInstruction {
            return BranchInstruction(opCode)
        }
    }
}