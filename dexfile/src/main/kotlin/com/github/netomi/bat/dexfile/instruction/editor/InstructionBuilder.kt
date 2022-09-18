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

    fun label(label: String): DexInstruction {
        return add(LabelInstruction.of(label))
    }

    fun nop(): NopInstruction {
        return add(NopInstruction.of(DexOpCode.NOP))
    }

    fun const(value: Long, destinationRegister: Int): LiteralInstruction {
        return add(LiteralInstruction.of(DexOpCode.CONST, value, destinationRegister))
    }

    fun const4(value: Long, destinationRegister: Int): LiteralInstruction {
        return add(LiteralInstruction.of(DexOpCode.CONST_4, value, destinationRegister))
    }

    fun const16(value: Long, destinationRegister: Int): LiteralInstruction {
        return add(LiteralInstruction.of(DexOpCode.CONST_16, value, destinationRegister))
    }

    fun newArray(type: String, destinationRegister: Int, sizeRegister: Int): TypeInstruction {
        return newArray(dexEditor.addOrGetTypeIDIndex(type), destinationRegister, sizeRegister)
    }

    fun newArray(typeIndex: Int, destinationRegister: Int, sizeRegister: Int): TypeInstruction {
        return add(TypeInstruction.of(DexOpCode.NEW_ARRAY, typeIndex, destinationRegister, sizeRegister))
    }

    fun fillArrayData(payload: FillArrayPayload, register: Int): PayloadInstruction<FillArrayPayload> {
        return add(FillArrayDataInstruction.of(payload, register))
    }

    fun arrayGetByte(destinationRegister: Int, arrayRegister: Int, indexRegister: Int): ArrayInstruction {
        return add(ArrayInstruction.of(DexOpCode.AGET_BYTE, destinationRegister, arrayRegister, indexRegister))
    }

    fun arrayPutByte(destinationRegister: Int, arrayRegister: Int, indexRegister: Int): ArrayInstruction {
        return add(ArrayInstruction.of(DexOpCode.APUT_BYTE, destinationRegister, arrayRegister, indexRegister))
    }

    fun returnVoid(): ReturnInstruction {
        return add(ReturnInstruction.of(DexOpCode.RETURN_VOID))
    }

    fun `return`(register: Int): ReturnInstruction {
        return add(ReturnInstruction.of(DexOpCode.RETURN, register))
    }

    fun invokeDirect(methodIndex: Int, vararg registers: Int): MethodInstruction {
        return add(MethodInstruction.of(DexOpCode.INVOKE_DIRECT, methodIndex, *registers))
    }

    fun invokeDirect(classType: String, name: String, parameterTypes: List<String>, returnType: String, vararg registers: Int): MethodInstruction {
        return invokeDirect(dexEditor.addOrGetMethodIDIndex(classType, name, parameterTypes, returnType), *registers)
    }

    fun invokeVirtual(methodIndex: Int, vararg registers: Int): MethodInstruction {
        return add(MethodInstruction.of(DexOpCode.INVOKE_VIRTUAL, methodIndex, *registers))
    }

    fun invokeVirtual(classType: String, name: String, parameterTypes: List<String>, returnType: String, vararg registers: Int): MethodInstruction {
        return invokeVirtual(dexEditor.addOrGetMethodIDIndex(classType, name, parameterTypes, returnType), *registers)
    }

    fun invokeStatic(methodIndex: Int, vararg registers: Int): MethodInstruction {
        return add(MethodInstruction.of(DexOpCode.INVOKE_STATIC, methodIndex, *registers))
    }

    fun invokeStatic(classType: String, name: String, parameterTypes: List<String>, returnType: String, vararg registers: Int): MethodInstruction {
        return invokeStatic(dexEditor.addOrGetMethodIDIndex(classType, name, parameterTypes, returnType), *registers)
    }

    fun staticGetObject(classType: String, name: String, type: String, register: Int): FieldInstruction {
        return add(FieldInstruction.of(DexOpCode.SGET_OBJECT, dexEditor.addOrGetFieldIDIndex(classType, name, type), register))
    }

    fun intToChar(destinationRegister: Int, sourceRegister: Int): ConversionInstruction {
        return add(ConversionInstruction.of(DexOpCode.INT_TO_CHAR, destinationRegister, sourceRegister))
    }

    fun intToByte(destinationRegister: Int, sourceRegister: Int): ConversionInstruction {
        return add(ConversionInstruction.of(DexOpCode.INT_TO_BYTE, destinationRegister, sourceRegister))
    }

    fun moveResult(register: Int): MoveInstruction {
        return add(MoveInstruction.of(DexOpCode.MOVE_RESULT, register))
    }

    fun addIntLit8(literal: Int, destinationRegister: Int, sourceRegister: Int): ArithmeticLiteralInstruction {
        return add(ArithmeticLiteralInstruction.of(DexOpCode.ADD_INT_LIT8, literal, destinationRegister, sourceRegister))
    }

    fun ifEqualZero(register: Int, label: String): BranchInstruction {
        return add(BranchInstruction.of(DexOpCode.IF_EQZ, label, register))
    }

    fun goto(label: String): BranchInstruction {
        return add(BranchInstruction.of(DexOpCode.GOTO, label))
    }

    fun goto16(label: String): BranchInstruction {
        return add(BranchInstruction.of(DexOpCode.GOTO_16, label))
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
