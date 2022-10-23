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

import com.github.netomi.bat.classfile.Method
import com.github.netomi.bat.classfile.editor.CodeEditor
import com.github.netomi.bat.classfile.instruction.JvmInstruction
import com.github.netomi.bat.jasm.parser.JasmParser
import com.github.netomi.bat.jasm.parser.JasmParser.*
import org.antlr.v4.runtime.ParserRuleContext

internal class CodeAssembler constructor(private val method:      Method,
                                         private val codeEditor:  CodeEditor,
                                         private val lenientMode: Boolean) {

    fun parseCode(iCtx: List<JasmParser.SInstructionContext>) {

        println("assembling method $method")
        val instructionAssembler = InstructionAssembler(codeEditor.constantPoolEditor)

        var codeOffset = 0

        iCtx.forEach { ctx ->
            try {
                val t = ctx.getChild(0) as ParserRuleContext
                val insn: JvmInstruction? = when (t.ruleIndex) {
                    RULE_sLabel -> {
                        val c = t as SLabelContext
                        val label = c.label.text
                        //codeEditor.prependLabel(0, c.label.text)
                        null
                    }

                    RULE_fArithmeticInstructions       -> instructionAssembler.parseArithmeticInstructions(t as FArithmeticInstructionsContext)
                    RULE_fConversionInstructions       -> instructionAssembler.parseConversionInstructions(t as FConversionInstructionsContext)
                    RULE_fStackInstructions            -> instructionAssembler.parseStackInstructions(t as FStackInstructionsContext)
                    RULE_fImplicitVariableInstructions -> instructionAssembler.parseImplicitVariableInstructions(t as FImplicitVariableInstructionsContext)
                    RULE_fExplicitVariableInstructions -> instructionAssembler.parseExplicitVariableInstructions(t as FExplicitVariableInstructionsContext)
                    RULE_fArrayInstructions            -> instructionAssembler.parseArrayInstructions(t as FArrayInstructionsContext)
                    RULE_fExceptionInstructions        -> instructionAssembler.parseExceptionInstructions(t as FExceptionInstructionsContext)
                    RULE_fNullReferenceInstructions    -> instructionAssembler.parseNullReferenceInstructions(t as FNullReferenceInstructionsContext)
                    RULE_fReturnInstructions           -> instructionAssembler.parseReturnInstructions(t as FReturnInstructionsContext)
                    RULE_fMonitorInstructions          -> instructionAssembler.parseMonitorInstructions(t as FMonitorInstructionsContext)
                    RULE_fCompareInstructions          -> instructionAssembler.parseCompareInstructions(t as FCompareInstructionsContext)
                    RULE_fFieldInstructions            -> instructionAssembler.parseFieldInstructions(t as FFieldInstructionsContext)
                    RULE_fMethodInstructions           -> instructionAssembler.parseMethodInstructions(t as FMethodInstructionsContext)
                    RULE_fClassInstructions            -> instructionAssembler.parseClassInstructions(t as FClassInstructionsContext)
                    RULE_fBranchInstructions           -> instructionAssembler.parseBranchInstructions(t as FBranchInstructionsContext)
                    else -> null
                }

                if (insn != null) {
                    println("%04x: %s".format(codeOffset, insn.toString(codeEditor.classFile)))
                    codeOffset += insn.getLength(codeOffset)
                }

            } catch (exception: RuntimeException) {
                parserError(ctx, exception)
            }
        }

    }
}