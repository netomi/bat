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

class MethodHandleRefInstruction private constructor(       opcode:            DexOpCode,
                                                            methodHandleIndex: Int = NO_INDEX,
                                                     vararg registers:         Int) : DexInstruction(opcode, *registers) {

    var methodHandleIndex: Int = methodHandleIndex
        internal set

    fun getMethodHandle(dexFile: DexFile): MethodHandle {
        return dexFile.getMethodHandle(methodHandleIndex)
    }

    override fun read(instructions: ShortArray, offset: Int) {
        super.read(instructions, offset)

        methodHandleIndex = when (opCode.format) {
            FORMAT_21c -> instructions[offset + 1].toInt() and 0xffff

            else -> throw IllegalStateException("unexpected format ${opCode.format} for opcode ${opCode.mnemonic}")
        }
    }

    override fun writeData(): ShortArray {
        val data = super.writeData()

        when (opCode.format) {
            FORMAT_21c -> data[1] = methodHandleIndex.toShort()

            else -> {}
        }
        return data
    }

    override fun accept(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, visitor: InstructionVisitor) {
        visitor.visitMethodHandleRefInstruction(dexFile, classDef, method, code, offset, this)
    }

    companion object {
        fun of(opCode: DexOpCode, methodHandleIndex: Int, vararg registers: Int): MethodHandleRefInstruction {
            return MethodHandleRefInstruction(opCode, methodHandleIndex, *registers)
        }

        internal fun create(opCode: DexOpCode): MethodHandleRefInstruction {
            return MethodHandleRefInstruction(opCode)
        }
    }
}