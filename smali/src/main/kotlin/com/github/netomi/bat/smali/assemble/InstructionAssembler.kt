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
import com.github.netomi.bat.dexfile.editor.DexEditor
import com.github.netomi.bat.dexfile.instruction.*
import com.github.netomi.bat.dexfile.value.*
import com.github.netomi.bat.smali.parser.SmaliParser.*
import com.github.netomi.bat.util.toIntArray

internal class InstructionAssembler internal constructor(private val registerInfo: RegisterInfo,
                                                         private val dexEditor:    DexEditor) {

    private val encodedValueAssembler = EncodedValueAssembler(dexEditor)

    fun parseConversionInstructionF12x(ctx: F12x_conversionContext): ConversionInstruction {
        val mnemonic = ctx.op.text
        val opcode   = DexOpCode[mnemonic]

        val r1 = registerInfo.registerNumber(ctx.r1.text)
        val r2 = registerInfo.registerNumber(ctx.r2.text)

        return ConversionInstruction.of(opcode, r1, r2)
    }

    fun parseMoveInstructionF11x(ctx: F11x_moveContext): MoveInstruction {
        val mnemonic = ctx.op.text
        val opcode   = DexOpCode[mnemonic]

        val r1 = registerInfo.registerNumber(ctx.r1.text)

        return MoveInstruction.of(opcode, r1)
    }

    fun parseReturnInstructionF11x(ctx: F11x_returnContext): ReturnInstruction {
        val mnemonic = ctx.op.text
        val opcode   = DexOpCode[mnemonic]

        val r1 = registerInfo.registerNumber(ctx.r1.text)

        return ReturnInstruction.of(opcode, r1)
    }

    fun parseMonitorInstructionF11x(ctx: F11x_monitorContext): MonitorInstruction {
        val mnemonic = ctx.op.text
        val opcode   = DexOpCode[mnemonic]

        val r1 = registerInfo.registerNumber(ctx.r1.text)

        return MonitorInstruction.of(opcode, r1)
    }

    fun parseExceptionInstructionF11x(ctx: F11x_exceptionContext): ExceptionInstruction {
        val mnemonic = ctx.op.text
        val opcode   = DexOpCode[mnemonic]

        val r1 = registerInfo.registerNumber(ctx.r1.text)

        return ExceptionInstruction.of(opcode, r1)
    }

    fun parseBranchInstructionFx0t(ctx: Fx0t_branchContext): BranchInstruction {
        val mnemonic = ctx.op.text
        val opcode   = DexOpCode[mnemonic]
        val label    = ctx.target.label.text
        return BranchInstruction.of(opcode, label)
    }

    fun parseBranchInstructionF21t(ctx: F21t_branchContext): BranchInstruction {
        val mnemonic = ctx.op.text
        val opcode   = DexOpCode[mnemonic]

        val label = ctx.label.label.text
        val r1 = registerInfo.registerNumber(ctx.r1.text)

        return BranchInstruction.of(opcode, label, r1)
    }

    fun parseBranchInstructionF22t(ctx: F22t_branchContext): BranchInstruction {
        val mnemonic = ctx.op.text
        val opcode   = DexOpCode[mnemonic]

        val label = ctx.label.label.text
        val r1 = registerInfo.registerNumber(ctx.r1.text)
        val r2 = registerInfo.registerNumber(ctx.r2.text)

        return BranchInstruction.of(opcode, label, r1, r2)
    }

    fun parseFieldInstructionF21c(ctx: F21c_fieldContext): FieldInstruction {
        val mnemonic = ctx.op.text
        val opcode   = DexOpCode[mnemonic]

        val register = registerInfo.registerNumber(ctx.r1.text)

        val field = ctx.fld.text
        val (classType, fieldName, fieldType) = parseFieldObject(field)

        val fieldIndex = dexEditor.addOrGetFieldIDIndex(classType!!, fieldName, fieldType)
        return FieldInstruction.of(opcode, fieldIndex, register)
    }

    fun parseFieldInstructionF22c(ctx: F22c_fieldContext): FieldInstruction {
        val mnemonic = ctx.op.text
        val opcode = DexOpCode[mnemonic]

        val r1 = registerInfo.registerNumber(ctx.r1.text)
        val r2 = registerInfo.registerNumber(ctx.r2.text)

        val field = ctx.fld.text
        val (classType, fieldName, fieldType) = parseFieldObject(field)

        val fieldIndex = dexEditor.addOrGetFieldIDIndex(classType!!, fieldName, fieldType)
        return FieldInstruction.of(opcode, fieldIndex, r1, r2)
    }

    fun parseLiteralInstructionInteger(ctx: Fconst_intContext): LiteralInstruction {
        val mnemonic = ctx.op.text
        val opcode = DexOpCode[mnemonic]

        val r1 = registerInfo.registerNumber(ctx.r1.text)
        var value = parseLiteral(ctx.cst.text)

        if (mnemonic.contains("high16")) {
            val shift = if (opcode.targetsWideRegister) 48 else 16
            if ((value shr shift) == 0L) {
                value = value shl shift
            }
        }

        return LiteralInstruction.of(opcode, value, r1)
    }

    fun parseLiteralInstructionString(ctx: Fconst_stringContext): StringInstruction {
        val mnemonic = ctx.op.text
        val opcode = DexOpCode[mnemonic]

        val r1 = registerInfo.registerNumber(ctx.r1.text)
        val string = parseString(ctx.cst.text)
        val stringIndex = dexEditor.addOrGetStringIDIndex(string)

        return StringInstruction.of(opcode, stringIndex, r1)
    }

    fun parseLiteralInstructionType(ctx: Fconst_typeContext): TypeInstruction {
        val mnemonic = ctx.op.text
        val opcode = DexOpCode[mnemonic]

        val r1 = registerInfo.registerNumber(ctx.r1.text)
        val typeIndex = dexEditor.addOrGetTypeIDIndex(ctx.cst.text)

        return TypeInstruction.of(opcode, typeIndex, r1)
    }

    fun parseArithmeticInstructionF12x(ctx: F12x_arithmeticContext): ArithmeticInstruction {
        val mnemonic = ctx.op.text
        val opcode = DexOpCode[mnemonic]

        val r1 = registerInfo.registerNumber(ctx.r1.text)
        val r2 = registerInfo.registerNumber(ctx.r2.text)

        return ArithmeticInstruction.of(opcode, r1, r2)
    }

    fun parseArithmeticInstructionF23x(ctx: F23x_arithmeticContext): ArithmeticInstruction {
        val mnemonic = ctx.op.text
        val opcode = DexOpCode[mnemonic]

        val r1 = registerInfo.registerNumber(ctx.r1.text)
        val r2 = registerInfo.registerNumber(ctx.r2.text)
        val r3 = registerInfo.registerNumber(ctx.r3.text)

        return ArithmeticInstruction.of(opcode, r1, r2, r3)
    }

    fun parseArithmeticLiteralInstructionF22sb(ctx: F22sb_arithmeticContext): ArithmeticInstruction {
        val mnemonic = ctx.op.text
        val opcode = DexOpCode[mnemonic]

        val r1 = registerInfo.registerNumber(ctx.r1.text)
        val r2 = registerInfo.registerNumber(ctx.r2.text)
        val value = parseLiteral(ctx.lit.text).toInt()

        return ArithmeticLiteralInstruction.of(opcode, value, r1, r2)
    }

    fun parseCompareInstructionF23x(ctx: F23x_compareContext): CompareInstruction {
        val mnemonic = ctx.op.text
        val opcode = DexOpCode[mnemonic]

        val r1 = registerInfo.registerNumber(ctx.r1.text)
        val r2 = registerInfo.registerNumber(ctx.r2.text)
        val r3 = registerInfo.registerNumber(ctx.r3.text)

        return CompareInstruction.of(opcode, r1, r2, r3)
    }

    fun parseMoveInstructionFx2x(ctx: Fx2x_moveContext): MoveInstruction {
        val mnemonic = ctx.op.text
        val opcode = DexOpCode[mnemonic]

        val r1 = registerInfo.registerNumber(ctx.r1.text)
        val r2 = registerInfo.registerNumber(ctx.r2.text)

        return MoveInstruction.of(opcode, r1, r2)
    }

    fun parseMethodInstructionF35c(ctx: F35c_methodContext): MethodInstruction {
        val mnemonic = ctx.op.text
        val opcode = DexOpCode[mnemonic]
        val registers = ctx.REGISTER().map { registerInfo.registerNumber(it.text) }.toIntArray()

        val methodType = ctx.method.text

        val (classType, methodName, parameterTypes, returnType) = parseMethodObject(methodType)
        val methodIndex = dexEditor.addOrGetMethodIDIndex(classType!!, methodName, parameterTypes, returnType)

        return MethodInstruction.of(opcode, methodIndex, *registers)
    }

    fun parseMethodInstructionF3rc(ctx: F3rc_methodContext): MethodInstruction {
        val mnemonic = ctx.op.text
        val opcode = DexOpCode[mnemonic]

        val rStart = registerInfo.registerNumber(ctx.rstart.text)
        val rEnd   = registerInfo.registerNumber(ctx.rend.text)

        val methodType = ctx.method.text

        val (classType, methodName, parameterTypes, returnType) = parseMethodObject(methodType)
        val methodIndex = dexEditor.addOrGetMethodIDIndex(classType!!, methodName, parameterTypes, returnType)

        val registers = (rStart..rEnd).toIntArray()
        return MethodInstruction.of(opcode, methodIndex, *registers)
    }

    fun parseArrayInstructionF23x(ctx: F23x_arrayContext): ArrayInstruction {
        val mnemonic = ctx.op.text
        val opcode = DexOpCode[mnemonic]

        val r1 = registerInfo.registerNumber(ctx.r1.text)
        val r2 = registerInfo.registerNumber(ctx.r2.text)
        val r3 = registerInfo.registerNumber(ctx.r3.text)

        return ArrayInstruction.of(opcode, r1, r2, r3)
    }

    fun parseArrayInstructionF12x(ctx: F12x_arrayContext): ArrayInstruction {
        val mnemonic = ctx.op.text
        val opcode = DexOpCode[mnemonic]

        val r1 = registerInfo.registerNumber(ctx.r1.text)
        val r2 = registerInfo.registerNumber(ctx.r2.text)

        return ArrayInstruction.of(opcode, r1, r2)
    }

    fun parseTypeInstructionFt2c(ctx: Ft2c_typeContext): TypeInstruction {
        val mnemonic = ctx.op.text
        val opcode = DexOpCode[mnemonic]

        val r1 = registerInfo.registerNumber(ctx.r1.text)
        val r2 = registerInfo.registerNumber(ctx.r2.text)

        val typeIndex = dexEditor.addOrGetTypeIDIndex(ctx.type.text)

        return TypeInstruction.of(opcode, typeIndex, r1, r2)
    }

    fun parseArrayTypeInstructionF35c(ctx: F35c_arrayContext): ArrayTypeInstruction {
        val mnemonic = ctx.op.text
        val opcode = DexOpCode[mnemonic]

        val registers = mutableListOf<Int>()
        ctx.REGISTER().forEach { registers.add(registerInfo.registerNumber(it.text)) }

        val typeIndex = dexEditor.addOrGetTypeIDIndex(ctx.type.text)

        return ArrayTypeInstruction.of(opcode, typeIndex, *registers.toIntArray())
    }

    fun parseArrayTypeInstructionF3rc(ctx: F3rc_arrayContext): ArrayTypeInstruction {
        val mnemonic = ctx.op.text
        val opcode = DexOpCode[mnemonic]

        val rStart = registerInfo.registerNumber(ctx.rstart.text)
        val rEnd   = registerInfo.registerNumber(ctx.rend.text)

        val registers = (rStart..rEnd).toIntArray()
        val typeIndex = dexEditor.addOrGetTypeIDIndex(ctx.type.text)

        return ArrayTypeInstruction.of(opcode, typeIndex, *registers)
    }

    fun parseMethodProtoInstructionF45cc(ctx: F45cc_methodprotoContext): MethodProtoInstruction {
        val mnemonic = ctx.op.text
        val opcode = DexOpCode[mnemonic]

        val registers = mutableListOf<Int>()
        ctx.REGISTER().forEach { registers.add(registerInfo.registerNumber(it.text)) }

        val methodIndex = ctx.method.text.let {
            val (classType, methodName, parameterTypes, returnType) = parseMethodObject(it)
            dexEditor.addOrGetMethodIDIndex(classType!!, methodName, parameterTypes, returnType)
        }

        val protoIndex = ctx.proto.text.let {
            val (_, _, parameterTypes, returnType) = parseMethodObject(it)
            dexEditor.addOrGetProtoIDIndex(parameterTypes, returnType)
        }

        return MethodProtoInstruction.of(opcode, methodIndex, protoIndex, *registers.toIntArray())
    }

    fun parseMethodProtoInstructionF4rcc(ctx: F4rcc_methodprotoContext): MethodProtoInstruction {
        val mnemonic = ctx.op.text
        val opcode = DexOpCode[mnemonic]

        val rStart = registerInfo.registerNumber(ctx.rstart.text)
        val rEnd   = registerInfo.registerNumber(ctx.rend.text)

        val registers = (rStart..rEnd).toIntArray()

        val methodIndex = ctx.method.text.let {
            val (classType, methodName, parameterTypes, returnType) = parseMethodObject(it)
            dexEditor.addOrGetMethodIDIndex(classType!!, methodName, parameterTypes, returnType)
        }

        val protoIndex = ctx.proto.text.let {
            val (_, _, parameterTypes, returnType) = parseMethodObject(it)
            dexEditor.addOrGetProtoIDIndex(parameterTypes, returnType)
        }

        return MethodProtoInstruction.of(opcode, methodIndex, protoIndex, *registers)
    }

    fun parseMethodHandleInstructionF21c(ctx: F21c_const_handleContext): MethodHandleRefInstruction {
        val mnemonic = ctx.op.text
        val opcode = DexOpCode[mnemonic]

        val r1 = registerInfo.registerNumber(ctx.r1.text)

        val methodHandleType =
            MethodHandleType.of(ctx.methodHandleType.text)

        val methodHandle = if (methodHandleType.targetsField) {
            val fieldIndex = ctx.fieldOrMethod.text.let {
                val (classType, name, type) = parseFieldObject(it)
                dexEditor.addOrGetFieldIDIndex(classType!!, name, type)
            }
            MethodHandle.of(methodHandleType, fieldIndex)
        } else {
            val methodIndex = ctx.fieldOrMethod.text.let {
                val (classType, methodName, parameterTypes, returnType) = parseMethodObject(it)
                dexEditor.addOrGetMethodIDIndex(classType!!, methodName, parameterTypes, returnType)
            }
            MethodHandle.of(methodHandleType, methodIndex)
        }

        val methodHandleIndex = dexEditor.addOrGetMethodHandleIndex(methodHandle)
        return MethodHandleRefInstruction.of(opcode, methodHandleIndex, r1)
    }

    fun parseMethodTypeInstructionF21c(ctx: F21c_const_typeContext): MethodTypeRefInstruction {
        val mnemonic = ctx.op.text
        val opcode = DexOpCode[mnemonic]

        val r1 = registerInfo.registerNumber(ctx.r1.text)

        val protoIndex = ctx.proto.text.let {
            val (_, _, parameterTypes, returnType) = parseMethodObject(it)
            dexEditor.addOrGetProtoIDIndex(parameterTypes, returnType)
        }

        return MethodTypeRefInstruction.of(opcode, protoIndex, r1)
    }

    fun parseCallSiteInstructionF35c(ctx: F35c_customContext): CallSiteInstruction {
        val mnemonic = ctx.op.text
        val opcode = DexOpCode[mnemonic]

        val registers = mutableListOf<Int>()
        ctx.REGISTER().forEach { registers.add(registerInfo.registerNumber(it.text)) }

        // id is ignored for now
        //val id = ctx.ID().text

        val encodedValues = mutableListOf<EncodedValue>()
        ctx.sBaseValue().forEach { encodedValues.add(encodedValueAssembler.parseBaseValue(it)) }

        val (classType, name, parameterTypes, returnType) = parseMethodObject(ctx.method.text)
        val methodIndex = dexEditor.addOrGetMethodIDIndex(classType!!, name, parameterTypes, returnType)
        val bootstrapMethodHandleIndex =
            dexEditor.addOrGetMethodHandleIndex(MethodHandle.of(MethodHandleType.INVOKE_STATIC, methodIndex))

        val callSite        = CallSite.of(bootstrapMethodHandleIndex, *encodedValues.toTypedArray())
        val callSiteIDIndex = dexEditor.addOrGetCallSiteIDIndex(callSite)

        return CallSiteInstruction.of(opcode, callSiteIDIndex, *registers.toIntArray())
    }

    fun parseCallSiteInstructionF3rc(ctx: F3rc_customContext): CallSiteInstruction {
        val mnemonic = ctx.op.text
        val opcode = DexOpCode[mnemonic]

        val rStart = registerInfo.registerNumber(ctx.rstart.text)
        val rEnd   = registerInfo.registerNumber(ctx.rend.text)

        val registers = (rStart..rEnd).toIntArray()

        // id is ignored for now
        //val id = ctx.ID().text

        val encodedValues = mutableListOf<EncodedValue>()
        ctx.sBaseValue().forEach { encodedValues.add(encodedValueAssembler.parseBaseValue(it)) }

        val (classType, name, parameterTypes, returnType) = parseMethodObject(ctx.method.text)
        val methodIndex = dexEditor.addOrGetMethodIDIndex(classType!!, name, parameterTypes, returnType)
        val bootstrapMethodHandleIndex =
            dexEditor.addOrGetMethodHandleIndex(MethodHandle.of(MethodHandleType.INVOKE_STATIC, methodIndex))

        val callSite        = CallSite.of(bootstrapMethodHandleIndex, *encodedValues.toTypedArray())
        val callSiteIDIndex = dexEditor.addOrGetCallSiteIDIndex(callSite)

        return CallSiteInstruction.of(opcode, callSiteIDIndex, *registers)
    }

    fun parsePayloadInstructionF31t(ctx: F31t_payloadContext, payloadMapping: Map<String, Payload>): PayloadInstruction<*> {
        val mnemonic = ctx.op.text
        val opcode = DexOpCode[mnemonic]

        val r1    = registerInfo.registerNumber(ctx.r1.text)
        val label = ctx.label.label.text

        val payload = payloadMapping[label] ?: parserError(ctx, "unknown payload label $label")
        return PayloadInstruction.of(opcode, payload, r1)
    }

    fun parseArrayDataPayload(ctx: FarraydataContext): FillArrayPayload {
        val elementWidth = ctx.size.text.toInt()

        val encodedValues = mutableListOf<EncodedValue>()
        ctx.sBaseValue().forEach { encodedValues.add(encodedValueAssembler.parseBaseValue(it)) }

        return when (elementWidth) {
            1 -> FillArrayPayload.of(encodedValues.map { (it as EncodedByteValue).value }.toByteArray())
            2 -> FillArrayPayload.of(encodedValues.map { (it as EncodedShortValue).value }.toShortArray())
            4 -> FillArrayPayload.of(encodedValues.map { (it as EncodedIntValue).value }.toIntArray())
            8 -> FillArrayPayload.of(encodedValues.map {
                if (it is EncodedIntValue) it.value.toLong() else (it as EncodedLongValue).value
            }.toLongArray())
            else -> throw RuntimeException("unexpected elementWidth $elementWidth")
        }
    }

    fun parseSparseSwitchPayload(ctx: FsparseswitchContext): SparseSwitchPayload {
        val keys         = IntArray(ctx.INT().size) { i -> parseInt(ctx.INT(i).text) }
        val branchLabels = Array<String>(ctx.sLabel().size) { i -> ctx.sLabel(i).label.text }
        return SparseSwitchPayload.of(keys, branchLabels)
    }

    fun parsePackedSwitchPayload(ctx: FpackedswitchContext): PackedSwitchPayload {
        val firstKey     = parseInt(ctx.start.text)
        val branchLabels = Array<String>(ctx.sLabel().size) { i -> ctx.sLabel(i).label.text }
        return PackedSwitchPayload.of(firstKey, branchLabels)
    }

    fun parseCatchDirective(ctx: FcatchContext): Try {
        val exceptionType = ctx.type.text
        val exceptionTypeIndex = dexEditor.addOrGetTypeIDIndex(exceptionType)

        val handlerLabel     = ctx.handle.label.text
        val typeAddrList     = listOf(TypeAddrPair.of(exceptionTypeIndex, handlerLabel))
        val exceptionHandler = EncodedCatchHandler.of(typeAddrList)

        val startLabel = ctx.start.label.text
        val endLabel   = ctx.end.label.text

        return Try.of(startLabel, endLabel, exceptionHandler)
    }

    fun parseCatchAllDirective(ctx: FcatchallContext): Try {
        val handlerLabel    = ctx.handle.label.text
        val exceptionHandler = EncodedCatchHandler.of(handlerLabel)

        val startLabel = ctx.start.label.text
        val endLabel   = ctx.end.label.text

        return Try.of(startLabel, endLabel, exceptionHandler)
    }
}