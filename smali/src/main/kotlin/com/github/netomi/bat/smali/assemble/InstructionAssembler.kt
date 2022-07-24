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
import com.github.netomi.bat.dexfile.value.*
import com.github.netomi.bat.smali.parser.SmaliParser.*
import com.github.netomi.bat.util.toIntArray
import org.antlr.v4.runtime.ParserRuleContext

internal class InstructionAssembler internal constructor(            listCtx:               List<SInstructionContext>,
                                                         private val registerInfo:          RegisterInfo,
                                                         private val dexComposer:           DexComposer) {

    private val encodedValueAssembler = EncodedValueAssembler(dexComposer)

    private val labelMapping:        MutableMap<String, Int> = LinkedHashMap()
    private val payloadLabelMapping: MutableMap<String, Int> = HashMap()

    private val dexFile: DexFile
        get() = dexComposer.dexFile

    init {
        collectLabels(listCtx)
    }

    private fun collectLabels(listCtx: List<SInstructionContext>): Pair<MutableMap<String, Int>, MutableMap<String, Int>> {
        var codeOffset = 0

        listCtx.forEach { ctx ->
            val t = ctx.getChild(0) as ParserRuleContext
            when (t.ruleIndex) {
                RULE_sLabel -> {
                    val c = t as SLabelContext
                    labelMapping[c.label.text] = codeOffset
                }

                RULE_farraydata -> {
                    codeOffset = alignPayloadLabel(codeOffset)
                    val insn = parseArrayDataPayload(t as FarraydataContext)
                    codeOffset += insn.length
                }

                RULE_fsparseswitch -> {
                    codeOffset = alignPayloadLabel(codeOffset)
                    val insn = parseSparseSwitchPayload(t as FsparseswitchContext, -1)
                    codeOffset += insn.length
                }

                RULE_fpackedswitch -> {
                    codeOffset = alignPayloadLabel(codeOffset)
                    val insn = parsePackedSwitchPayload(t as FpackedswitchContext, -1)
                    codeOffset += insn.length
                }

                else -> {
                    // check if it's a known instruction and advance the code offset
                    val mnemonic = t.getChild(0).text
                    val opcode   = DexOpCode.get(mnemonic)
                    if (opcode != null) {
                        val insn = opcode.createInstruction(0)
                        codeOffset += insn.length
                    }
                }
            }
        }

        return Pair(labelMapping, payloadLabelMapping)
    }

    private fun alignPayloadLabel(codeOffset: Int): Int {
        val lastLabel = labelMapping.entries.lastOrNull()
        if (lastLabel != null && lastLabel.value == codeOffset) {
            if (codeOffset % 2 == 1) {
                labelMapping[lastLabel.key] = codeOffset + 1
                return codeOffset + 1
            }
        }

        return codeOffset
    }

    fun parseConversionInstructionF12x(ctx: F12x_conversionContext): ConversionInstruction {
        val mnemonic = ctx.op.text
        val opcode = DexOpCode.get(mnemonic)

        val r1 = registerInfo.registerNumber(ctx.r1.text)
        val r2 = registerInfo.registerNumber(ctx.r2.text)

        return ConversionInstruction.of(opcode, r1, r2)
    }

    fun parseBasicInstructionF11x(ctx: F11x_basicContext): BasicInstruction {
        val mnemonic = ctx.op.text
        val opcode = DexOpCode.get(mnemonic)

        val r1 = registerInfo.registerNumber(ctx.r1.text)

        return BasicInstruction.of(opcode, r1)
    }

    fun parseBranchInstructionFx0t(ctx: Fx0t_branchContext, codeOffset: Int): BranchInstruction {
        val mnemonic = ctx.op.text
        val opcode   = DexOpCode.get(mnemonic)

        val label = ctx.target.label.text
        val branchOffset = branchOffset(codeOffset, label)

        return BranchInstruction.of(opcode, branchOffset)
    }

    fun parseBranchInstructionF21t(ctx: F21t_branchContext, codeOffset: Int): BranchInstruction {
        val mnemonic = ctx.op.text
        val opcode   = DexOpCode.get(mnemonic)

        val label = ctx.label.label.text
        val branchOffset = branchOffset(codeOffset, label)
        val r1 = registerInfo.registerNumber(ctx.r1.text)

        return BranchInstruction.of(opcode, branchOffset, r1)
    }

    fun parseBranchInstructionF22t(ctx: F22t_branchContext, codeOffset: Int): BranchInstruction {
        val mnemonic = ctx.op.text
        val opcode   = DexOpCode.get(mnemonic)

        val label = ctx.label.label.text
        val branchOffset = branchOffset(codeOffset, label)
        val r1 = registerInfo.registerNumber(ctx.r1.text)
        val r2 = registerInfo.registerNumber(ctx.r2.text)

        return BranchInstruction.of(opcode, branchOffset, r1, r2)
    }

    fun parseFieldInstructionF21c(ctx: F21c_fieldContext): FieldInstruction {
        val mnemonic = ctx.op.text
        val opcode   = DexOpCode.get(mnemonic)

        val register = registerInfo.registerNumber(ctx.r1.text)

        val field = ctx.fld.text
        val (classType, fieldName, fieldType) = parseFieldObject(field)

        val fieldIndex = dexComposer.addOrGetFieldIDIndex(classType!!, fieldName, fieldType)
        return FieldInstruction.of(opcode, fieldIndex, register)
    }

    fun parseFieldInstructionF22c(ctx: F22c_fieldContext): FieldInstruction {
        val mnemonic = ctx.op.text
        val opcode = DexOpCode.get(mnemonic)

        val r1 = registerInfo.registerNumber(ctx.r1.text)
        val r2 = registerInfo.registerNumber(ctx.r2.text)

        val field = ctx.fld.text
        val (classType, fieldName, fieldType) = parseFieldObject(field)

        val fieldIndex = dexComposer.addOrGetFieldIDIndex(classType!!, fieldName, fieldType)
        return FieldInstruction.of(opcode, fieldIndex, r1, r2)
    }

    fun parseLiteralInstructionInteger(ctx: Fconst_intContext): LiteralInstruction {
        val mnemonic = ctx.op.text
        val opcode = DexOpCode.get(mnemonic)

        val r1 = registerInfo.registerNumber(ctx.r1.text)
        var value = parseNumber(ctx.cst.text)

        if (mnemonic.contains("high16")) {
            val shift = if (opcode.targetsWideRegister()) 48 else 16
            if ((value shr shift) == 0L) {
                value = value shl shift
            }
        }

        return LiteralInstruction.of(opcode, value, r1)
    }

    fun parseLiteralInstructionString(ctx: Fconst_stringContext): StringInstruction {
        val mnemonic = ctx.op.text
        val opcode = DexOpCode.get(mnemonic)

        val r1 = registerInfo.registerNumber(ctx.r1.text)
        val stringIndex = dexComposer.addOrGetStringIDIndex(parseString(ctx.cst.text))

        return StringInstruction.of(opcode, stringIndex, r1)
    }

    fun parseLiteralInstructionType(ctx: Fconst_typeContext): TypeInstruction {
        val mnemonic = ctx.op.text
        val opcode = DexOpCode.get(mnemonic)

        val r1 = registerInfo.registerNumber(ctx.r1.text)
        val typeIndex = dexComposer.addOrGetTypeIDIndex(ctx.cst.text)

        return TypeInstruction.of(opcode, typeIndex, r1)
    }

    fun parseArithmeticInstructionF12x(ctx: F12x_arithmeticContext): ArithmeticInstruction {
        val mnemonic = ctx.op.text
        val opcode = DexOpCode.get(mnemonic)

        val r1 = registerInfo.registerNumber(ctx.r1.text)
        val r2 = registerInfo.registerNumber(ctx.r2.text)

        return ArithmeticInstruction.of(opcode, r1, r2)
    }

    fun parseArithmeticInstructionF23x(ctx: F23x_arithmeticContext): ArithmeticInstruction {
        val mnemonic = ctx.op.text
        val opcode = DexOpCode.get(mnemonic)

        val r1 = registerInfo.registerNumber(ctx.r1.text)
        val r2 = registerInfo.registerNumber(ctx.r2.text)
        val r3 = registerInfo.registerNumber(ctx.r3.text)

        return ArithmeticInstruction.of(opcode, r1, r2, r3)
    }

    fun parseArithmeticLiteralInstructionF22sb(ctx: F22sb_arithmeticContext): ArithmeticInstruction {
        val mnemonic = ctx.op.text
        val opcode = DexOpCode.get(mnemonic)

        val r1 = registerInfo.registerNumber(ctx.r1.text)
        val r2 = registerInfo.registerNumber(ctx.r2.text)
        val value = parseNumber(ctx.lit.text).toInt()

        return ArithmeticLiteralInstruction.of(opcode, value, r1, r2)
    }

    fun parseBasicInstructionCompareF23x(ctx: F23x_compareContext): BasicInstruction {
        val mnemonic = ctx.op.text
        val opcode = DexOpCode.get(mnemonic)

        val r1 = registerInfo.registerNumber(ctx.r1.text)
        val r2 = registerInfo.registerNumber(ctx.r2.text)
        val r3 = registerInfo.registerNumber(ctx.r3.text)

        return BasicInstruction.of(opcode, r1, r2, r3)
    }

    fun parseBasicInstructionMoveFx2x(ctx: Fx2x_moveContext): BasicInstruction {
        val mnemonic = ctx.op.text
        val opcode = DexOpCode.get(mnemonic)

        val r1 = registerInfo.registerNumber(ctx.r1.text)
        val r2 = registerInfo.registerNumber(ctx.r2.text)

        return BasicInstruction.of(opcode, r1, r2)
    }

    fun parseMethodInstructionF35c(ctx: F35c_methodContext): MethodInstruction {
        val mnemonic = ctx.op.text
        val opcode = DexOpCode.get(mnemonic)
        val registers = ctx.REGISTER().map { registerInfo.registerNumber(it.text) }.toIntArray()

        val methodType = ctx.method.text

        val (classType, methodName, parameterTypes, returnType) = parseMethodObject(methodType)
        val methodIndex = dexComposer.addOrGetMethodIDIndex(classType!!, methodName, parameterTypes, returnType)

        return MethodInstruction.of(opcode, methodIndex, *registers)
    }

    fun parseMethodInstructionF3rc(ctx: F3rc_methodContext): MethodInstruction {
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

    fun parseArrayInstructionF23x(ctx: F23x_arrayContext): ArrayInstruction {
        val mnemonic = ctx.op.text
        val opcode = DexOpCode.get(mnemonic)

        val r1 = registerInfo.registerNumber(ctx.r1.text)
        val r2 = registerInfo.registerNumber(ctx.r2.text)
        val r3 = registerInfo.registerNumber(ctx.r3.text)

        return ArrayInstruction.of(opcode, r1, r2, r3)
    }

    fun parseArrayInstructionF12x(ctx: F12x_arrayContext): ArrayInstruction {
        val mnemonic = ctx.op.text
        val opcode = DexOpCode.get(mnemonic)

        val r1 = registerInfo.registerNumber(ctx.r1.text)
        val r2 = registerInfo.registerNumber(ctx.r2.text)

        return ArrayInstruction.of(opcode, r1, r2)
    }

    fun parseTypeInstructionFt2c(ctx: Ft2c_typeContext): TypeInstruction {
        val mnemonic = ctx.op.text
        val opcode = DexOpCode.get(mnemonic)

        val r1 = registerInfo.registerNumber(ctx.r1.text)
        val r2 = registerInfo.registerNumber(ctx.r2.text)

        val typeIndex = dexComposer.addOrGetTypeIDIndex(ctx.type.text)

        return TypeInstruction.of(opcode, typeIndex, r1, r2)
    }

    fun parseArrayTypeInstructionF35c(ctx: F35c_arrayContext): ArrayTypeInstruction {
        val mnemonic = ctx.op.text
        val opcode = DexOpCode.get(mnemonic)

        val registers = mutableListOf<Int>()
        ctx.REGISTER().forEach { registers.add(registerInfo.registerNumber(it.text)) }

        val typeIndex = dexComposer.addOrGetTypeIDIndex(ctx.type.text)

        return ArrayTypeInstruction.of(opcode, typeIndex, *registers.toIntArray())
    }

    fun parseArrayTypeInstructionF3rc(ctx: F3rc_arrayContext): ArrayTypeInstruction {
        val mnemonic = ctx.op.text
        val opcode = DexOpCode.get(mnemonic)

        val rStart = registerInfo.registerNumber(ctx.rstart.text)
        val rEnd   = registerInfo.registerNumber(ctx.rend.text)

        val registers = (rStart..rEnd).toIntArray()
        val typeIndex = dexComposer.addOrGetTypeIDIndex(ctx.type.text)

        return ArrayTypeInstruction.of(opcode, typeIndex, *registers)
    }

    fun parseMethodProtoInstructionF45cc(ctx: F45cc_methodprotoContext): MethodProtoInstruction {
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

    fun parseMethodProtoInstructionF4rcc(ctx: F4rcc_methodprotoContext): MethodProtoInstruction {
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

    fun parseMethodHandleInstructionF21c(ctx: F21c_const_handleContext): MethodHandleRefInstruction {
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

        val methodHandleIndex = dexComposer.dexFile.addMethodHandle(methodHandle)

        return MethodHandleRefInstruction.of(opcode, methodHandleIndex, r1)
    }

    fun parseMethodTypeInstructionF21c(ctx: F21c_const_typeContext): MethodTypeRefInstruction {
        val mnemonic = ctx.op.text
        val opcode = DexOpCode.get(mnemonic)

        val r1 = registerInfo.registerNumber(ctx.r1.text)

        val protoIndex = ctx.proto.text.let {
            val (_, _, parameterTypes, returnType) = parseMethodObject(it)
            dexComposer.addOrGetProtoIDIndex(parameterTypes, returnType)
        }

        return MethodTypeRefInstruction.of(opcode, protoIndex, r1)
    }

    fun parseCallSiteInstructionF35c(ctx: F35c_customContext): CallSiteInstruction {
        val mnemonic = ctx.op.text
        val opcode = DexOpCode.get(mnemonic)

        val registers = mutableListOf<Int>()
        ctx.REGISTER().forEach { registers.add(registerInfo.registerNumber(it.text)) }

        // id is ignored for now
        //val id = ctx.ID().text

        val encodedValues = mutableListOf<EncodedValue>()
        ctx.sBaseValue().forEach { encodedValues.add(encodedValueAssembler.parseBaseValue(it)) }

        val (classType, name, parameterTypes, returnType) = parseMethodObject(ctx.method.text)
        val methodIndex = dexComposer.addOrGetMethodIDIndex(classType!!, name, parameterTypes, returnType)
        val bootstrapMethodHandleIndex =
            dexFile.addMethodHandle(MethodHandle.of(MethodHandleType.INVOKE_STATIC, methodIndex))

        val callSite      = CallSite.of(bootstrapMethodHandleIndex, *encodedValues.toTypedArray())
        val callSiteIndex = dexFile.addCallSiteID(CallSiteID.of(callSite))

        return CallSiteInstruction.of(opcode, callSiteIndex, *registers.toIntArray())
    }

    fun parseCallSiteInstructionF3rc(ctx: F3rc_customContext): CallSiteInstruction {
        val mnemonic = ctx.op.text
        val opcode = DexOpCode.get(mnemonic)

        val rStart = registerInfo.registerNumber(ctx.rstart.text)
        val rEnd   = registerInfo.registerNumber(ctx.rend.text)

        val registers = (rStart..rEnd).toIntArray()

        // id is ignored for now
        //val id = ctx.ID().text

        val encodedValues = mutableListOf<EncodedValue>()
        ctx.sBaseValue().forEach { encodedValues.add(encodedValueAssembler.parseBaseValue(it)) }

        val (classType, name, parameterTypes, returnType) = parseMethodObject(ctx.method.text)
        val methodIndex = dexComposer.addOrGetMethodIDIndex(classType!!, name, parameterTypes, returnType)
        val bootstrapMethodHandleIndex =
            dexFile.addMethodHandle(MethodHandle.of(MethodHandleType.INVOKE_STATIC, methodIndex))

        val callSite      = CallSite.of(bootstrapMethodHandleIndex, *encodedValues.toTypedArray())
        val callSiteIndex = dexFile.addCallSiteID(CallSiteID.of(callSite))

        return CallSiteInstruction.of(opcode, callSiteIndex, *registers)
    }

    fun parsePayloadInstructionF31t(ctx: F31t_payloadContext, codeOffset: Int): PayloadInstruction {
        val mnemonic = ctx.op.text
        val opcode = DexOpCode.get(mnemonic)

        val r1 = registerInfo.registerNumber(ctx.r1.text)

        val label = ctx.label.label.text
        val payloadOffset = branchOffset(codeOffset, label)
        payloadLabelMapping[label] = codeOffset

        return PayloadInstruction.of(opcode, payloadOffset, r1)
    }

    fun parseArrayDataPayload(ctx: FarraydataContext): FillArrayPayload {
        val elementWidth = ctx.size.text.toInt()

        val encodedValues = mutableListOf<EncodedValue>()
        ctx.sBaseValue().forEach { encodedValues.add(encodedValueAssembler.parseBaseValue(it)) }

        return when (elementWidth) {
            1 -> FillArrayPayload.of(encodedValues.map { (it as EncodedByteValue).value }.toByteArray())
            2 -> FillArrayPayload.of(encodedValues.map { (it as EncodedShortValue).value }.toShortArray())
            4 -> FillArrayPayload.of(encodedValues.map { (it as EncodedIntValue).value }.toIntArray())
            8 -> FillArrayPayload.of(encodedValues.map { (it as EncodedLongValue).value }.toLongArray())
            else -> throw RuntimeException("unexpected elementWidth $elementWidth")
        }
    }

    fun parseSparseSwitchPayload(ctx: FsparseswitchContext, codeOffset: Int): SparseSwitchPayload {
        val switchOffset = if (codeOffset == -1) { 0 } else {
            val payloadLabel = labelMapping.entries.find { it.value == codeOffset }?.key ?: throw RuntimeException("unknown label for payload")
            payloadLabelMapping[payloadLabel] ?: throw RuntimeException("payload not referenced")
        }

        val keys = IntArray(ctx.INT().size)
        ctx.INT().forEachIndexed { index, node -> keys[index] = node.text.toInt() }

        val branchTargets = IntArray(ctx.sLabel().size)
        ctx.sLabel().forEachIndexed { index, node -> branchTargets[index] = branchOffset(switchOffset, node.label.text) }

        return SparseSwitchPayload.of(keys, branchTargets)
    }

    fun parsePackedSwitchPayload(ctx: FpackedswitchContext, codeOffset: Int): PackedSwitchPayload {
        val switchOffset = if (codeOffset == -1) { 0 } else {
            val payloadLabel = labelMapping.entries.find { it.value == codeOffset }?.key ?: throw RuntimeException("unknown label for payload")
            payloadLabelMapping[payloadLabel] ?: throw RuntimeException("payload not referenced")
        }

        val firstKey = ctx.start.text.toInt()

        val branchTargets = IntArray(ctx.sLabel().size)
        ctx.sLabel().forEachIndexed { index, node -> branchTargets[index] = branchOffset(switchOffset, node.label.text) }

        return PackedSwitchPayload.of(firstKey, branchTargets)
    }

    fun parseCatchDirective(ctx: FcatchContext): Try {
        val exceptionType = ctx.type.text
        val exceptionTypeIndex = dexComposer.addOrGetTypeIDIndex(exceptionType)

        val handler  = ctx.handle.label.text
        val handlerOffset = labelMapping[handler]!!

        val typeAddrList     = listOf(TypeAddrPair.of(exceptionTypeIndex, handlerOffset))
        val exceptionHandler = EncodedCatchHandler.of(typeAddrList)

        val tryStart = ctx.start.label.text
        val tryEnd   = ctx.end.label.text

        val startOffset = labelMapping[tryStart]!!
        val endOffset   = labelMapping[tryEnd]!!

        return Try.of(startOffset, endOffset - 1, exceptionHandler)
    }

    fun parseCatchAllDirective(ctx: FcatchallContext): Try {
        val handler  = ctx.handle.label.text
        val handlerOffset = labelMapping[handler]!!

        val exceptionHandler = EncodedCatchHandler.of(handlerOffset)

        val tryStart = ctx.start.label.text
        val tryEnd   = ctx.end.label.text

        val startOffset = labelMapping[tryStart]!!
        val endOffset   = labelMapping[tryEnd]!!

        return Try.of(startOffset, endOffset - 1, exceptionHandler)
    }

    private fun branchOffset(currentOffset: Int, target: String): Int {
        val targetOffset = labelMapping[target] ?: throw RuntimeException("unknown label $target")
        return targetOffset - currentOffset
    }
}