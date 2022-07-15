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

import com.github.netomi.bat.dexfile.Code
import com.github.netomi.bat.dexfile.DexFile
import com.github.netomi.bat.dexfile.EncodedMethod
import com.github.netomi.bat.dexfile.editor.DexComposer
import com.github.netomi.bat.dexfile.instruction.*
import com.github.netomi.bat.dexfile.io.InstructionWriter
import com.github.netomi.bat.dexfile.util.DexClasses
import com.github.netomi.bat.smali.parser.SmaliParser
import com.github.netomi.bat.smali.parser.SmaliParser.F11x_basicContext
import com.github.netomi.bat.smali.parser.SmaliParser.F12x_conversionContext
import com.github.netomi.bat.smali.parser.SmaliParser.Fx0t_branchContext
import org.antlr.v4.runtime.ParserRuleContext

internal class CodeAssembler constructor(private val method: EncodedMethod, private val dexComposer: DexComposer) {

    private val dexFile: DexFile
        get() = dexComposer.dexFile

    private lateinit var registerInfo: RegisterInfo
    private          var labelMapping: MutableMap<String, Int> = HashMap()

    fun parseCode(lCtx: List<SmaliParser.SInstructionContext>): Code {

        val instructions = mutableListOf<DexInstruction>()

        collectRegisterInfo(lCtx)
        collectLabels(lCtx)

        var codeOffset = 0

        lCtx.forEach { ctx ->
            val t = ctx.getChild(0) as ParserRuleContext
            val insn: DexInstruction? = when (t.ruleIndex) {
                SmaliParser.RULE_fline -> {
                    val c = t as SmaliParser.FlineContext
                    val lineNumber = c.line.text.toInt()
                    null
                }

                SmaliParser.RULE_fprologue  -> null
                SmaliParser.RULE_fepilogue  -> null
                SmaliParser.RULE_fregisters -> null

                SmaliParser.RULE_f10x -> {
                    val c = t as SmaliParser.F10xContext

                    val mnemonic = c.op.text
                    val opcode = DexOpCode.get(mnemonic)

                    if (opcode.format != DexInstructionFormat.FORMAT_10x) {
                        parserError(ctx, "unexpected instruction $mnemonic")
                    }

                    opcode.createInstruction(0)
                }

                SmaliParser.RULE_f12x_conversion -> parseConversionInstructionF12x(t as F12x_conversionContext)
                SmaliParser.RULE_f11x_basic      -> parseBasicInstructionF11x(t as F11x_basicContext)
                SmaliParser.RULE_fx0t_branch     -> parseBranchInstructionFx0t(t as Fx0t_branchContext, codeOffset)

                SmaliParser.RULE_f21t -> {
                    val c = t as SmaliParser.F21tContext

                    val mnemonic = c.op.text
                    val opcode   = DexOpCode.get(mnemonic)

                    val label = c.label.text
                    val branchOffset = branchOffset(codeOffset, label)
                    val r1 = registerInfo.registerNumber(c.r1.text)

                    if (!mnemonic.startsWith("if-") && !mnemonic.endsWith("z")) {
                        parserError(ctx, "unexpected instruction $mnemonic")
                    }

                    BranchInstruction.of(opcode, branchOffset, r1)
                }

                SmaliParser.RULE_f21c -> {
                    val c = t as SmaliParser.F21cContext

                    val mnemonic = c.op.text
                    if (!(mnemonic.startsWith("sget") || mnemonic.startsWith("sput"))) {
                        parserError(ctx, "unexpected instruction $mnemonic")
                    }
                    val opcode   = DexOpCode.get(mnemonic)

                    val register = registerInfo.registerNumber(c.r1.text)

                    val field = c.fld.text
                    val (classType, fieldName, fieldType) = parseFieldObject(field)

                    val fieldIndex = dexComposer.addOrGetFieldIDIndex(classType!!, fieldName, fieldType)
                    FieldInstruction.of(opcode, fieldIndex, register)
                }

                SmaliParser.RULE_ff2c -> {
                    val c = t as SmaliParser.Ff2cContext

                    val mnemonic = c.op.text
                    if (!(mnemonic.startsWith("iget") || mnemonic.startsWith("iput"))) {
                        parserError(ctx, "unexpected instruction $mnemonic")
                    }
                    val opcode = DexOpCode.get(mnemonic)

                    val r1 = registerInfo.registerNumber(c.r1.text)
                    val r2 = registerInfo.registerNumber(c.r2.text)

                    val field = c.fld.text
                    val (classType, fieldName, fieldType) = parseFieldObject(field)

                    val fieldIndex = dexComposer.addOrGetFieldIDIndex(classType!!, fieldName, fieldType)

                    FieldInstruction.of(opcode, fieldIndex, r1, r2)
                }

                SmaliParser.RULE_fm5c -> {
                    val c = t as SmaliParser.Fm5cContext

                    val mnemonic = c.op.text
                    if (!mnemonic.startsWith("invoke-")) {
                        parserError(ctx, "unexpected instruction $mnemonic")
                    }
                    val opcode = DexOpCode.get(mnemonic)
                    val registers = c.REGISTER().map { registerInfo.registerNumber(it.text) }.toIntArray()

                    val methodType = c.method.text

                    val (classType, methodName, parameterTypes, returnType) = parseMethodObject(methodType)
                    val methodIndex = dexComposer.addOrGetMethodIDIndex(classType!!, methodName, parameterTypes, returnType)

                    MethodInstruction.of(opcode, methodIndex, *registers)
                }

                else -> parserError(t, "unexpected instruction")
            }

            insn?.apply {
                codeOffset += length
                instructions.add(this)
            }
        }

        val code = Code.of(registerInfo.registers, registerInfo.insSize, 0)

        val insns = writeInstructions(instructions)
        code.insns     = insns
        code.insnsSize = insns.size

        return code
    }

    private fun collectRegisterInfo(listCtx: List<SmaliParser.SInstructionContext>) {

        val protoID = method.getMethodID(dexFile).getProtoID(dexFile)
        var insSize = if (method.isStatic) 0 else 1
        val argumentSize = DexClasses.getArgumentSize(protoID.parameters.getTypes(dexFile))
        insSize += argumentSize

        listCtx.forEach { ctx ->
            val t = ctx.getChild(0) as ParserRuleContext
            when (t.ruleIndex) {
                SmaliParser.RULE_fregisters -> {
                    val c = t as SmaliParser.FregistersContext
                    val registers = c.xregisters.text.toInt()
                    registerInfo = RegisterInfo(registers, registers - insSize, insSize)
                    return
                }

                SmaliParser.RULE_flocals -> {
                    val c = t as SmaliParser.FlocalsContext
                    val locals = c.xlocals.text.toInt()
                    registerInfo = RegisterInfo(locals + insSize, locals, insSize)
                    return
                }

                else -> {}
            }
        }
    }

    private fun collectLabels(listCtx: List<SmaliParser.SInstructionContext>) {
        labelMapping.clear()
        var codeOffset = 0

        listCtx.forEach { ctx ->
            val t = ctx.getChild(0) as ParserRuleContext
            when (t.ruleIndex) {
                SmaliParser.RULE_sLabel -> {
                    val c = t as SmaliParser.SLabelContext
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

        if (!mnemonic.startsWith("goto")) {
            parserError(ctx, "unexpected instruction $mnemonic")
        }

        val label = ctx.target.text
        val branchOffset = branchOffset(codeOffset, label)

        return BranchInstruction.of(opcode, branchOffset)
    }

    private fun writeInstructions(instructions: List<DexInstruction>): ShortArray {
        val codeLen = instructions.stream().map { a: DexInstruction -> a.length }.reduce(0) { a, b -> a + b }

        val writer = InstructionWriter(codeLen)
        var offset = 0
        for (instruction in instructions) {
            instruction.write(writer, offset)
            offset += instruction.length
        }

        return writer.array
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