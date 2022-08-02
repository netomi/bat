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

class SparseSwitchPayload private constructor(_keys:          IntArray      = EMPTY_ARRAY,
                                              _branchTargets: IntArray      = EMPTY_ARRAY,
                                              _branchLabels:  Array<String> = EMPTY_LABELS) : SwitchPayload(DexOpCode.NOP) {

    var keys: IntArray = _keys
        internal set

    var branchTargets: IntArray = _branchTargets
        internal set

    var branchLabels: Array<String> = _branchLabels
        internal set

    override val length: Int
        get() = branchTargets.size * 4 + 2

    override fun read(instructions: ShortArray, offset: Int) {
        var currOffset = offset
        val size = instructions[++currOffset].toInt() and 0xffff

        keys = IntArray(size)
        for (idx in 0 until size) {
            keys[idx] = (instructions[++currOffset].toInt() and 0xffff) or
                        (instructions[++currOffset].toInt() shl 16)
        }

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

        data[0] = ((opCode.opCode and 0xff) or (IDENT shl 8)).toShort()

        data[1] = keys.size.toShort()
        var offset = 2
        for (i in keys.indices) {
            data[offset++] = keys[i].toShort()
            data[offset++] = (keys[i] shr 16).toShort()
        }

        for (i in branchTargets.indices) {
            data[offset++] = branchTargets[i].toShort()
            data[offset++] = (branchTargets[i] shr 16).toShort()
        }

        return data
    }

    override fun accept(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, visitor: InstructionVisitor) {
        visitor.visitSparseSwitchPayload(dexFile, classDef, method, code, offset, this)
    }

    override fun toString(): String {
        return "sparse-switch-data (${length} units)"
    }

    companion object {
        internal const val IDENT        = 0x02
        private        val EMPTY_ARRAY  = IntArray(0)
        private        val EMPTY_LABELS = emptyArray<String>()

        fun empty(): SparseSwitchPayload {
            return SparseSwitchPayload()
        }

        fun of(keys: IntArray, branchTargets: IntArray): SparseSwitchPayload {
            assert(keys.size == branchTargets.size) { "keys and branchTargets have different sizes" }
            return SparseSwitchPayload(keys, branchTargets)
        }

        fun of(keys: IntArray, branchLabels: Array<String>): SparseSwitchPayload {
            assert(keys.size == branchLabels.size) { "keys and branchLabels have different sizes" }
            return SparseSwitchPayload(keys, _branchLabels = branchLabels)
        }

        internal fun create(instructions: ShortArray, offset: Int): SparseSwitchPayload {
            val opcode = (instructions[offset].toInt() and 0xff).toByte()
            val ident  = (instructions[offset].toInt() ushr 8 and 0xff)
            val opCode = DexOpCode[opcode]

            if (opCode == DexOpCode.NOP && ident == IDENT) {
                val payload = empty()
                payload.read(instructions, offset)
                return payload
            } else {
                throw RuntimeException("expected SparseSwitchPayload at offset $offset")
            }
        }
    }
}