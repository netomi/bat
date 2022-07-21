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
import com.github.netomi.bat.dexfile.visitor.InstructionVisitor

class PayloadInstruction internal constructor(opcode: DexOpCode, _payloadOffset: Int = 0, vararg registers: Int) : DexInstruction(opcode, *registers) {

    var payloadOffset = _payloadOffset
        internal set

    override fun read(instructions: ShortArray, offset: Int) {
        super.read(instructions, offset)
        payloadOffset = when (opcode.format) {
            FORMAT_31t -> (instructions[offset + 1].toInt() and 0xffff) or
                          (instructions[offset + 2].toInt() shl 16)

            else -> throw IllegalStateException("unexpected format for opcode " + opcode.mnemonic)
        }
    }

    override fun writeData(): ShortArray {
        val data = super.writeData()

        when (opcode.format) {
            FORMAT_31t -> {
                data[1] = payloadOffset.toShort()
                data[2] = (payloadOffset shr 16).toShort()
            }

            else -> {}
        }

        return data
    }

    override fun accept(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, visitor: InstructionVisitor) {
        visitor.visitPayloadInstruction(dexFile, classDef, method, code, offset, this)
    }

    companion object {
        @JvmStatic
        fun create(opCode: DexOpCode, ident: Byte): PayloadInstruction {
            return PayloadInstruction(opCode)
        }
    }
}