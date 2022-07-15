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

class BasicInstruction internal constructor(opcode: DexOpCode, vararg registers: Int) : DexInstruction(opcode, *registers) {

    override fun read(instructions: ShortArray, offset: Int) {
        super.read(instructions, offset)
    }

    override fun accept(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, visitor: InstructionVisitor) {
        visitor.visitBasicInstruction(dexFile, classDef, method, code, offset, this)
    }

    companion object {
        fun of(opcode: DexOpCode, vararg registers: Int): BasicInstruction {
            return BasicInstruction(opcode, *registers)
        }

        @JvmStatic
        fun create(opCode: DexOpCode, ident: Byte): DexInstruction {
            return when (opCode) {
                DexOpCode.NOP -> {
                    if (ident.toInt() == 0x01) {
                        PackedSwitchPayload(opCode)
                    } else if (ident.toInt() == 0x02) {
                        SparseSwitchPayload(opCode)
                    } else if (ident.toInt() == 0x03) {
                        FillArrayPayload(opCode)
                    } else {
                        BasicInstruction(opCode)
                    }
                }
                else -> BasicInstruction(opCode)
            }
        }
    }
}