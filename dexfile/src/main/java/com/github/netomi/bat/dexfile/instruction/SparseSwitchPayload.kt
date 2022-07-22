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

class SparseSwitchPayload private constructor(_keys: IntArray = EMPTY_ARRAY, _branchTargets: IntArray = EMPTY_ARRAY) : SwitchPayload(DexOpCode.NOP) {

    var keys: IntArray = _keys
        internal set

    var branchTargets: IntArray = _branchTargets
        internal set

    override fun getLength(): Int {
        return branchTargets.size * 4 + 2
    }

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

    override fun writeData(): ShortArray {
        val data = ShortArray(length)

        data[0] = ((opcode.opCode.toInt() and 0xff) or (IDENT shl 8)).toShort()

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
        internal const val IDENT       = 0x02
        private        val EMPTY_ARRAY = IntArray(0)

        fun empty(): SparseSwitchPayload {
            return SparseSwitchPayload()
        }

        fun of(keys: IntArray, branchTargets: IntArray): SparseSwitchPayload {
            return SparseSwitchPayload(keys, branchTargets)
        }
    }
}