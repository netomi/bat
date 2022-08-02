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
import com.github.netomi.bat.dexfile.editor.CodeEditor
import com.github.netomi.bat.dexfile.editor.DexEditor
import com.github.netomi.bat.dexfile.instruction.*
import com.github.netomi.bat.dexfile.util.DexClasses
import com.github.netomi.bat.smali.parser.SmaliParser.*
import org.antlr.v4.runtime.ParserRuleContext

internal class CodeAssembler constructor(private val method:     EncodedMethod,
                                         private val codeEditor: CodeEditor) {

    private val dexEditor: DexEditor
        get() = codeEditor.dexEditor

    private val dexFile: DexFile
        get() = dexEditor.dexFile

    private val code: Code
        get() = codeEditor.code

    fun parseCode(iCtx: List<SInstructionContext>, pCtx: List<SParameterContext>) {

        val tryElements = mutableListOf<Try>()

        val registerInfo         = collectRegisterInfo(iCtx)
        val instructionAssembler = InstructionAssembler(registerInfo, dexEditor)

        val debugSequenceAssembler = DebugSequenceAssembler(code.debugInfo)

        var codeOffset = 0

        pCtx.forEach { ctx ->
            val parameterIndex = parseParameterIndex(ctx, dexFile, method)
            val name = ctx.name?.text?.removeSurrounding("\"")
            codeEditor.setParameterName(parameterIndex, name)
        }

        // collect all payloads in the first pass.
        val payloadMapping = collectPayloads(iCtx, instructionAssembler)

        // in the second pass, we parse the instructions.
        iCtx.forEach { ctx ->
            val t = ctx.getChild(0) as ParserRuleContext
            val insn: DexInstruction? = when (t.ruleIndex) {
                RULE_sLabel -> {
                    val c = t as SLabelContext
                    codeEditor.prependLabel(0, c.label.text)
                    null
                }

                RULE_fline -> {
                    val c = t as FlineContext
                    val lineNumber = parseLong(c.line.text).toInt()

                    if (lineNumber <= 0) {
                        parserError(ctx, "line number too large")
                    }

                    debugSequenceAssembler.advanceLine(lineNumber, codeOffset)
                    null
                }

                RULE_fprologue  -> {
                    debugSequenceAssembler.prologueEnd()
                    null
                }

                RULE_fepilogue  -> {
                    debugSequenceAssembler.epilogueStart()
                    null
                }

                RULE_fstartlocal -> {
                    val c = t as FstartlocalContext

                    val register = registerInfo.registerNumber(c.r.text)

                    var nameIndex: Int = NO_INDEX
                    var typeIndex: Int = NO_INDEX

                    val name = c.name.text.removeSurrounding("\"")
                    if (name.isNotEmpty()) {
                        nameIndex = dexEditor.addOrGetStringIDIndex(name)
                    }

                    if (c.type != null) {
                        val type = c.type.text
                        typeIndex = dexEditor.addOrGetTypeIDIndex(type)
                    }

                    val sigIndex = if (c.sig != null) {
                        dexEditor.addOrGetStringIDIndex(c.sig.text.removeSurrounding("\""))
                    } else {
                        NO_INDEX
                    }

                    debugSequenceAssembler.startLocal(register, nameIndex, typeIndex, sigIndex, codeOffset)
                    null
                }

                RULE_frestart -> {
                    val c = t as FstartlocalContext

                    val register = registerInfo.registerNumber(c.r.text)

                    debugSequenceAssembler.restartLocal(register, codeOffset)
                    null
                }

                RULE_fendlocal -> {
                    val c = t as FendlocalContext

                    val register = registerInfo.registerNumber(c.r.text)
                    debugSequenceAssembler.endLocal(register, codeOffset)
                    null
                }

                RULE_fcatch -> {
                    tryElements.add(instructionAssembler.parseCatchDirective(t as FcatchContext))
                    null
                }

                RULE_fcatchall -> {
                    tryElements.add(instructionAssembler.parseCatchAllDirective(t as FcatchallContext))
                    null
                }

                RULE_farraydata -> {
                    codeOffset = alignOffsetForPayload(codeOffset)
                    instructionAssembler.parseArrayDataPayload(t as FarraydataContext)
                }

                RULE_fsparseswitch -> {
                    codeOffset = alignOffsetForPayload(codeOffset)
                    instructionAssembler.parseSparseSwitchPayload(t as FsparseswitchContext)
                }

                RULE_fpackedswitch -> {
                    codeOffset = alignOffsetForPayload(codeOffset)
                    instructionAssembler.parsePackedSwitchPayload(t as FpackedswitchContext)
                }

                RULE_f10x -> {
                    val c = t as F10xContext

                    val mnemonic = c.op.text
                    val opcode = DexOpCode[mnemonic]
                    opcode.createInstruction()
                }

                RULE_f12x_conversion   -> instructionAssembler.parseConversionInstructionF12x(t as F12x_conversionContext)
                RULE_f11x_basic        -> instructionAssembler.parseBasicInstructionF11x(t as F11x_basicContext)
                RULE_fx0t_branch       -> instructionAssembler.parseBranchInstructionFx0t(t as Fx0t_branchContext)
                RULE_f21t_branch       -> instructionAssembler.parseBranchInstructionF21t(t as F21t_branchContext)
                RULE_f22t_branch       -> instructionAssembler.parseBranchInstructionF22t(t as F22t_branchContext)
                RULE_f21c_field        -> instructionAssembler.parseFieldInstructionF21c(t as F21c_fieldContext)
                RULE_f22c_field        -> instructionAssembler.parseFieldInstructionF22c(t as F22c_fieldContext)
                RULE_fconst_int        -> instructionAssembler.parseLiteralInstructionInteger(t as Fconst_intContext)
                RULE_fconst_string     -> instructionAssembler.parseLiteralInstructionString(t as Fconst_stringContext)
                RULE_fconst_type       -> instructionAssembler.parseLiteralInstructionType(t as Fconst_typeContext)
                RULE_f12x_arithmetic   -> instructionAssembler.parseArithmeticInstructionF12x(t as F12x_arithmeticContext)
                RULE_f23x_arithmetic   -> instructionAssembler.parseArithmeticInstructionF23x(t as F23x_arithmeticContext)
                RULE_f22sb_arithmetic  -> instructionAssembler.parseArithmeticLiteralInstructionF22sb(t as F22sb_arithmeticContext)
                RULE_fx2x_move         -> instructionAssembler.parseBasicInstructionMoveFx2x(t as Fx2x_moveContext)
                RULE_f12x_array        -> instructionAssembler.parseArrayInstructionF12x(t as F12x_arrayContext)
                RULE_ft2c_type         -> instructionAssembler.parseTypeInstructionFt2c(t as Ft2c_typeContext)
                RULE_f23x_compare      -> instructionAssembler.parseBasicInstructionCompareF23x(t as F23x_compareContext)
                RULE_f23x_array        -> instructionAssembler.parseArrayInstructionF23x(t as F23x_arrayContext)
                RULE_f35c_method       -> instructionAssembler.parseMethodInstructionF35c(t as F35c_methodContext)
                RULE_f3rc_method       -> instructionAssembler.parseMethodInstructionF3rc(t as F3rc_methodContext)
                RULE_f35c_array        -> instructionAssembler.parseArrayTypeInstructionF35c(t as F35c_arrayContext)
                RULE_f3rc_array        -> instructionAssembler.parseArrayTypeInstructionF3rc(t as F3rc_arrayContext)
                RULE_f45cc_methodproto -> instructionAssembler.parseMethodProtoInstructionF45cc(t as F45cc_methodprotoContext)
                RULE_f4rcc_methodproto -> instructionAssembler.parseMethodProtoInstructionF4rcc(t as F4rcc_methodprotoContext)
                RULE_f35c_custom       -> instructionAssembler.parseCallSiteInstructionF35c(t as F35c_customContext)
                RULE_f3rc_custom       -> instructionAssembler.parseCallSiteInstructionF3rc(t as F3rc_customContext)
                RULE_f21c_const_handle -> instructionAssembler.parseMethodHandleInstructionF21c(t as F21c_const_handleContext)
                RULE_f21c_const_type   -> instructionAssembler.parseMethodTypeInstructionF21c(t as F21c_const_typeContext)
                RULE_f31t_payload      -> instructionAssembler.parsePayloadInstructionF31t(t as F31t_payloadContext, payloadMapping)

                else -> null
            }

            if (insn != null) {
                codeOffset += insn.length
                if (insn !is Payload) {
                    codeEditor.prependInstruction(0, insn)
                }
            }
        }

        debugSequenceAssembler.end()

        for (tryElement in tryElements) {
            codeEditor.addTryCatchElement(tryElement)
        }

        // TODO: the registersSize should actually be calculated however this is non-trivial
        codeEditor.finishEditing(registerInfo.registers)
    }

    private fun collectPayloads(iCtx: List<SInstructionContext>, instructionAssembler: InstructionAssembler): Map<String, Payload> {
        val payloadMapping = mutableMapOf<String, Payload>()
        var lastLabel: String? = null

        // in the first pass, we collect the payloads
        iCtx.forEach { ctx ->
            val t = ctx.getChild(0) as ParserRuleContext
            when (t.ruleIndex) {
                RULE_sLabel -> {
                    val c = t as SLabelContext
                    lastLabel = c.label.text
                }

                RULE_farraydata -> {
                    val payload = instructionAssembler.parseArrayDataPayload(t as FarraydataContext)
                    if (lastLabel != null) {
                        payloadMapping[lastLabel!!] = payload
                    } else {
                        parserError(ctx, "unknown label for array data payload")
                    }
                }

                RULE_fsparseswitch -> {
                    val payload = instructionAssembler.parseSparseSwitchPayload(t as FsparseswitchContext)
                    if (lastLabel != null) {
                        payloadMapping[lastLabel!!] = payload
                    } else {
                        parserError(ctx, "unknown label for array data payload")
                    }
                }

                RULE_fpackedswitch -> {
                    val payload = instructionAssembler.parsePackedSwitchPayload(t as FpackedswitchContext)
                    if (lastLabel != null) {
                        payloadMapping[lastLabel!!] = payload
                    } else {
                        parserError(ctx, "unknown label for array data payload")
                    }
                }
            }
        }

        return payloadMapping
    }

    private fun collectRegisterInfo(listCtx: List<SInstructionContext>): RegisterInfo {
        val protoID = method.getProtoID(dexFile)
        var insSize = if (method.isStatic) 0 else 1
        val argumentSize = DexClasses.getArgumentSize(protoID.parameters.getTypes(dexFile))
        insSize += argumentSize

        listCtx.forEach { ctx ->
            val t = ctx.getChild(0) as ParserRuleContext
            when (t.ruleIndex) {
                RULE_fregisters -> {
                    val c = t as FregistersContext
                    val registers = c.xregisters.text.toInt()
                    return RegisterInfo(registers, registers - insSize, insSize)
                }

                RULE_flocals -> {
                    val c = t as FlocalsContext
                    val locals = c.xlocals.text.toInt()
                    return RegisterInfo(locals + insSize, locals, insSize)
                }

                else -> {}
            }
        }

        throw RuntimeException("no registers / locals directive found")
    }

    private fun alignOffsetForPayload(codeOffset: Int): Int {
        return if (codeOffset % 2 == 1) {
            codeOffset + 1
        } else {
            codeOffset
        }
    }
}

internal data class RegisterInfo(val registers: Int, val locals: Int, val insSize: Int) {
    fun registerNumber(register: String): Int {
        return when (register.first()) {
            'v' -> register.substring(1).toInt()

            'p' -> {
                val number = register.substring(1).toInt()
                locals + number
            }

            else -> throw RuntimeException("unknown register format $register")
        }
    }
}
