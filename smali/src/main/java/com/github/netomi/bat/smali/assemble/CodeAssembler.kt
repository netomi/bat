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
import com.github.netomi.bat.dexfile.instruction.DexInstruction
import com.github.netomi.bat.dexfile.instruction.DexInstructions
import com.github.netomi.bat.dexfile.io.InstructionWriter
import com.github.netomi.bat.smali.parser.SmaliParser
import org.antlr.v4.runtime.ParserRuleContext

internal class CodeAssembler constructor(private val dexFile: DexFile, private val dexComposer: DexComposer) {

    fun parseCode(lCtx: List<SmaliParser.SInstructionContext>, method: EncodedMethod): Code {

        val instructions = mutableListOf<DexInstruction>()
        var registers = 0

        lCtx.forEach { ctx ->
            val t = ctx.getChild(0) as ParserRuleContext
            when (t.ruleIndex) {
                SmaliParser.RULE_fregisters -> {
                    val c = t as SmaliParser.FregistersContext
                    registers = c.xregisters.text.toInt()
                }

                SmaliParser.RULE_f0x -> {
                    val c = t as SmaliParser.F0xContext
                    val insn = when (val opName = c.op.text) {
                        "return-void" -> DexInstructions.returnVoid()
                        "nop"         -> DexInstructions.nop()
                        else          -> parserError(ctx, "unexpected opname $opName")
                    }

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

                    instructions.add(insn)
                }
            }
        }

        val code = Code.of(registers, 1, 0)

        val insns = writeInstructions(instructions)
        code.insns = insns
        code.insnsSize = insns.size

        return code
    }

    private fun writeInstructions(instructions: List<DexInstruction>): ShortArray {
        val codeLen = instructions.stream().map { a: DexInstruction -> a.length }.reduce(0) { a: Int, b: Int -> a + b }

        val writer = InstructionWriter(codeLen)
        var offset = 0
        for (instruction in instructions) {
            instruction.write(writer, offset)
            offset += instruction.length
        }

        return writer.array
    }

}