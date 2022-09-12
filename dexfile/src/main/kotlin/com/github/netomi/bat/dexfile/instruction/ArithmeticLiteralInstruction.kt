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
import com.github.netomi.bat.util.toSignedHexStringWithPrefix

class ArithmeticLiteralInstruction: LiteralInstruction {

    private constructor(opCode: DexOpCode): super(opCode)

    private constructor(opCode: DexOpCode, literal: Int, rA: Int, rB: Int): super(opCode, literal.toLong(), rA, rB) {
        when (opCode.format) {
            FORMAT_22b -> checkRange(literal, -0x80, 0x7f, opCode)
            FORMAT_22s -> checkRange(literal, -0x8000, 0x7fff, opCode)
            else -> {}
        }
    }

    override fun accept(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, visitor: InstructionVisitor) {
        visitor.visitArithmeticLiteralInstruction(dexFile, classDef, method, code, offset, this)
    }

    companion object {
        private fun checkRange(value: Int, minValue: Int, maxValue: Int, opCode: DexOpCode) {
            if (value < minValue || value > maxValue) {
                throw IllegalArgumentException("literal value '%s' exceeds allowed range [%s, %s] for opcode '%s'"
                    .format(
                        toSignedHexStringWithPrefix(value),
                        toSignedHexStringWithPrefix(minValue),
                        toSignedHexStringWithPrefix(maxValue),
                        opCode.mnemonic))
            }
        }

        fun of(opCode: DexOpCode, literal: Int, rA: Int, rB: Int): ArithmeticLiteralInstruction {
            return ArithmeticLiteralInstruction(opCode, literal, rA, rB)
        }

        internal fun create(opCode: DexOpCode): ArithmeticLiteralInstruction {
            return ArithmeticLiteralInstruction(opCode)
        }
    }
}