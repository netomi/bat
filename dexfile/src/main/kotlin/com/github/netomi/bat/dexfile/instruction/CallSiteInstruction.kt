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

class CallSiteInstruction: DexInstruction {

    var callSiteIndex: Int = NO_INDEX
        internal set

    private constructor(opCode: DexOpCode): super(opCode)

    private constructor(opCode: DexOpCode, callSiteIndex: Int, vararg registers: Int): super(opCode, *registers) {
        require(callSiteIndex >= 0) { "callSiteIndex must not be negative for instruction ${opCode.mnemonic}" }
        this.callSiteIndex = callSiteIndex
    }

    fun getCallSiteID(dexFile: DexFile): CallSiteID {
        return dexFile.getCallSiteID(callSiteIndex)
    }

    override fun read(instructions: ShortArray, offset: Int) {
        super.read(instructions, offset)

        callSiteIndex = when (opCode.format) {
            FORMAT_3rc,
            FORMAT_35c -> instructions[offset + 1].toInt() and 0xffff

            else -> error("unexpected format '${opCode.format}' for opcode '${opCode.mnemonic}'")
        }
    }

    override fun writeData(): ShortArray {
        val data = super.writeData()

        when (opCode.format) {
            FORMAT_3rc,
            FORMAT_35c -> data[1] = callSiteIndex.toShort()

            else -> {}
        }
        return data
    }

    override fun accept(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, visitor: InstructionVisitor) {
        visitor.visitCallSiteInstruction(dexFile, classDef, method, code, offset, this)
    }

    override fun toString(): String {
        return super.toString() + ", callSite@${toHexString(callSiteIndex, 4)}"
    }

    companion object {
        fun of(opCode: DexOpCode, callSiteIndex: Int, vararg registers: Int): CallSiteInstruction {
            return CallSiteInstruction(opCode, callSiteIndex, *registers)
        }

        internal fun create(opCode: DexOpCode): CallSiteInstruction {
            return CallSiteInstruction(opCode)
        }
    }
}