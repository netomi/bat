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
import com.github.netomi.bat.dexfile.instruction.InstructionFormat.*
import com.github.netomi.bat.dexfile.instruction.visitor.InstructionVisitor

open class ArithmeticInstruction: DexInstruction {

    protected constructor(opCode: DexOpCode): super(opCode)

    protected constructor(opCode: DexOpCode, vararg registers: Int): super(opCode, *registers)

    override fun read(instructions: ShortArray, offset: Int) {
        super.read(instructions, offset)

        when (opCode.format) {
            FORMAT_12x,
            FORMAT_23x -> {}

            FORMAT_22b,
            FORMAT_22s -> {}

            else -> throw IllegalStateException("unexpected format ${opCode.format} for opcode ${opCode.mnemonic}")
        }
    }

    override fun accept(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, visitor: InstructionVisitor) {
        visitor.visitArithmeticInstruction(dexFile, classDef, method, code, offset, this)
    }

    companion object {
        fun of(opcode: DexOpCode, vararg registers: Int): ArithmeticInstruction {
            return ArithmeticInstruction(opcode, *registers)
        }

        internal fun create(opCode: DexOpCode): ArithmeticInstruction {
            return ArithmeticInstruction(opCode)
        }
    }
}