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

package com.github.netomi.bat.dexfile.instruction.editor

import com.github.netomi.bat.dexfile.editor.CodeEditor
import com.github.netomi.bat.dexfile.editor.DexEditor
import com.github.netomi.bat.dexfile.instruction.*

class InstructionBuilder private constructor(private val dexEditor: DexEditor) {

    private var instructionList = mutableListOf<DexInstruction>()

    private fun <T: DexInstruction> add(instruction: T): T {
        instructionList.add(instruction)
        return instruction
    }

    fun nop(): DexInstruction {
        return add(BasicInstruction.of(DexOpCode.NOP))
    }

    fun const(value: Long, vararg register: Int): LiteralInstruction {
        return add(LiteralInstruction.of(DexOpCode.CONST, value, *register))
    }

    fun newArray(type: String, vararg register: Int): TypeInstruction {
        return newArray(dexEditor.addOrGetTypeIDIndex(type), *register)
    }

    fun newArray(typeIndex: Int, vararg register: Int): TypeInstruction {
        return add(TypeInstruction.of(DexOpCode.NEW_ARRAY, typeIndex, *register))
    }

    fun fillArrayData(payload: FillArrayPayload, register: Int): PayloadInstruction<FillArrayPayload> {
        return add(FillArrayDataInstruction.of(payload, register))
    }

    fun returnVoid(): BasicInstruction {
        return add(BasicInstruction.of(DexOpCode.RETURN_VOID))
    }

    fun invokeDirect(methodIndex: Int, vararg registers: Int): MethodInstruction {
        return add(MethodInstruction.of(DexOpCode.INVOKE_DIRECT, methodIndex, *registers))
    }

    fun invokeDirect(classType: String, name: String, parameterTypes: List<String>, returnType: String, vararg registers: Int): MethodInstruction {
        return invokeDirect(dexEditor.addOrGetMethodIDIndex(classType, name, parameterTypes, returnType), *registers)
    }

    fun getInstructionSequence(): List<DexInstruction> {
        val list = instructionList
        instructionList = mutableListOf()
        return list
    }

    companion object {
        fun of(codeEditor: CodeEditor): InstructionBuilder {
            return InstructionBuilder(codeEditor.dexEditor)
        }

        fun of(dexEditor: DexEditor): InstructionBuilder {
            return InstructionBuilder(dexEditor)
        }
    }
}