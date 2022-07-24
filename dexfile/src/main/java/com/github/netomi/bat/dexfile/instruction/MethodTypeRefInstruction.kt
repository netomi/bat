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

class MethodTypeRefInstruction internal constructor(       opcode:      DexOpCode,
                                                           _protoIndex: Int = NO_INDEX,
                                                    vararg registers:   Int) : DexInstruction(opcode, *registers) {

    var protoIndex: Int = _protoIndex
        internal set

    fun getProtoID(dexFile: DexFile): ProtoID {
        return dexFile.getProtoID(protoIndex)
    }

    override fun read(instructions: ShortArray, offset: Int) {
        super.read(instructions, offset)

        protoIndex = when (opcode.format) {
            FORMAT_21c -> instructions[offset + 1].toInt() and 0xffff

            else -> throw IllegalStateException("unexpected format for opcode " + opcode.mnemonic)
        }
    }

    override fun writeData(): ShortArray {
        val data = super.writeData()

        when (opcode.format) {
            FORMAT_21c -> data[1] = protoIndex.toShort()

            else -> {}
        }
        return data
    }

    override fun accept(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, visitor: InstructionVisitor) {
        visitor.visitMethodTypeRefInstruction(dexFile, classDef, method, code, offset, this)
    }

    companion object {
        fun of(opCode: DexOpCode, protoIndex: Int, vararg registers: Int): MethodTypeRefInstruction {
            return MethodTypeRefInstruction(opCode, protoIndex, *registers)
        }

        @JvmStatic
        fun create(opCode: DexOpCode, ident: Byte): MethodTypeRefInstruction {
            return MethodTypeRefInstruction(opCode)
        }
    }
}