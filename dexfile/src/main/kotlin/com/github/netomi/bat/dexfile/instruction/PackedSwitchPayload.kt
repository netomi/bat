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
import com.github.netomi.bat.dexfile.instruction.editor.OffsetMap
import com.github.netomi.bat.dexfile.instruction.visitor.InstructionVisitor

class PackedSwitchPayload private constructor(firstKey:      Int           = 0,
                                              branchTargets: IntArray      = EMPTY_TARGETS,
                                              branchLabels:  Array<String> = EMPTY_LABELS) : SwitchPayload(DexOpCode.NOP) {

    var firstKey: Int = firstKey
        internal set

    var branchTargets: IntArray = branchTargets
        internal set

    var branchLabels:  Array<String> = branchLabels
        internal set

    override val length: Int
        get() {
            val targetsSize = branchTargets.size.coerceAtLeast(branchLabels.size)
            return targetsSize * 2 + 4
        }

    override fun read(instructions: ShortArray, offset: Int) {
        var currOffset = offset

        val size = instructions[++currOffset].toInt() and 0xffff
        firstKey = (instructions[++currOffset].toInt() and 0xffff) or
                   (instructions[++currOffset].toInt() shl 16)

        branchTargets = IntArray(size)
        for (idx in 0 until size) {
            branchTargets[idx] = (instructions[++currOffset].toInt() and 0xffff) or
                                 (instructions[++currOffset].toInt() shl 16)
        }
    }

    override fun updatePayloadOffsets(payloadInstructionOffset: Int, offsetMap: OffsetMap) {
        if (branchLabels.isNotEmpty()) {
            branchTargets = IntArray(branchLabels.size)
            for (i in branchLabels.indices) {
                branchTargets[i] = offsetMap.computeOffsetDiffToTargetLabel(payloadInstructionOffset, branchLabels[i])
            }
        } else {
            for (i in branchTargets.indices) {
                branchTargets[i] = offsetMap.computeOffsetDiffToTargetOffset(payloadInstructionOffset, branchTargets[i])
            }
        }
    }

    override fun writeData(): ShortArray {
        val data = ShortArray(length)

        data[0] = ((opCode.value and 0xff) or (IDENT shl 8)).toShort()
        data[1] = branchTargets.size.toShort()

        var offset = 2
        data[offset++] = firstKey.toShort()
        data[offset++] = (firstKey shr 16).toShort()

        for (i in branchTargets.indices) {
            data[offset++] = branchTargets[i].toShort()
            data[offset++] = (branchTargets[i] shr 16).toShort()
        }

        return data
    }

    override fun accept(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, visitor: InstructionVisitor) {
        visitor.visitPackedSwitchPayload(dexFile, classDef, method, code, offset, this)
    }

    override fun toString(): String {
        return "packed-switch-data (${length} units)"
    }

    companion object {
        internal const val IDENT         = 0x01
        private        val EMPTY_TARGETS = IntArray(0)
        private        val EMPTY_LABELS  = emptyArray<String>()

        fun empty(): PackedSwitchPayload {
            return PackedSwitchPayload()
        }

        fun of(firstKey: Int, branchTargets: IntArray): PackedSwitchPayload {
            return PackedSwitchPayload(firstKey, branchTargets)
        }

        fun of(firstKey: Int, branchLabels: Array<String>): PackedSwitchPayload {
            return PackedSwitchPayload(firstKey, branchLabels = branchLabels)
        }

        internal fun create(instructions: ShortArray, offset: Int): PackedSwitchPayload {
            val opcode = (instructions[offset].toInt() and 0xff).toByte()
            val ident  = (instructions[offset].toInt() ushr 8 and 0xff)
            val opCode = DexOpCode[opcode]

            if (opCode == DexOpCode.NOP && ident == IDENT) {
                val payload = empty()
                payload.read(instructions, offset)
                return payload
            } else {
                error("expected PackedSwitchPayload at offset $offset")
            }
        }
    }
}