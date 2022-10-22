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

package com.github.netomi.bat.jasm.assemble

import com.github.netomi.bat.classfile.constant.editor.ConstantPoolEditor
import com.github.netomi.bat.classfile.instruction.*
import com.github.netomi.bat.jasm.parser.JasmParser.*

internal class InstructionAssembler constructor(private val constantPoolEditor: ConstantPoolEditor) {

    fun parseArithmeticInstructions(ctx: FArithmeticInstructionsContext): ArithmeticInstruction {
        val mnemonic = ctx.op.text
        val opcode   = JvmOpCode[mnemonic]

        return ArithmeticInstruction.of(opcode)
    }

    fun parseStackInstructions(ctx: FStackInstructionsContext): StackInstruction {
        val mnemonic = ctx.op.text
        val opcode   = JvmOpCode[mnemonic]

        return StackInstruction.of(opcode)
    }

    fun parseImplicitVariableInstructions(ctx: FImplicitVariableInstructionsContext): VariableInstruction {
        val mnemonic = ctx.op.text
        val opcode   = JvmOpCode[mnemonic]

        return VariableInstruction.of(opcode)
    }

    fun parseExplicitVariableInstructions(ctx: FExplicitVariableInstructionsContext): VariableInstruction {
        val mnemonic = ctx.op.text
        val opcode   = JvmOpCode[mnemonic]

        val variable = ctx.variable.text.toInt()

        return VariableInstruction.of(opcode, variable)
    }

    fun parseFieldInstructions(ctx: FFieldInstructionsContext): FieldInstruction {
        val mnemonic = ctx.op.text
        val opCode   = JvmOpCode[mnemonic]

        val field = ctx.fld.text
        val (classType, fieldName, fieldType) = parseFieldObject(field)

        val fieldRefConstantIndex = constantPoolEditor.addOrGetFieldRefConstantIndex(classType!!, fieldName, fieldType)
        return FieldInstruction.of(opCode, fieldRefConstantIndex)
    }
}