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

import com.github.netomi.bat.classfile.attribute.ExceptionEntry
import com.github.netomi.bat.classfile.constant.editor.ConstantPoolEditor
import com.github.netomi.bat.classfile.instruction.*
import com.github.netomi.bat.jasm.parser.JasmParser.*
import com.github.netomi.bat.util.getArgumentSize
import com.github.netomi.bat.util.parseDescriptorToJvmTypes

internal class InstructionAssembler constructor(private val constantPoolEditor: ConstantPoolEditor) {

    private val constantAssembler = ConstantAssembler(constantPoolEditor)

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

    fun parseInterfaceMethodInstructions(ctx: FInterfaceMethodInstructionsContext): InterfaceMethodInstruction {
        val mnemonic = ctx.op.text
        val opCode   = JvmOpCode[mnemonic]

        val method = ctx.method.text
        val (className, methodName, descriptor) = parseSimpleMethodObject(method)

        val methodRefConstantIndex = constantPoolEditor.addOrGetInterfaceMethodRefConstantIndex(className!!, methodName, descriptor)

        val (parameterTypes, _) = parseDescriptorToJvmTypes(descriptor)
        val argumentCount = 1 + parameterTypes.getArgumentSize()

        return InterfaceMethodInstruction.of(opCode, methodRefConstantIndex, argumentCount)
    }

    fun parseInvokeDynamicInstruction(ctx: FInvokeDynamicInstructionsContext): InvokeDynamicInstruction {
        val mnemonic = ctx.op.text
        val opCode   = JvmOpCode[mnemonic]

        val bootstrapMethodId = ctx.ID().text
        val underscoreIndex   = bootstrapMethodId.lastIndexOf('_')
        if (underscoreIndex == -1) {
            parserError(ctx, "failed to parse bootstrap method index '$bootstrapMethodId'")
        }
        val bootstrapMethodAttrIndex = bootstrapMethodId.substring(underscoreIndex + 1).toInt()

        val method = ctx.method.text
        val (_, methodName, descriptor) = parseSimpleMethodObject(method)

        val invokeDynamicConstantIndex = constantPoolEditor.addOrGetInvokeDynamicConstantIndex(bootstrapMethodAttrIndex, methodName, descriptor)

        return InvokeDynamicInstruction.of(opCode, invokeDynamicConstantIndex)
    }

    fun parseClassInstructions(ctx: FClassInstructionsContext): ClassInstruction {
        val mnemonic = ctx.op.text
        val opCode   = JvmOpCode[mnemonic]

        val className = ctx.className.text

        val classConstantIndex = constantPoolEditor.addOrGetClassConstantIndex(className)
        return ClassInstruction.of(opCode, classConstantIndex)
    }

    fun parsePrimitiveArrayInstructions(ctx: FPrimitiveArrayInstructionsContext): ArrayPrimitiveTypeInstruction {
        val mnemonic = ctx.op.text
        val opCode   = JvmOpCode[mnemonic]

        val primitiveType = ctx.type.text

        return ArrayPrimitiveTypeInstruction.of(opCode, primitiveType)
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

    fun parseLiteralConstantInstructions(ctx: FLiteralConstantInstructionsContext): LiteralConstantInstruction {
        val mnemonic = ctx.op.text
        var opCode   = JvmOpCode[mnemonic]

        val constantIndex = constantAssembler.parseBaseValue(ctx.value)

        // TODO: make this clean by having a general mechanism for widening / shrinking instructions
        if (opCode == JvmOpCode.LDC && constantIndex > 0xff) {
            opCode = JvmOpCode.LDC_W
        }

        return LiteralConstantInstruction.of(opCode, constantIndex)
    }

    fun parseWideLiteralConstantInstructions(ctx: FWideLiteralConstantInstructionsContext): LiteralConstantInstruction {
        val mnemonic = ctx.op.text
        val opCode   = JvmOpCode[mnemonic]

        val constantIndex = constantAssembler.parseBaseValue(ctx.value)

        return LiteralConstantInstruction.of(opCode, constantIndex)
    }

    fun parseLiteralVariableInstructions(ctx: FLiteralVariableInstructionsContext): LiteralVariableInstruction {
        val mnemonic = ctx.op.text
        val opCode   = JvmOpCode[mnemonic]

        val variable = ctx.variable.text.toInt()
        val value    = ctx.value.text.toInt()

        return LiteralVariableInstruction.of(opCode, variable, value)
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

    fun parseLookupSwitchInstruction(ctx: FLookupSwitchContext): LookupSwitchInstruction {
        val mnemonic = ctx.op.text
        val opCode   = JvmOpCode[mnemonic]

        val matchOffsetPairs = mutableListOf<MatchOffsetPair>()
        var defaultLabel: String? = null

        for (i in 0 until ctx.sSwitchKey().size) {
            val key   = ctx.sSwitchKey(i).text
            val label = ctx.sLabel(i).label.text

            if (key != "default") {
                matchOffsetPairs.add(MatchOffsetPair(key.toInt(), -1, label))
            } else {
                defaultLabel = label
            }
        }

        return LookupSwitchInstruction.of(opCode, defaultLabel ?: parserError(ctx, "missing 'default' label"), matchOffsetPairs)
    }

    fun parseTableSwitchInstruction(ctx: FTableSwitchContext): TableSwitchInstruction {
        val mnemonic = ctx.op.text
        val opCode   = JvmOpCode[mnemonic]

        val matchOffsetPairs = mutableListOf<MatchOffsetPair>()
        var defaultLabel: String? = null

        for (i in 0 until ctx.sSwitchKey().size) {
            val key   = ctx.sSwitchKey(i).text
            val label = ctx.sLabel(i).label.text

            if (key != "default") {
                matchOffsetPairs.add(MatchOffsetPair(key.toInt(), -1, label))
            } else {
                defaultLabel = label
            }
        }

        return TableSwitchInstruction.of(opCode, defaultLabel ?: parserError(ctx, "missing 'default' label"), matchOffsetPairs)
    }

    fun parseCatchDirective(ctx: FCatchContext): ExceptionEntry {
        val exceptionClass = ctx.type.text
        val exceptionClassIndex = constantPoolEditor.addOrGetClassConstantIndex(exceptionClass)

        val startLabel   = ctx.start.label.text
        val endLabel     = ctx.end.label.text
        val handlerLabel = ctx.handle.label.text

        return ExceptionEntry.of(startLabel, endLabel, handlerLabel, exceptionClassIndex)
    }

    fun parseCatchAllDirective(ctx: FCatchallContext): ExceptionEntry {
        val startLabel   = ctx.start.label.text
        val endLabel     = ctx.end.label.text
        val handlerLabel = ctx.handle.label.text

        return ExceptionEntry.of(startLabel, endLabel, handlerLabel)
    }

}