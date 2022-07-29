/*
 *  Copyright (c) 2020-2022 Thomas Neidhart.
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

package com.github.netomi.bat.dexfile.instruction.editor

import com.github.netomi.bat.dexfile.instruction.DexInstruction

class InstructionWriter constructor(size: Int = 8192) {

    var array: ShortArray = ShortArray(size)

    private var lastWrittenOffset: Int = -1

    val nextWriteOffset: Int
        get() = lastWrittenOffset + 1

    fun reset() {
        lastWrittenOffset = -1
    }

    fun write(offset: Int, value: Short) {
        ensureCapacity(offset)
        array[offset] = value
        lastWrittenOffset = offset
    }

    fun read(offset: Int): Short {
        if (offset > lastWrittenOffset) {
            throw RuntimeException("read data at offset $offset before it was written")
        }
        return array[offset]
    }

    fun getInstructionArray(): ShortArray {
        return array.copyOfRange(0, lastWrittenOffset + 1)
    }

    private fun ensureCapacity(offset: Int) {
        if (offset >= array.size) {
            array = array.copyOf(array.size + INCREMENT)
        }
    }

    companion object {
        private const val INCREMENT = 8192

        fun writeInstructions(instructions: List<DexInstruction>): ShortArray {
            val codeLen = instructions.stream().map { a: DexInstruction -> a.length }.reduce(0) { a, b -> a + b }

            val writer = InstructionWriter(codeLen)
            var offset = 0
            for (instruction in instructions) {
                instruction.write(writer, offset)
                offset += instruction.length
            }

            return writer.array
        }
    }
}