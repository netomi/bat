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

    fun parseConversionInstructions(ctx: FConversionInstructionsContext): ConversionInstruction {
        val mnemonic = ctx.op.text
        val opcode   = JvmOpCode[mnemonic]

        return ConversionInstruction.of(opcode)
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

    fun parseArrayInstructions(ctx: FArrayInstructionsContext): ArrayInstruction {
        val mnemonic = ctx.op.text
        val opcode   = JvmOpCode[mnemonic]

        return ArrayInstruction.of(opcode)
    }

    fun parseExceptionInstructions(ctx: FExceptionInstructionsContext): ExceptionInstruction {
        val mnemonic = ctx.op.text
        val opcode   = JvmOpCode[mnemonic]

        return ExceptionInstruction.of(opcode)
    }

    fun parseNullReferenceInstructions(ctx: FNullReferenceInstructionsContext): NullReferenceInstruction {
        val mnemonic = ctx.op.text
        val opcode   = JvmOpCode[mnemonic]

        return NullReferenceInstruction.of(opcode)
    }

    fun parseReturnInstructions(ctx: FReturnInstructionsContext): ReturnInstruction {
        val mnemonic = ctx.op.text
        val opcode   = JvmOpCode[mnemonic]

        return ReturnInstruction.of(opcode)
    }

    fun parseMonitorInstructions(ctx: FMonitorInstructionsContext): MonitorInstruction {
        val mnemonic = ctx.op.text
        val opcode   = JvmOpCode[mnemonic]

        return MonitorInstruction.of(opcode)
    }

    fun parseCompareInstructions(ctx: FCompareInstructionsContext): CompareInstruction {
        val mnemonic = ctx.op.text
        val opcode   = JvmOpCode[mnemonic]

        return CompareInstruction.of(opcode)
    }

    fun parseFieldInstructions(ctx: FFieldInstructionsContext): FieldInstruction {
        val mnemonic = ctx.op.text
        val opCode   = JvmOpCode[mnemonic]

        val field = ctx.fld.text
        val (classType, fieldName, fieldType) = parseFieldObject(field)

        val fieldRefConstantIndex = constantPoolEditor.addOrGetFieldRefConstantIndex(classType!!, fieldName, fieldType)
        return FieldInstruction.of(opCode, fieldRefConstantIndex)
    }

    fun parseMethodInstructions(ctx: FMethodInstructionsContext): MethodInstruction {
        val mnemonic = ctx.op.text
        val opCode   = JvmOpCode[mnemonic]

        val method = ctx.method.text
        val (className, methodName, descriptor) = parseSimpleMethodObject(method)

        val methodRefConstantIndex = constantPoolEditor.addOrGetMethodRefConstantIndex(className!!, methodName, descriptor)
        return MethodInstruction.of(opCode, methodRefConstantIndex)
    }

    fun parseClassInstructions(ctx: FClassInstructionsContext): ClassInstruction {
        val mnemonic = ctx.op.text
        val opCode   = JvmOpCode[mnemonic]

        val className = ctx.className.text

        val classConstantIndex = constantPoolEditor.addOrGetClassConstantIndex(className)
        return ClassInstruction.of(opCode, classConstantIndex)
    }

    fun parseArrayClassInstructions(ctx: FArrayClassInstructionsContext): ArrayClassInstruction {
        val mnemonic = ctx.op.text
        val opCode   = JvmOpCode[mnemonic]

        val className = ctx.className.text

        val classConstantIndex = constantPoolEditor.addOrGetClassConstantIndex(className)
        return ArrayClassInstruction.of(opCode, classConstantIndex)
    }

    fun parseMultiArrayClassInstructions(ctx: FMultiArrayClassInstructionContext): ArrayClassInstruction {
        val mnemonic = ctx.op.text
        val opCode   = JvmOpCode[mnemonic]

        val className = ctx.className.text
        val dimension = ctx.dimension.text.toInt()

        val classConstantIndex = constantPoolEditor.addOrGetClassConstantIndex(className)
        return ArrayClassInstruction.of(opCode, classConstantIndex, dimension)
    }

    fun parseBranchInstructions(ctx: FBranchInstructionsContext): BranchInstruction {
        val mnemonic = ctx.op.text
        val opCode   = JvmOpCode[mnemonic]

        val label = ctx.label.label.text
        return BranchInstruction.of(opCode, label)
    }

    fun parseImplicitLiteralInstructions(ctx: FImplicitLiteralInstructionsContext): LiteralInstruction {
        val mnemonic = ctx.op.text
        val opCode   = JvmOpCode[mnemonic]

        return LiteralInstruction.of(opCode)
    }

    fun parseExplicitLiteralInstructions(ctx: FExplicitLiteralInstructionContext): LiteralInstruction {
        val mnemonic = ctx.op.text
        val opCode   = JvmOpCode[mnemonic]

        val value = ctx.value.text.toLong()

        return LiteralInstruction.of(opCode, value)
    }

}