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

package com.github.netomi.bat.classfile.instruction

import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.Method
import com.github.netomi.bat.classfile.attribute.CodeAttribute
import com.github.netomi.bat.classfile.instruction.editor.InstructionWriter
import com.github.netomi.bat.classfile.instruction.visitor.InstructionVisitor
import com.github.netomi.bat.util.mutableListOfCapacity

class TableSwitchInstruction
    private constructor(opCode:           JvmOpCode,
                        defaultOffset:    Int                          = 0,
                        defaultLabel:     String?                      = null,
                        matchOffsetPairs: MutableList<MatchOffsetPair> = mutableListOfCapacity(0))
    : SwitchInstruction(opCode, defaultOffset, defaultLabel, matchOffsetPairs) {

    override fun getLength(offset: Int): Int {
        val padding = getPadding(offset + 1)
        return 1 + padding + 12 + matchOffsetPairs.size * 4
    }

    val lowValue: Int
        get() = matchOffsetPairs.first().match

    val highValue: Int
        get() = matchOffsetPairs.last().match

    override fun read(instructions: ByteArray, offset: Int) {
        var currOffset = offset + 1
        currOffset    += getPadding(currOffset)

        _defaultOffset =
            getOffset(instructions[currOffset++],
                      instructions[currOffset++],
                      instructions[currOffset++],
                      instructions[currOffset++])

        val lowValue =
            getLiteral(instructions[currOffset++],
                       instructions[currOffset++],
                       instructions[currOffset++],
                       instructions[currOffset++])

        val highValue =
            getLiteral(instructions[currOffset++],
                       instructions[currOffset++],
                       instructions[currOffset++],
                       instructions[currOffset++])

        val numberOfPairs = highValue - lowValue + 1
        matchOffsetPairs = mutableListOfCapacity(numberOfPairs)

        var currentMatch = lowValue
        for (i in 0 until numberOfPairs) {
            val offsetOfMatch =
                getOffset(instructions[currOffset++],
                          instructions[currOffset++],
                          instructions[currOffset++],
                          instructions[currOffset++])

            matchOffsetPairs.add(MatchOffsetPair(currentMatch, offsetOfMatch))
            currentMatch++
        }
    }

    override fun write(writer: InstructionWriter, offset: Int) {
        var currOffset = offset
        writer.write(currOffset++, opCode.value.toByte())
        val padding = getPadding(currOffset)
        for (i in 0 until padding) {
            writer.write(currOffset++, 0x00)
        }

        writeOffsetWide(writer, currOffset, defaultOffset)
        currOffset += 4
        writeLiteralWide(writer, currOffset, lowValue)
        currOffset += 4
        writeLiteralWide(writer, currOffset, highValue)
        currOffset += 4

        for (pair in matchOffsetPairs) {
            writeOffsetWide(writer, currOffset, pair.offset)
            currOffset += 4
        }
    }

    override fun accept(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, visitor: InstructionVisitor) {
        visitor.visitTableSwitchInstruction(classFile, method, code, offset, this)
    }

    companion object {
        internal fun create(opCode: JvmOpCode): JvmInstruction {
            return TableSwitchInstruction(opCode)
        }

        fun of(opCode: JvmOpCode, defaultOffset: Int, matchOffsetPairs: List<MatchOffsetPair>): TableSwitchInstruction {
            return TableSwitchInstruction(opCode, defaultOffset, null, matchOffsetPairs.toMutableList())
        }

        fun of(opCode: JvmOpCode, defaultLabel: String, matchOffsetPairs: List<MatchOffsetPair>): TableSwitchInstruction {
            return TableSwitchInstruction(opCode, -1, defaultLabel, matchOffsetPairs.toMutableList())
        }
    }
}