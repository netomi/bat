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
import com.github.netomi.bat.classfile.attribute.ExceptionEntry
import com.github.netomi.bat.classfile.editor.CodeEditor
import com.github.netomi.bat.classfile.instruction.JvmInstruction
import com.github.netomi.bat.jasm.parser.JasmParser.*
import org.antlr.v4.runtime.ParserRuleContext

internal class CodeAssembler constructor(private val method:      Method,
                                         private val codeEditor:  CodeEditor,
                                         private val lenientMode: Boolean) {

    fun parseCode(iCtx: List<SInstructionContext>) {
        val instructionAssembler = InstructionAssembler(codeEditor.constantPoolEditor)
        val debugStateComposer   = DebugStateComposer(codeEditor)

        val exceptionEntries = mutableListOf<ExceptionEntry>()
        var codeOffset = 0

        var maxStack  = -1
        var maxLocals = -1

        iCtx.forEach { ctx ->
            try {
                val t = ctx.getChild(0) as ParserRuleContext
                val insn: JvmInstruction? = when (t.ruleIndex) {
                    RULE_fMaxStack -> {
                        val c = t as FMaxStackContext
                        maxStack = c.maxStack.text.toInt()
                        null
                    }

                    RULE_fMaxLocals -> {
                        val c = t as FMaxLocalsContext
                        maxLocals = c.maxLocals.text.toInt()
                        null
                    }

                    RULE_sLabel -> {
                        val c = t as SLabelContext
                        val label = c.label.text
                        codeEditor.prependLabel(0, label)
                        null
                    }

                    RULE_fLine -> {
                        val c = t as FLineContext
                        val lineNumber = parseLong(c.line.text).toInt()

                        if (lineNumber < 0 && !lenientMode) {
                            parserError(ctx, "negative line number")
                        }

                        debugStateComposer.addLineNumber(codeOffset, lineNumber)
                        null
                    }

                    RULE_fCatch -> {
                        exceptionEntries.add(instructionAssembler.parseCatchDirective(t as FCatchContext))
                        null
                    }

                    RULE_fCatchall -> {
                        exceptionEntries.add(instructionAssembler.parseCatchAllDirective(t as FCatchallContext))
                        null
                    }

                    RULE_fStartlocal -> {
                        val c = t as FStartlocalContext

                        val variable = c.variable.text.toInt()

                        val name       = c.name.text.removeSurrounding("\"")
                        val descriptor = c.descriptor?.text
                        val signature  = c.signature?.text?.removeSurrounding("\"")

                        debugStateComposer.startLocalVariable(codeOffset, variable, name, descriptor, signature)
                        null
                    }

                    RULE_fEndlocal -> {
                        val c = t as FEndlocalContext
                        val variable = c.variable.text.toInt()

                        debugStateComposer.endLocalVariable(codeOffset, variable)
                        null
                    }

                    RULE_fArithmeticInstructions          -> instructionAssembler.parseArithmeticInstructions(t as FArithmeticInstructionsContext)
                    RULE_fConversionInstructions          -> instructionAssembler.parseConversionInstructions(t as FConversionInstructionsContext)
                    RULE_fStackInstructions               -> instructionAssembler.parseStackInstructions(t as FStackInstructionsContext)
                    RULE_fImplicitVariableInstructions    -> instructionAssembler.parseImplicitVariableInstructions(t as FImplicitVariableInstructionsContext)
                    RULE_fExplicitVariableInstructions    -> instructionAssembler.parseExplicitVariableInstructions(t as FExplicitVariableInstructionsContext)
                    RULE_fArrayInstructions               -> instructionAssembler.parseArrayInstructions(t as FArrayInstructionsContext)
                    RULE_fExceptionInstructions           -> instructionAssembler.parseExceptionInstructions(t as FExceptionInstructionsContext)
                    RULE_fNullReferenceInstructions       -> instructionAssembler.parseNullReferenceInstructions(t as FNullReferenceInstructionsContext)
                    RULE_fReturnInstructions              -> instructionAssembler.parseReturnInstructions(t as FReturnInstructionsContext)
                    RULE_fMonitorInstructions             -> instructionAssembler.parseMonitorInstructions(t as FMonitorInstructionsContext)
                    RULE_fCompareInstructions             -> instructionAssembler.parseCompareInstructions(t as FCompareInstructionsContext)
                    RULE_fFieldInstructions               -> instructionAssembler.parseFieldInstructions(t as FFieldInstructionsContext)
                    RULE_fMethodInstructions              -> instructionAssembler.parseMethodInstructions(t as FMethodInstructionsContext)
                    RULE_fInterfaceMethodInstructions     -> instructionAssembler.parseInterfaceMethodInstructions(t as FInterfaceMethodInstructionsContext)
                    RULE_fInvokeDynamicInstructions       -> instructionAssembler.parseInvokeDynamicInstruction(t as FInvokeDynamicInstructionsContext)
                    RULE_fClassInstructions               -> instructionAssembler.parseClassInstructions(t as FClassInstructionsContext)
                    RULE_fPrimitiveArrayInstructions      -> instructionAssembler.parsePrimitiveArrayInstructions(t as FPrimitiveArrayInstructionsContext)
                    RULE_fArrayClassInstructions          -> instructionAssembler.parseArrayClassInstructions(t as FArrayClassInstructionsContext)
                    RULE_fMultiArrayClassInstruction      -> instructionAssembler.parseMultiArrayClassInstructions(t as FMultiArrayClassInstructionContext)
                    RULE_fBranchInstructions              -> instructionAssembler.parseBranchInstructions(t as FBranchInstructionsContext)
                    RULE_fLiteralConstantInstructions     -> instructionAssembler.parseLiteralConstantInstructions(t as FLiteralConstantInstructionsContext)
                    RULE_fWideLiteralConstantInstructions -> instructionAssembler.parseWideLiteralConstantInstructions(t as FWideLiteralConstantInstructionsContext)
                    RULE_fLiteralVariableInstructions     -> instructionAssembler.parseLiteralVariableInstructions(t as FLiteralVariableInstructionsContext)
                    RULE_fImplicitLiteralInstructions     -> instructionAssembler.parseImplicitLiteralInstructions(t as FImplicitLiteralInstructionsContext)
                    RULE_fExplicitLiteralInstruction      -> instructionAssembler.parseExplicitLiteralInstructions(t as FExplicitLiteralInstructionContext)
                    RULE_fLookupSwitch                    -> instructionAssembler.parseLookupSwitchInstruction(t as FLookupSwitchContext)
                    RULE_fTableSwitch                     -> instructionAssembler.parseTableSwitchInstruction(t as FTableSwitchContext)

                    else -> error("rule ${t.ruleIndex} not handled")
                }

                if (insn != null) {
                    codeOffset += insn.getLength(codeOffset)
                    codeEditor.prependInstruction(0, insn)
                }

            } catch (exception: RuntimeException) {
                parserError(ctx, exception)
            }
        }

        for (exceptionEntry in exceptionEntries) {
            codeEditor.addExceptionEntry(exceptionEntry)
        }

        codeEditor.finishEditing()

        debugStateComposer.finish(codeOffset)
    }
}