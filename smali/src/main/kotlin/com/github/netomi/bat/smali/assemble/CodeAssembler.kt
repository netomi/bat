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
import com.github.netomi.bat.dexfile.debug.DebugInfo
import com.github.netomi.bat.dexfile.editor.DexComposer
import com.github.netomi.bat.dexfile.instruction.*
import com.github.netomi.bat.dexfile.io.InstructionWriter
import com.github.netomi.bat.dexfile.util.DexClasses
import com.github.netomi.bat.smali.parser.SmaliParser.*
import org.antlr.v4.runtime.ParserRuleContext

internal class CodeAssembler constructor(private val classDef:    ClassDef,
                                         private val method:      EncodedMethod,
                                         private val dexComposer: DexComposer) {

    private val dexFile: DexFile
        get() = dexComposer.dexFile

    fun parseCode(iCtx: List<SInstructionContext>, pCtx: List<SParameterContext>): Code {

        val instructions = mutableListOf<DexInstruction>()
        val tryElements  = mutableListOf<Try>()

        val registerInfo         = collectRegisterInfo(iCtx)
        val instructionAssembler = InstructionAssembler(iCtx, registerInfo, dexComposer)

        val parameters = method.getMethodID(dexFile).getProtoID(dexFile).parameters.typeCount
        val debugInfo = DebugInfo.empty(parameters)
        val debugSequenceAssembler = DebugSequenceAssembler(debugInfo)

        var codeOffset = 0

        pCtx.forEach { ctx ->
            val parameterNumber = registerInfo.parameterNumber(ctx.r.text)

            val parameterIndex = if (method.isStatic) {
                parameterNumber
            } else {
                parameterNumber - 1
            }

            val nameIndex = if (ctx.name != null) {
                dexComposer.addOrGetStringIDIndex(ctx.name.text.removeSurrounding("\""))
            } else {
                NO_INDEX
            }

            debugInfo.setParameterName(parameterIndex, nameIndex)
        }

        iCtx.forEach { ctx ->
            val t = ctx.getChild(0) as ParserRuleContext
            val insn: DexInstruction? = when (t.ruleIndex) {
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
                    debugSequenceAssembler.prologueEnd(codeOffset)
                    null
                }

                RULE_fepilogue  -> {
                    debugSequenceAssembler.epilogueStart(codeOffset)
                    null
                }

                RULE_fstartlocal -> {
                    val c = t as FstartlocalContext

                    val register = registerInfo.registerNumber(c.r.text)

                    var nameIndex: Int = NO_INDEX
                    var typeIndex: Int = NO_INDEX

                    val name = c.name.text.removeSurrounding("\"")
                    if (name.isNotEmpty()) {
                        nameIndex = dexComposer.addOrGetStringIDIndex(name)
                    }

                    if (c.type != null) {
                        val type = c.type.text
                        typeIndex = dexComposer.addOrGetTypeIDIndex(type)
                    }

                    val sigIndex = if (c.sig != null) {
                        dexComposer.addOrGetStringIDIndex(c.sig.text.removeSurrounding("\""))
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

                RULE_farraydata -> {
                    codeOffset = insertNopInstructionIfUnaligned(codeOffset, instructions)
                    instructionAssembler.parseArrayDataPayload(t as FarraydataContext)
                }

                RULE_fsparseswitch -> {
                    codeOffset = insertNopInstructionIfUnaligned(codeOffset, instructions)
                    instructionAssembler.parseSparseSwitchPayload(t as FsparseswitchContext, codeOffset)
                }

                RULE_fpackedswitch -> {
                    codeOffset = insertNopInstructionIfUnaligned(codeOffset, instructions)
                    instructionAssembler.parsePackedSwitchPayload(t as FpackedswitchContext, codeOffset)
                }

                RULE_fcatch -> {
                    val tryElement = instructionAssembler.parseCatchDirective(t as FcatchContext)
                    tryElements.add(tryElement)
                    null
                }

                RULE_fcatchall -> {
                    val tryElement = instructionAssembler.parseCatchAllDirective(t as FcatchallContext)
                    tryElements.add(tryElement)
                    null
                }

                RULE_f10x -> {
                    val c = t as F10xContext

                    val mnemonic = c.op.text
                    val opcode = DexOpCode.get(mnemonic)

                    opcode.createInstruction(0)
                }

                RULE_f12x_conversion   -> instructionAssembler.parseConversionInstructionF12x(t as F12x_conversionContext)
                RULE_f11x_basic        -> instructionAssembler.parseBasicInstructionF11x(t as F11x_basicContext)
                RULE_fx0t_branch       -> instructionAssembler.parseBranchInstructionFx0t(t as Fx0t_branchContext, codeOffset)
                RULE_f21t_branch       -> instructionAssembler.parseBranchInstructionF21t(t as F21t_branchContext, codeOffset)
                RULE_f22t_branch       -> instructionAssembler.parseBranchInstructionF22t(t as F22t_branchContext, codeOffset)
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
                RULE_f31t_payload      -> instructionAssembler.parsePayloadInstructionF31t(t as F31t_payloadContext, codeOffset)

                else -> null
            }

            insn?.apply {
                codeOffset += length
                instructions.add(this)
            }
        }

        debugSequenceAssembler.end()

        val code = Code.of(registerInfo.registers, registerInfo.insSize, 0)

        val insns = InstructionWriter.writeInstructions(instructions)

        code.insns     = insns
        code.insnsSize = insns.size

        val outgoingArgumentSizeCalculator = OutgoingArgumentSizeCalculator()
        code.instructionsAccept(dexFile, classDef, method, code, outgoingArgumentSizeCalculator)
        code.outsSize = outgoingArgumentSizeCalculator.outgoingArgumentSize

        code.tries = normalizeTries(tryElements)

        val handlerList = LinkedHashSet<EncodedCatchHandler>()
        for (tryElement in code.tries) {
            handlerList.add(tryElement.catchHandler)
        }

        code.catchHandlerList = handlerList.toList()

        if (!debugInfo.isEmpty) {
            code.debugInfo = debugInfo
        }

        return code
    }

    private fun normalizeTries(tryElements: MutableList<Try>): MutableList<Try> {
        if (tryElements.isEmpty()) {
            return tryElements
        }

        tryElements.sortBy { it.startAddr }

        val flattenedElements = mutableListOf<Try>()
        var lastTry: Try? = null
        for (tryElement in tryElements) {
            if (lastTry != null &&
                lastTry.startAddr == tryElement.startAddr &&
                lastTry.endAddr   == tryElement.endAddr) {
                flattenedElements.removeLast()
                flattenedElements.add(Try.of(lastTry.startAddr, lastTry.endAddr, lastTry.catchHandler.add(tryElement.catchHandler)))
            } else {
                flattenedElements.add(tryElement)
            }
            lastTry = tryElement
        }

        val sequence = mutableListOf<Seq>()
        flattenedElements.forEach {
            sequence.add(Seq(it.startAddr, SeqType.START, it))
            sequence.add(Seq(it.endAddr, SeqType.END, it))
        }

        sequence.sortWith(compareBy<Seq>{ it.addr }.thenByDescending { it.type })

        val nonOverlappingTries = mutableListOf<Try>()
        var currentTry: Try? = null

        for (seq in sequence) {
            when (seq.type) {
                SeqType.START -> {
                    currentTry = if (currentTry == null) {
                        seq.tryElement
                    } else {
                        val endingTry = Try.of(currentTry.startAddr, seq.addr - 1, currentTry.catchHandler)
                        nonOverlappingTries.add(endingTry)

                        val handler = seq.tryElement.catchHandler.add(currentTry.catchHandler)
                        val startingTry = Try.of(seq.addr, currentTry.endAddr, handler)
                        startingTry
                    }
                }
                SeqType.END -> {
                    if (currentTry != null) {
                        currentTry = if (currentTry.endAddr == seq.addr) {
                            nonOverlappingTries.add(currentTry)
                            null
                        } else {
                            if (currentTry.startAddr < seq.addr - 1) {
                                val endingTry = Try.of(currentTry.startAddr, seq.addr - 1, currentTry.catchHandler)
                                nonOverlappingTries.add(endingTry)
                            }

                            val handler = currentTry.catchHandler.subtract(seq.tryElement.catchHandler)
                            val startingTry = Try.of(seq.addr, currentTry.endAddr, handler)
                            startingTry
                        }
                    } else {
                        if (nonOverlappingTries.last().endAddr != seq.addr) {
                            throw RuntimeException("not expected")
                        }
                    }
                }
            }
        }

        return nonOverlappingTries
    }

    private fun collectRegisterInfo(listCtx: List<SInstructionContext>): RegisterInfo {

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

    private fun insertNopInstructionIfUnaligned(codeOffset: Int, instructions: MutableList<DexInstruction>): Int {
        if (codeOffset % 2 == 1) {
            val nop = BasicInstruction.of(DexOpCode.NOP)
            instructions.add(nop)
            return codeOffset + nop.length
        }

        return codeOffset
    }
}

internal data class RegisterInfo(val registers: Int, val locals: Int, val insSize: Int) {
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

    fun parameterNumber(register: String): Int {
        return when (register.first()) {
            'p' -> {
                register.substring(1).toInt()
            }

            else -> throw RuntimeException("unknown register format $register")
        }
    }
}

data class Seq(val addr: Int, val type: SeqType, val tryElement: Try)

enum class SeqType {
    START,
    END
}

private fun EncodedCatchHandler.subtract(other: EncodedCatchHandler): EncodedCatchHandler {
    var newCatchAllAddr = catchAllAddr
    if (other.catchAllAddr != NO_INDEX) {
        newCatchAllAddr = NO_INDEX
    }

    val newHandlers = LinkedHashSet(handlers) - other.handlers.toSet()
    return EncodedCatchHandler.of(newCatchAllAddr, newHandlers.toList())
}

private fun EncodedCatchHandler.add(other: EncodedCatchHandler): EncodedCatchHandler {
    var newCatchAllAddr = catchAllAddr
    if (newCatchAllAddr == NO_INDEX) {
        newCatchAllAddr = other.catchAllAddr
    }

    val newHandlers = handlers + other.handlers
    return EncodedCatchHandler.of(newCatchAllAddr, newHandlers)
}
