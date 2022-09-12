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
import com.github.netomi.bat.dexfile.instruction.visitor.InstructionVisitor

class ArrayTypeInstruction: TypeInstruction {

    private constructor(opCode: DexOpCode): super(opCode)

    private constructor(opCode: DexOpCode, typeIndex: Int, vararg registers: Int): super(opCode, typeIndex, *registers)

    override fun accept(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, visitor: InstructionVisitor) {
        visitor.visitArrayTypeInstruction(dexFile, classDef, method, code, offset, this)
    }

    companion object {
        fun of(opCode: DexOpCode, typeIndex: Int, vararg registers: Int): ArrayTypeInstruction {
            return ArrayTypeInstruction(opCode, typeIndex, *registers)
        }

        internal fun create(opCode: DexOpCode): ArrayTypeInstruction {
            return ArrayTypeInstruction(opCode)
        }
    }
}