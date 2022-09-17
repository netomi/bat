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

import com.github.netomi.bat.dexfile.instruction.DexOpCode.*
import com.github.netomi.bat.dexfile.instruction.InstructionFormat.*
import com.github.netomi.bat.dexfile.instruction.editor.OffsetMap
import com.github.netomi.bat.util.toSignedHexString

abstract class PayloadInstruction<T: Payload>: DexInstruction {

    var payloadOffset = 0
        internal set

    abstract val payload: T

    protected constructor(opCode: DexOpCode): super(opCode)

    protected constructor(opCode: DexOpCode, register: Int): super(opCode, register)

    override fun read(instructions: ShortArray, offset: Int) {
        super.read(instructions, offset)
        payloadOffset = when (opCode.format) {
            FORMAT_31t -> (instructions[offset + 1].toInt() and 0xffff) or
                          (instructions[offset + 2].toInt() shl 16)

            else -> error("unexpected format '${opCode.format}' for opcode '${opCode.mnemonic}'")
        }
    }

    override fun updateOffsets(offset: Int, offsetMap: OffsetMap) {
        payloadOffset = offsetMap.computeOffsetDiffToPayload(offset, payload)
        payload.updatePayloadOffsets(offset, offsetMap)
    }

    override fun writeData(): ShortArray {
        val data = super.writeData()

        when (opCode.format) {
            FORMAT_31t -> {
                data[1] = payloadOffset.toShort()
                data[2] = (payloadOffset shr 16).toShort()
            }

            else -> {}
        }

        return data
    }

    override fun toString(): String {
        return super.toString() + ", ${toSignedHexString(payloadOffset, 8)}"
    }

    companion object {
        fun of(opCode: DexOpCode, payload: Payload, register: Int): PayloadInstruction<*> {
            return when (opCode) {
                FILL_ARRAY_DATA -> FillArrayDataInstruction.of(payload as FillArrayPayload, register)
                PACKED_SWITCH   -> PackedSwitchInstruction.of(payload as PackedSwitchPayload, register)
                SPARSE_SWITCH   -> SparseSwitchInstruction.of(payload as SparseSwitchPayload, register)
                else            -> error("unexpected opcode '$opCode' for PayloadInstruction")
            }
        }
    }
}
