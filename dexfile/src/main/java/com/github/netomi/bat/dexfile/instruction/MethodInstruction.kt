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

import com.github.netomi.bat.dexfile.*
import com.github.netomi.bat.dexfile.instruction.InstructionFormat.*
import com.github.netomi.bat.dexfile.instruction.visitor.InstructionVisitor
import com.github.netomi.bat.util.toHexString

open class MethodInstruction internal constructor(opcode: DexOpCode, _methodIndex: Int = NO_INDEX, vararg registers: Int) : DexInstruction(opcode, *registers) {

    var methodIndex: Int = _methodIndex
        internal set

    fun getMethodID(dexFile: DexFile): MethodID {
        return dexFile.getMethodID(methodIndex)
    }

    override fun read(instructions: ShortArray, offset: Int) {
        super.read(instructions, offset)

        methodIndex = when (opCode.format) {
            FORMAT_3rc,
            FORMAT_35c,
            FORMAT_45cc,
            FORMAT_4rcc -> instructions[offset + 1].toInt() and 0xffff

            else -> throw IllegalStateException("unexpected format ${opCode.format} for opcode ${opCode.mnemonic}")
        }
    }

    override fun writeData(): ShortArray {
        val data = super.writeData()

        when (opCode.format) {
            FORMAT_3rc,
            FORMAT_35c,
            FORMAT_45cc,
            FORMAT_4rcc -> data[1] = methodIndex.toShort()

            else -> {}
        }
        return data
    }

    override fun accept(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, visitor: InstructionVisitor) {
        visitor.visitMethodInstruction(dexFile, classDef, method, code, offset, this)
    }

    override fun toString(): String {
        return super.toString() + ", method@${toHexString(methodIndex, 4)}"
    }

    companion object {
        fun of(opCode: DexOpCode, methodIndex: Int, vararg registers: Int): MethodInstruction {
            return MethodInstruction(opCode, methodIndex, *registers)
        }

        fun create(opCode: DexOpCode): MethodInstruction {
            return MethodInstruction(opCode)
        }
    }
}