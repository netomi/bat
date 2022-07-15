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
import org.antlr.v4.runtime.ParserRuleContext

internal class CodeAssembler constructor(private val dexFile: DexFile, private val dexComposer: DexComposer) {

    fun parseCode(lCtx: List<SmaliParser.SInstructionContext>, method: EncodedMethod): Code {

        val instructions = mutableListOf<DexInstruction>()

        var registerInfo: RegisterInfo? = null

        val labelMapping = collectLabels(lCtx)
        var codeOffset = 0

        lCtx.forEach { ctx ->
            val t = ctx.getChild(0) as ParserRuleContext
            when (t.ruleIndex) {
                SmaliParser.RULE_fline -> {
                    val c = t as SmaliParser.FlineContext
                    val lineNumber = c.line.text.toInt()
                }

                SmaliParser.RULE_fprologue -> {
                }

                SmaliParser.RULE_fepilogue -> {

                }

                SmaliParser.RULE_fregisters -> {
                    val c = t as SmaliParser.FregistersContext
                    val registers = c.xregisters.text.toInt()
                    registerInfo = computeRegisterInfo(method, registers)
                }

                SmaliParser.RULE_f10x -> {
                    val c = t as SmaliParser.F10xContext

                    val mnemonic = c.op.text
                    val opcode = DexOpCode.get(mnemonic)

                    if (opcode.format != DexInstructionFormat.FORMAT_10x) {
                        parserError(ctx, "unexpected instruction $mnemonic")
                    }

                    val insn = opcode.createInstruction(0)
                    codeOffset += insn.length
                    instructions.add(insn)
                }

                SmaliParser.RULE_fx0t -> {
                    val c = t as SmaliParser.Fx0tContext

                    val mnemonic = c.op.text
                    val opcode   = DexOpCode.get(mnemonic)

                    if (!mnemonic.startsWith("goto")) {
                        parserError(ctx, "unexpected instruction $mnemonic")
                    }

                    val label = c.target.text
                    val branchOffset = getBranchOffset(codeOffset, label, labelMapping)

                    val insn = BranchInstruction.of(opcode, branchOffset)
                    codeOffset += insn.length
                    instructions.add(insn)
                }

                SmaliParser.RULE_f21t -> {
                    val c = t as SmaliParser.F21tContext

                    val mnemonic = c.op.text
                    val opcode   = DexOpCode.get(mnemonic)

                    val label = c.label.text
                    val branchOffset = getBranchOffset(codeOffset, label, labelMapping)
                    val register = getRegisterNumber(c.r1.text, registerInfo!!)

                    if (!mnemonic.startsWith("if-") && !mnemonic.endsWith("z")) {
                        parserError(ctx, "unexpected instruction $mnemonic")
                    }

                    val insn = BranchInstruction.of(opcode, branchOffset, register!!)
                    codeOffset += insn.length
                    instructions.add(insn)
                }

                SmaliParser.RULE_f21c -> {
                    val c = t as SmaliParser.F21cContext

                    val mnemonic = c.op.text
                    if (!(mnemonic.startsWith("sget") || mnemonic.startsWith("sput"))) {
                        parserError(ctx, "unexpected instruction $mnemonic")
                    }
                    val opcode   = DexOpCode.get(mnemonic)

                    val register = getRegisterNumber(c.r1.text, registerInfo!!)

                    val field = c.fld.text
                    val (classType, fieldName, fieldType) = parseFieldObject(field)

                    val fieldIndex = dexComposer.addOrGetFieldIDIndex(classType!!, fieldName, fieldType)

                    val insn = FieldInstruction.of(opcode, fieldIndex, register!!)
                    codeOffset += insn.length
                    instructions.add(insn)
                }

                SmaliParser.RULE_fm5c -> {
                    val c = t as SmaliParser.Fm5cContext
                    val opName = c.op.text

                    val insn = when (opName) {
                        "invoke-direct" -> {
                            val methodType = c.method.text

                            val (classType, methodName, parameterTypes, returnType) = parseMethodObject(methodType)
                            val methodID =
                                dexComposer.addOrGetMethodIDIndex(classType!!, methodName, parameterTypes, returnType)

                            val regs = intArrayOf(c.REGISTER(0).text.substring(1).toInt())
                            DexInstructions.invokeDirect(methodID, *regs)
                        }

                        "return-void" -> {
                            DexInstructions.returnVoid()
                        }
                        else -> DexInstructions.nop()
                    }

                    codeOffset += insn.length
                    instructions.add(insn)
                }
            }
        }

        val code = Code.of(registerInfo!!.registers, registerInfo!!.insSize, 0)

        val insns = writeInstructions(instructions)
        code.insns     = insns
        code.insnsSize = insns.size

        return code
    }

    private fun collectLabels(listCtx: List<SmaliParser.SInstructionContext>): Map<String, Int> {
        var codeOffset = 0
        val labelMapping = mutableMapOf<String, Int>()

        listCtx.forEach { ctx ->
            val t = ctx.getChild(0) as ParserRuleContext
            when (t.ruleIndex) {
                SmaliParser.RULE_fx0t,
                SmaliParser.RULE_f10x,
                SmaliParser.RULE_f1x,
                SmaliParser.RULE_fconst,
                SmaliParser.RULE_f21c,
                SmaliParser.RULE_ft2c,
                SmaliParser.RULE_ff2c,
                SmaliParser.RULE_f2x,
                SmaliParser.RULE_f3x,
                SmaliParser.RULE_ft5c,
                SmaliParser.RULE_fm5c,
                SmaliParser.RULE_fmrc,
                SmaliParser.RULE_fm45cc,
                SmaliParser.RULE_fm4rcc,
                SmaliParser.RULE_fmcustomc,
                SmaliParser.RULE_fmcustomrc,
                SmaliParser.RULE_ftrc,
                SmaliParser.RULE_f31t,
                SmaliParser.RULE_f21t,
                SmaliParser.RULE_f2t,
                SmaliParser.RULE_f2sb -> {
                    val mnemonic = t.getChild(0).text
                    val opcode   = DexOpCode.get(mnemonic)

                    val insn = opcode.createInstruction(0)
                    codeOffset += insn.length
                }

                SmaliParser.RULE_sLabel -> {
                    val c = t as SmaliParser.SLabelContext
                    val label = c.label.text
                    labelMapping[label] = codeOffset
                }
                else -> {}
            }
        }

        return labelMapping
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

    private fun computeRegisterInfo(method: EncodedMethod, registers: Int): RegisterInfo {
        val protoID = method.getMethodID(dexFile).getProtoID(dexFile)

        var insSize = if (method.isStatic) 1 else 0
        val argumentSize = DexClasses.getArgumentSize(protoID.parameters.getTypes(dexFile))
        insSize += argumentSize

        return RegisterInfo(registers, registers - argumentSize, argumentSize)
    }

    private fun getRegisterNumber(register: String, registerInfo: RegisterInfo): Int? {
        return when (register.first()) {
            'v' -> register.substring(1).toInt()

            'p' -> {
                val number = register.substring(1).toInt()
                return registerInfo.locals + number
            }

            else -> null
        }
    }

    private fun getBranchOffset(currentOffset: Int, target: String, labelMapping: Map<String, Int>): Int {
        val targetOffset = labelMapping[target] ?: throw RuntimeException("unknown label $target")
        return targetOffset - currentOffset
    }
}

data class RegisterInfo(val registers: Int, val locals: Int, val insSize: Int)