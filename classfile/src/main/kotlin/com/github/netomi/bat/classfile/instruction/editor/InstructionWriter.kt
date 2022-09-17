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

package com.github.netomi.bat.classfile.instruction.editor

import com.github.netomi.bat.classfile.instruction.JvmInstruction

class InstructionWriter constructor(size: Int = 8192) {

    private var array: ByteArray = ByteArray(size)

    private var lastWrittenOffset: Int = -1

    val nextWriteOffset: Int
        get() = lastWrittenOffset + 1

    fun reset() {
        lastWrittenOffset = -1
    }

    fun write(offset: Int, data: Byte) {
        ensureCapacity(offset)
        array[offset]     = data
        lastWrittenOffset = offset
    }

    fun write(offset: Int, data: ByteArray) {
        require(data.isNotEmpty())
        val lastOffsetToWrite = offset + data.size - 1
        ensureCapacity(lastOffsetToWrite)
        data.copyInto(array, offset)
        lastWrittenOffset = lastOffsetToWrite
    }

    fun read(offset: Int): Byte {
        if (offset > lastWrittenOffset) {
            error("read data at offset '$offset' before it was written")
        }
        return array[offset]
    }

    fun getInstructionArray(): ByteArray {
        return array.copyOfRange(0, lastWrittenOffset + 1)
    }

    private fun ensureCapacity(offset: Int) {
        if (offset >= array.size) {
            array = array.copyOf(array.size + INCREMENT)
        }
    }

    companion object {
        private const val INCREMENT = 8192

        fun writeInstructions(instructions: List<JvmInstruction>): ByteArray {
            val codeLen = instructions.fold(0) { offset, instruction -> offset + instruction.getLength(offset) }

            val writer = InstructionWriter(codeLen)
            for (instruction in instructions) {
                instruction.write(writer, writer.nextWriteOffset)
            }

            return writer.getInstructionArray()
        }
    }
}