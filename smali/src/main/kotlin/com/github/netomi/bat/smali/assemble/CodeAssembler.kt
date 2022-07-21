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

package com.github.netomi.bat.smali.assemble

import com.github.netomi.bat.dexfile.*
import com.github.netomi.bat.dexfile.editor.DexComposer
import com.github.netomi.bat.dexfile.instruction.*
import com.github.netomi.bat.dexfile.io.InstructionWriter
import com.github.netomi.bat.dexfile.util.DexClasses
import com.github.netomi.bat.smali.parser.SmaliParser.*
import com.github.netomi.bat.util.toIntArray
import org.antlr.v4.runtime.ParserRuleContext

internal class CodeAssembler constructor(private val classDef:    ClassDef,
                                         private val method:      EncodedMethod,
                                         private val dexComposer: DexComposer) {

    private val dexFile: DexFile
        get() = dexComposer.dexFile

    private lateinit var registerInfo: RegisterInfo
    private          var labelMapping: MutableMap<String, Int> = HashMap()

    fun parseCode(iCtx: List<SInstructionContext>, pCtx: List<SParameterContext>): Code {

        val instructions = mutableListOf<DexInstruction>()

        collectRegisterInfo(iCtx)
        collectLabels(iCtx)

        var codeOffset = 0

        iCtx.forEach { ctx ->
            val t = ctx.getChild(0) as ParserRuleContext
            val insn: DexInstruction? = when (t.ruleIndex) {
                RULE_fline -> {
                    val c = t as FlineContext
                    val lineNumber = c.line.text.toInt()
                    null
                }

                RULE_fprologue  -> null
                RULE_fepilogue  -> null
                RULE_fregisters -> null
                RULE_sLabel     -> null

                RULE_f10x -> {
                    val c = t as F10xContext

                    val mnemonic = c.op.text
                    val opcode = DexOpCode.get(mnemonic)

                    opcode.createInstruction(0)
                }

                RULE_f12x_conversion   -> parseConversionInstructionF12x(t as F12x_conversionContext)
                RULE_f11x_basic        -> parseBasicInstructionF11x(t as F11x_basicContext)
                RULE_fx0t_branch       -> parseBranchInstructionFx0t(t as Fx0t_branchContext, codeOffset)
                RULE_f21t_branch       -> parseBranchInstructionF21t(t as F21t_branchContext, codeOffset)
                RULE_f22t_branch       -> parseBranchInstructionF22t(t as F22t_branchContext, codeOffset)
                RULE_f21c_field        -> parseFieldInstructionF21c(t as F21c_fieldContext)
                RULE_f22c_field        -> parseFieldInstructionF22c(t as F22c_fieldContext)
                RULE_fconst_int        -> parseLiteralInstructionInteger(t as Fconst_intContext)
                RULE_fconst_string     -> parseLiteralInstructionString(t as Fconst_stringContext)
                RULE_fconst_type       -> parseLiteralInstructionType(t as Fconst_typeContext)
                RULE_f12x_arithmetic   -> parseArithmeticInstructionF12x(t as F12x_arithmeticContext)
                RULE_f23x_arithmetic   -> parseArithmeticInstructionF23x(t as F23x_arithmeticContext)
                RULE_f22sb_arithmetic  -> parseArithmeticLiteralInstructionF22sb(t as F22sb_arithmeticContext)
                RULE_fx2x_move         -> parseBasicInstructionMoveFx2x(t as Fx2x_moveContext)
                RULE_f12x_array        -> parseArrayInstructionF12x(t as F12x_arrayContext)
                RULE_ft2c_type         -> parseTypeInstructionFt2c(t as Ft2c_typeContext)
                RULE_f23x_compare      -> parseBasicInstructionCompareF23x(t as F23x_compareContext)
                RULE_f23x_array        -> parseArrayInstructionF23x(t as F23x_arrayContext)
                RULE_f35c_method       -> parseMethodInstructionF35c(t as F35c_methodContext)
                RULE_f3rc_method       -> parseMethodInstructionF3rc(t as F3rc_methodContext)
                RULE_f35c_array        -> parseArrayTypeInstructionF35c(t as F35c_arrayContext)
                RULE_f3rc_array        -> parseArrayTypeInstructionF3rc(t as F3rc_arrayContext)
                RULE_f45cc_methodproto -> parseMethodProtoInstructionF45cc(t as F45cc_methodprotoContext)
                RULE_f4rcc_methodproto -> parseMethodProtoInstructionF4rcc(t as F4rcc_methodprotoContext)
                RULE_f21c_const_handle -> parseMethodHandleInstructionF21c(t as F21c_const_handleContext)
                RULE_f21c_const_type   -> parseMethodTypeInstructionF21c(t as F21c_const_typeContext)
                RULE_f31t_payload      -> parsePayloadInstructionF31t(t as F31t_payloadContext, codeOffset)

                else -> null //parserError(t, "unexpected instruction")
            }

            insn?.apply {
                codeOffset += length
                instructions.add(this)
            }
        }

        val code = Code.of(registerInfo.registers, registerInfo.insSize, 0)

        val insns = InstructionWriter.writeInstructions(instructions)

        code.insns     = insns
        code.insnsSize = insns.size

        val outgoingArgumentSizeCalculator = OutgoingArgumentSizeCalculator()
        code.instructionsAccept(dexFile, classDef, method, code, outgoingArgumentSizeCalculator)
        code.outsSize = outgoingArgumentSizeCalculator.outgoingArgumentSize

        return code
    }

    private fun collectRegisterInfo(listCtx: List<SInstructionContext>) {

        val protoID = method.getMethodID(dexFile).getProtoID(dexFile)
        var insSize = if (method.isStatic) 0 else 1
        val argumentSize = DexClasses.getArgumentSize(protoID.parameters.getTypes(dexFile))
        insSize += argumentSize

        listCtx.forEach { ctx ->
            val t = ctx.getChild(0) as ParserRuleContext
            when (t.ruleIndex) {
                RULE_fregisters -> {
                    val c = t as FregistersContext
                    val registers = c.xregisters.text.toInt()
                    registerInfo = RegisterInfo(registers, registers - insSize, insSize)
                    return
                }

                RULE_flocals -> {
                    val c = t as FlocalsContext
                    val locals = c.xlocals.text.toInt()
                    registerInfo = RegisterInfo(locals + insSize, locals, insSize)
                    return
                }

                else -> {}
            }
        }
    }

    private fun collectLabels(listCtx: List<SInstructionContext>) {
        labelMapping.clear()
        var codeOffset = 0

        listCtx.forEach { ctx ->
            val t = ctx.getChild(0) as ParserRuleContext
            when (t.ruleIndex) {
                RULE_sLabel -> {
                    val c = t as SLabelContext
                    labelMapping[c.label.text] = codeOffset
                }

                else -> {
                    // check if its a known instruction and advance the code offset
                    val mnemonic = t.getChild(0).text
                    val opcode   = DexOpCode.get(mnemonic)
                    if (opcode != null) {
                        val insn = opcode.createInstruction(0)
                        codeOffset += insn.length
                    }
                }
            }
        }
    }

    private fun parseConversionInstructionF12x(ctx: F12x_conversionContext): ConversionInstruction {
        val mnemonic = ctx.op.text
        val opcode = DexOpCode.get(mnemonic)

        val r1 = registerInfo.registerNumber(ctx.r1.text)
        val r2 = registerInfo.registerNumber(ctx.r2.text)

        return ConversionInstruction.of(opcode, r1, r2)
    }

    private fun parseBasicInstructionF11x(ctx: F11x_basicContext): BasicInstruction {
        val mnemonic = ctx.op.text
        val opcode = DexOpCode.get(mnemonic)

        val r1 = registerInfo.registerNumber(ctx.r1.text)

        return BasicInstruction.of(opcode, r1)
    }

    private fun parseBranchInstructionFx0t(ctx: Fx0t_branchContext, codeOffset: Int): BranchInstruction {
        val mnemonic = ctx.op.text
        val opcode   = DexOpCode.get(mnemonic)

        val label = ctx.target.text
        val branchOffset = branchOffset(codeOffset, label)

        return BranchInstruction.of(opcode, branchOffset)
    }

    private fun parseBranchInstructionF21t(ctx: F21t_branchContext, codeOffset: Int): BranchInstruction {
        val mnemonic = ctx.op.text
        val opcode   = DexOpCode.get(mnemonic)

        val label = ctx.label.text
        val branchOffset = branchOffset(codeOffset, label)
        val r1 = registerInfo.registerNumber(ctx.r1.text)

        return BranchInstruction.of(opcode, branchOffset, r1)
    }

    private fun parseBranchInstructionF22t(ctx: F22t_branchContext, codeOffset: Int): BranchInstruction {
        val mnemonic = ctx.op.text
        val opcode   = DexOpCode.get(mnemonic)

        val label = ctx.label.text
        val branchOffset = branchOffset(codeOffset, label)
        val r1 = registerInfo.registerNumber(ctx.r1.text)
        val r2 = registerInfo.registerNumber(ctx.r2.text)

        return BranchInstruction.of(opcode, branchOffset, r1, r2)
    }

    private fun parseFieldInstructionF21c(ctx: F21c_fieldContext): FieldInstruction {
        val mnemonic = ctx.op.text
        val opcode   = DexOpCode.get(mnemonic)

        val register = registerInfo.registerNumber(ctx.r1.text)

        val field = ctx.fld.text
        val (classType, fieldName, fieldType) = parseFieldObject(field)

        val fieldIndex = dexComposer.addOrGetFieldIDIndex(classType!!, fieldName, fieldType)
        return FieldInstruction.of(opcode, fieldIndex, register)
    }

    private fun parseFieldInstructionF22c(ctx: F22c_fieldContext): FieldInstruction {
        val mnemonic = ctx.op.text
        val opcode = DexOpCode.get(mnemonic)

        val r1 = registerInfo.registerNumber(ctx.r1.text)
        val r2 = registerInfo.registerNumber(ctx.r2.text)

        val field = ctx.fld.text
        val (classType, fieldName, fieldType) = parseFieldObject(field)

        val fieldIndex = dexComposer.addOrGetFieldIDIndex(classType!!, fieldName, fieldType)
        return FieldInstruction.of(opcode, fieldIndex, r1, r2)
    }

    private fun parseLiteralInstructionInteger(ctx: Fconst_intContext): LiteralInstruction {
        val mnemonic = ctx.op.text
        val opcode = DexOpCode.get(mnemonic)

        val r1 = registerInfo.registerNumber(ctx.r1.text)
        val value = parseNumber(ctx.cst.text)

        return LiteralInstruction.of(opcode, value, r1)
    }

    private fun parseLiteralInstructionString(ctx: Fconst_stringContext): StringInstruction {
        val mnemonic = ctx.op.text
        val opcode = DexOpCode.get(mnemonic)

        val r1 = registerInfo.registerNumber(ctx.r1.text)
        val stringIndex = dexComposer.addOrGetStringIDIndex(parseString(ctx.cst.text))

        return StringInstruction.of(opcode, stringIndex, r1)
    }

    private fun parseLiteralInstructionType(ctx: Fconst_typeContext): TypeInstruction {
        val mnemonic = ctx.op.text
        val opcode = DexOpCode.get(mnemonic)

        val r1 = registerInfo.registerNumber(ctx.r1.text)
        val typeIndex = dexComposer.addOrGetTypeIDIndex(ctx.cst.text)

        return TypeInstruction.of(opcode, typeIndex, r1)
    }

    private fun parseArithmeticInstructionF12x(ctx: F12x_arithmeticContext): ArithmeticInstruction {
        val mnemonic = ctx.op.text
        val opcode = DexOpCode.get(mnemonic)

        val r1 = registerInfo.registerNumber(ctx.r1.text)
        val r2 = registerInfo.registerNumber(ctx.r2.text)

        return ArithmeticInstruction.of(opcode, r1, r2)
    }

    private fun parseArithmeticInstructionF23x(ctx: F23x_arithmeticContext): ArithmeticInstruction {
        val mnemonic = ctx.op.text
        val opcode = DexOpCode.get(mnemonic)

        val r1 = registerInfo.registerNumber(ctx.r1.text)
        val r2 = registerInfo.registerNumber(ctx.r2.text)
        val r3 = registerInfo.registerNumber(ctx.r3.text)

        return ArithmeticInstruction.of(opcode, r1, r2, r3)
    }

    private fun parseArithmeticLiteralInstructionF22sb(ctx: F22sb_arithmeticContext): ArithmeticInstruction {
        val mnemonic = ctx.op.text
        val opcode = DexOpCode.get(mnemonic)

        val r1 = registerInfo.registerNumber(ctx.r1.text)
        val r2 = registerInfo.registerNumber(ctx.r2.text)
        val value = parseNumber(ctx.lit.text).toInt()

        return ArithmeticLiteralInstruction.of(opcode, value, r1, r2)
    }

    private fun parseBasicInstructionCompareF23x(ctx: F23x_compareContext): BasicInstruction {
        val mnemonic = ctx.op.text
        val opcode = DexOpCode.get(mnemonic)

        val r1 = registerInfo.registerNumber(ctx.r1.text)
        val r2 = registerInfo.registerNumber(ctx.r2.text)
        val r3 = registerInfo.registerNumber(ctx.r3.text)

        return BasicInstruction.of(opcode, r1, r2, r3)
    }

    private fun parseBasicInstructionMoveFx2x(ctx: Fx2x_moveContext): BasicInstruction {
        val mnemonic = ctx.op.text
        val opcode = DexOpCode.get(mnemonic)

        val r1 = registerInfo.registerNumber(ctx.r1.text)
        val r2 = registerInfo.registerNumber(ctx.r2.text)

        return BasicInstruction.of(opcode, r1, r2)
    }

    private fun parseMethodInstructionF35c(ctx: F35c_methodContext): MethodInstruction {
        val mnemonic = ctx.op.text
        val opcode = DexOpCode.get(mnemonic)
        val registers = ctx.REGISTER().map { registerInfo.registerNumber(it.text) }.toIntArray()

        val methodType = ctx.method.text

        val (classType, methodName, parameterTypes, returnType) = parseMethodObject(methodType)
        val methodIndex = dexComposer.addOrGetMethodIDIndex(classType!!, methodName, parameterTypes, returnType)

        return MethodInstruction.of(opcode, methodIndex, *registers)
    }

    private fun parseMethodInstructionF3rc(ctx: F3rc_methodContext): MethodInstruction {
        val mnemonic = ctx.op.text
        val opcode = DexOpCode.get(mnemonic)

        val rStart = registerInfo.registerNumber(ctx.rstart.text)
        val rEnd   = registerInfo.registerNumber(ctx.rend.text)

        val methodType = ctx.method.text

        val (classType, methodName, parameterTypes, returnType) = parseMethodObject(methodType)
        val methodIndex = dexComposer.addOrGetMethodIDIndex(classType!!, methodName, parameterTypes, returnType)

        val registers = (rStart..rEnd).toIntArray()
        return MethodInstruction.of(opcode, methodIndex, *registers)
    }

    private fun parseArrayInstructionF23x(ctx: F23x_arrayContext): ArrayInstruction {
        val mnemonic = ctx.op.text
        val opcode = DexOpCode.get(mnemonic)

        val r1 = registerInfo.registerNumber(ctx.r1.text)
        val r2 = registerInfo.registerNumber(ctx.r2.text)
        val r3 = registerInfo.registerNumber(ctx.r3.text)

        return ArrayInstruction.of(opcode, r1, r2, r3)
    }

    private fun parseArrayInstructionF12x(ctx: F12x_arrayContext): ArrayInstruction {
        val mnemonic = ctx.op.text
        val opcode = DexOpCode.get(mnemonic)

        val r1 = registerInfo.registerNumber(ctx.r1.text)
        val r2 = registerInfo.registerNumber(ctx.r2.text)

        return ArrayInstruction.of(opcode, r1, r2)
    }

    private fun parseTypeInstructionFt2c(ctx: Ft2c_typeContext): TypeInstruction {
        val mnemonic = ctx.op.text
        val opcode = DexOpCode.get(mnemonic)

        val r1 = registerInfo.registerNumber(ctx.r1.text)
        val r2 = registerInfo.registerNumber(ctx.r2.text)

        val typeIndex = dexComposer.addOrGetTypeIDIndex(ctx.type.text)

        return TypeInstruction.of(opcode, typeIndex, r1, r2)
    }

    private fun parseArrayTypeInstructionF35c(ctx: F35c_arrayContext): ArrayTypeInstruction {
        val mnemonic = ctx.op.text
        val opcode = DexOpCode.get(mnemonic)

        val registers = mutableListOf<Int>()
        ctx.REGISTER().forEach { registers.add(registerInfo.registerNumber(it.text)) }

        val typeIndex = dexComposer.addOrGetTypeIDIndex(ctx.type.text)

        return ArrayTypeInstruction.of(opcode, typeIndex, *registers.toIntArray())
    }

    private fun parseArrayTypeInstructionF3rc(ctx: F3rc_arrayContext): ArrayTypeInstruction {
        val mnemonic = ctx.op.text
        val opcode = DexOpCode.get(mnemonic)

        val rStart = registerInfo.registerNumber(ctx.rstart.text)
        val rEnd   = registerInfo.registerNumber(ctx.rend.text)

        val registers = (rStart..rEnd).toIntArray()
        val typeIndex = dexComposer.addOrGetTypeIDIndex(ctx.type.text)

        return ArrayTypeInstruction.of(opcode, typeIndex, *registers)
    }

    private fun parseMethodProtoInstructionF45cc(ctx: F45cc_methodprotoContext): MethodProtoInstruction {
        val mnemonic = ctx.op.text
        val opcode = DexOpCode.get(mnemonic)

        val registers = mutableListOf<Int>()
        ctx.REGISTER().forEach { registers.add(registerInfo.registerNumber(it.text)) }

        val methodIndex = ctx.method.text.let {
            val (classType, methodName, parameterTypes, returnType) = parseMethodObject(it)
            dexComposer.addOrGetMethodIDIndex(classType!!, methodName, parameterTypes, returnType)
        }

        val protoIndex = ctx.proto.text.let {
            val (_, _, parameterTypes, returnType) = parseMethodObject(it)
            dexComposer.addOrGetProtoIDIndex(parameterTypes, returnType)
        }

        return MethodProtoInstruction.of(opcode, methodIndex, protoIndex, *registers.toIntArray())
    }

    private fun parseMethodProtoInstructionF4rcc(ctx: F4rcc_methodprotoContext): MethodProtoInstruction {
        val mnemonic = ctx.op.text
        val opcode = DexOpCode.get(mnemonic)

        val rStart = registerInfo.registerNumber(ctx.rstart.text)
        val rEnd   = registerInfo.registerNumber(ctx.rend.text)

        val registers = (rStart..rEnd).toIntArray()

        val methodIndex = ctx.method.text.let {
            val (classType, methodName, parameterTypes, returnType) = parseMethodObject(it)
            dexComposer.addOrGetMethodIDIndex(classType!!, methodName, parameterTypes, returnType)
        }

        val protoIndex = ctx.proto.text.let {
            val (_, _, parameterTypes, returnType) = parseMethodObject(it)
            dexComposer.addOrGetProtoIDIndex(parameterTypes, returnType)
        }

        return MethodProtoInstruction.of(opcode, methodIndex, protoIndex, *registers)
    }

    private fun parseMethodHandleInstructionF21c(ctx: F21c_const_handleContext): MethodHandleRefInstruction {
        val mnemonic = ctx.op.text
        val opcode = DexOpCode.get(mnemonic)

        val r1 = registerInfo.registerNumber(ctx.r1.text)

        val methodHandleType =
            MethodHandleType.of(ctx.methodHandleType.text)

        val methodHandle = if (methodHandleType.targetsField) {
            val fieldIndex = ctx.fieldOrMethod.text.let {
                val (classType, name, type) = parseFieldObject(it)
                dexComposer.addOrGetFieldIDIndex(classType!!, name, type)
            }
            MethodHandle.of(methodHandleType, fieldIndex)
        } else {
            val methodIndex = ctx.fieldOrMethod.text.let {
                val (classType, methodName, parameterTypes, returnType) = parseMethodObject(it)
                dexComposer.addOrGetMethodIDIndex(classType!!, methodName, parameterTypes, returnType)
            }
            MethodHandle.of(methodHandleType, methodIndex)
        }

        val methodHandleIndex = dexFile.addMethodHandle(methodHandle)

        return MethodHandleRefInstruction.of(opcode, methodHandleIndex, r1)
    }

    private fun parseMethodTypeInstructionF21c(ctx: F21c_const_typeContext): MethodTypeRefInstruction {
        val mnemonic = ctx.op.text
        val opcode = DexOpCode.get(mnemonic)

        val r1 = registerInfo.registerNumber(ctx.r1.text)

        val protoIndex = ctx.proto.text.let {
            val (_, _, parameterTypes, returnType) = parseMethodObject(it)
            dexComposer.addOrGetProtoIDIndex(parameterTypes, returnType)
        }

        return MethodTypeRefInstruction.of(opcode, protoIndex, r1)
    }

    private fun parsePayloadInstructionF31t(ctx: F31t_payloadContext, codeOffset: Int): PayloadInstruction {
        val mnemonic = ctx.op.text
        val opcode = DexOpCode.get(mnemonic)

        val r1 = registerInfo.registerNumber(ctx.r1.text)

        val label = ctx.label.text
        val payloadOffset = branchOffset(codeOffset, label)

        return PayloadInstruction.of(opcode, payloadOffset, r1)
    }

    private fun branchOffset(currentOffset: Int, target: String): Int {
        val targetOffset = labelMapping[target] ?: throw RuntimeException("unknown label $target")
        return targetOffset - currentOffset
    }
}

private data class RegisterInfo(val registers: Int, val locals: Int, val insSize: Int) {
    fun registerNumber(register: String): Int {
        return when (register.first()) {
            'v' -> register.substring(1).toInt()

            'p' -> {
                val number = register.substring(1).toInt()
                return locals + number
            }

            else -> throw RuntimeException("unknown register format $register")
        }
    }
}