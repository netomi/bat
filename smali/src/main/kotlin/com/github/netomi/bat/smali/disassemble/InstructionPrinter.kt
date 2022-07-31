/*
 *  Copyright (c) 2020 Thomas Neidhart.
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
package com.github.netomi.bat.smali.disassemble

import com.github.netomi.bat.dexfile.*
import com.github.netomi.bat.dexfile.instruction.*
import com.github.netomi.bat.dexfile.instruction.InstructionFormat.*
import com.github.netomi.bat.dexfile.value.visitor.filterByStartIndex
import com.github.netomi.bat.dexfile.instruction.visitor.InstructionVisitor
import com.github.netomi.bat.dexfile.visitor.allCode
import com.github.netomi.bat.dexfile.visitor.allInstructions
import com.github.netomi.bat.io.IndentingPrinter
import com.github.netomi.bat.util.escapeAsJavaString

internal class InstructionPrinter(private val printer:             IndentingPrinter,
                                  private val registerPrinter:     RegisterPrinter,
                                  private val branchTargetPrinter: BranchTargetPrinter,
                                  private val debugState:          Map<Int, List<String>>?) : InstructionVisitor {

    override fun visitAnyInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: DexInstruction) {
        printCommon(code, offset, instruction, useBrackets = false, appendNewLine = true)
        printEndLabels(dexFile, code, offset, instruction.length)
    }

    override fun visitArithmeticLiteralInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: ArithmeticLiteralInstruction) {
        printCommon(code, offset, instruction, useBrackets = false, appendNewLine = false)
        printer.print(", ")
        printer.println(toHexString(instruction.literal))
        printEndLabels(dexFile, code, offset, instruction.length)
    }

    override fun visitBranchInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: BranchInstruction) {
        printCommon(code, offset, instruction, useBrackets = false, appendNewLine = false)
        if (instruction.registers.isNotEmpty()) {
            printer.print(", ")
        } else {
            printer.print(" ")
        }
        printer.println(branchTargetPrinter.formatBranchInstructionTarget(offset, instruction))
        printEndLabels(dexFile, code, offset, instruction.length)
    }

    override fun visitCallSiteInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: CallSiteInstruction) {
        printCommon(code, offset, instruction, useBrackets = true, appendNewLine = false)
        printer.print(", call_site_" + instruction.callSiteIndex)
        val callSite = instruction.getCallSiteID(dexFile).callSite
        printer.print("(")
        callSite.accept(dexFile, filterByStartIndex(1, CallSiteArgumentPrinter(printer).joinedByValueConsumer { _, _ -> printer.print(", ") } ))
        printer.print(")@")
        val methodHandle = callSite.getMethodHandle(dexFile)
        printer.print(methodHandle.getTargetClassType(dexFile))
        printer.print("->")
        printer.print(methodHandle.getTargetMemberName(dexFile))
        printer.println(methodHandle.getTargetDescriptor(dexFile))
    }

    override fun visitFieldInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: FieldInstruction) {
        printCommon(code, offset, instruction, useBrackets = false, appendNewLine = false)
        printer.print(", ")
        val fieldID = instruction.getField(dexFile)
        printer.print(fieldID.getClassType(dexFile))
        printer.print("->")
        printer.print(fieldID.getName(dexFile))
        printer.print(":")
        printer.println(fieldID.getType(dexFile))
        printEndLabels(dexFile, code, offset, instruction.length)
    }

    override fun visitLiteralInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: LiteralInstruction) {
        printCommon(code, offset, instruction, useBrackets = false, appendNewLine = false)
        printer.print(", ")
        printer.print(toHexString(instruction.value))
        val opCode = instruction.opCode
        val instructionFormat = opCode.format

        // FIXME: this is a hack and should be made clean.
        if (instructionFormat == FORMAT_21h && opCode.targetsWideRegister ||
            instructionFormat == FORMAT_51l) {
            printer.print("L")
        }

        if (instruction.opCode.targetsWideRegister) {
            printCommentIfLikelyDouble(printer, instruction.value)
        } else {
            printCommentIfLikelyFloat(printer, instruction.value.toInt())
        }

        printer.println()
        printEndLabels(dexFile, code, offset, instruction.length)
    }

    override fun visitAnyMethodInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: MethodInstruction) {
        var methodFollowerExplanation: String? = null
        val methodID = instruction.getMethodID(dexFile)

        if (methodID.getName(dexFile).startsWith("access$")) {
            val methodFollower = AccessMethodFollower()
            methodID.accept(dexFile, allCode(allInstructions(methodFollower)))
            if (methodFollower.explanation != null) {
                methodFollowerExplanation = "# " + methodFollower.explanation
            }
        }

        printCommon(code, offset, instruction, methodFollowerExplanation, useBrackets = true, appendNewLine = false)

        printer.print(", ")
        printer.print(methodID.getClassType(dexFile))
        printer.print("->")
        printer.print(methodID.getName(dexFile))
        printer.print(methodID.getProtoID(dexFile).getDescriptor(dexFile))

        if (instruction is MethodProtoInstruction) {
            printer.println(", " + instruction.getProtoID(dexFile).getDescriptor(dexFile))
        } else {
            printer.println()
        }

        printEndLabels(dexFile, code, offset, instruction.length)
    }

    override fun visitMethodHandleRefInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: MethodHandleRefInstruction) {
        printCommon(code, offset, instruction, useBrackets = false, appendNewLine = false)
        printer.print(", ")
        val methodHandle = instruction.getMethodHandle(dexFile)
        printer.print(methodHandle.methodHandleType.simpleName)
        printer.print("@")
        printer.print(methodHandle.getTargetClassType(dexFile))
        printer.print("->")
        printer.print(methodHandle.getTargetMemberName(dexFile))
        printer.println(methodHandle.getTargetMemberDescriptor(dexFile))
        printEndLabels(dexFile, code, offset, instruction.length)
    }

    override fun visitMethodTypeRefInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: MethodTypeRefInstruction) {
        printCommon(code, offset, instruction, useBrackets = false, appendNewLine = false)
        printer.print(", ")
        val protoID = instruction.getProtoID(dexFile)
        printer.println(protoID.getDescriptor(dexFile))
        printEndLabels(dexFile, code, offset, instruction.length)
    }

    override fun visitAnyPayloadInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: PayloadInstruction) {
        printCommon(code, offset, instruction, useBrackets = false, appendNewLine = false)
        if (instruction.registers.isNotEmpty()) {
            printer.print(", ")
        } else {
            printer.print(" ")
        }
        printer.println(branchTargetPrinter.formatPayloadInstructionTarget(offset, instruction))
        printEndLabels(dexFile, code, offset, instruction.length)
    }

    override fun visitStringInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: StringInstruction) {
        printCommon(code, offset, instruction, useBrackets = false, appendNewLine = false)
        val str = instruction.getString(dexFile)
        // escape some chars
        printer.println(", \"${str.escapeAsJavaString()}\"")
        printEndLabels(dexFile, code, offset, instruction.length)
    }

    override fun visitTypeInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: TypeInstruction) {
        printCommon(code, offset, instruction, useBrackets = false, appendNewLine = false)
        printer.print(", ")
        val typeID = instruction.getTypeID(dexFile)
        printer.println(typeID.getType(dexFile))
        printEndLabels(dexFile, code, offset, instruction.length)
    }

    override fun visitArrayTypeInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: ArrayTypeInstruction) {
        printCommon(code, offset, instruction, useBrackets = true, appendNewLine = false)
        printer.print(", ")
        val typeID = instruction.getTypeID(dexFile)
        printer.println(typeID.getType(dexFile))
        printEndLabels(dexFile, code, offset, instruction.length)
    }

    override fun visitFillArrayPayload(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, payload: FillArrayPayload) {
        printer.println()
        printDebugInfo(offset)
        printLabels(code, offset)
        printer.println(".array-data " + payload.elementWidth)
        printer.levelUp()
        for (i in 0 until payload.elements) {
            when (payload.elementWidth) {
                1 -> {
                    val byteValue = payload.getElementAsByte(i)
                    printer.print(toHexString(byteValue) + "t")
                    printCommentIfLikelyFloat(printer, byteValue.toInt())
                }
                2 -> {
                    val shortValue = payload.getElementAsShort(i)
                    printer.print(toHexString(shortValue) + "s")
                    printCommentIfLikelyFloat(printer, shortValue.toInt())
                }
                4 -> {
                    val intValue = payload.getElementAsInt(i)
                    printer.print(toHexString(intValue))
                    printCommentIfLikelyFloat(printer, intValue)
                }
                8 -> {
                    val longValue = payload.getElementAsLong(i)
                    val hexString = toHexString(longValue)
                    printer.print(hexString)
                    if (longValue < Int.MIN_VALUE || longValue > Int.MAX_VALUE) {
                        printer.print("L")
                    }
                    printCommentIfLikelyDouble(printer, longValue)
                }
            }
            printer.println()
        }
        printer.levelDown()
        printer.println(".end array-data")
    }

    override fun visitPackedSwitchPayload(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, payload: PackedSwitchPayload) {
        printer.println()
        printDebugInfo(offset)
        printLabels(code, offset)
        printer.println(".packed-switch " + toHexString(payload.firstKey))
        printer.levelUp()
        for (branchTarget in payload.branchTargets) {
            printer.println(branchTargetPrinter.formatPackedSwitchTarget(offset, branchTarget))
        }
        printer.levelDown()
        printer.println(".end packed-switch")
    }

    override fun visitSparseSwitchPayload(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, payload: SparseSwitchPayload) {
        printer.println()
        printDebugInfo(offset)
        printLabels(code, offset)
        printer.println(".sparse-switch")
        printer.levelUp()
        for (i in payload.keys.indices) {
            val key    = payload.keys[i]
            val target = payload.branchTargets[i]

            printer.print(toHexString(key))
            printer.print(" -> ")
            printer.println(branchTargetPrinter.formatSparseSwitchTarget(offset, target))
        }
        printer.levelDown()
        printer.println(".end sparse-switch")
    }

    private fun printCommon(code: Code, offset: Int, instruction: DexInstruction, useBrackets: Boolean, appendNewLine: Boolean) {
        printCommon(code, offset, instruction, null, useBrackets, appendNewLine)
    }

    private fun printCommon(code: Code, offset: Int, instruction: DexInstruction, preInstruction: String?, useBrackets: Boolean, appendNewLine: Boolean) {
        printer.println()
        printDebugInfo(offset)
        printLabels(code, offset)
        if (preInstruction != null) {
            printer.println(preInstruction)
        }
        printer.print(instruction.mnemonic)
        printRegisters(instruction, useBrackets)
        if (appendNewLine) {
            printer.println()
        }
    }

    private fun printRegisters(instruction: DexInstruction, useBrackets: Boolean) {
        if (useBrackets) {
            if (instruction.registers.isNotEmpty()) {
                printer.print(" {")
                printRegistersInternal(instruction)
                printer.print("}")
            } else {
                printer.print(" {}")
            }
        } else {
            if (instruction.registers.isNotEmpty()) {
                printer.print(" ")
                printRegistersInternal(instruction)
            }
        }
    }

    private fun printRegistersInternal(instruction: DexInstruction) {
        val isRangeInstruction = instruction.mnemonic.contains("range")
        if (isRangeInstruction) {
            val firstRegister = instruction.registers[0]
            val lastRegister = instruction.registers[instruction.registers.size - 1]
            val firstRegisterString = registerPrinter.formatRegister(firstRegister)
            printer.print(firstRegisterString)
            printer.print(" .. ")
            // make sure that the first and last register in the range use the same format.
            val lastRegisterString = if (firstRegisterString.startsWith("v")) {
                registerPrinter.formatRegister(lastRegister, false)
            } else {
                registerPrinter.formatRegister(lastRegister)
            }
            printer.print(lastRegisterString)
        } else {
            for (idx in instruction.registers.indices) {
                if (idx > 0) {
                    printer.print(", ")
                }
                val registerNum = instruction.registers[idx]
                registerPrinter.printRegister(printer, registerNum)
            }
        }
    }

    private fun printDebugInfo(offset: Int) {
        if (debugState == null) {
            return
        }

        val debugInfos = debugState[offset]
        if (debugInfos != null) {
            for (info in debugInfos) {
                printer.println(info)
            }
        }
    }

    private fun printLabels(code: Code, offset: Int) {
        branchTargetPrinter.printLabels(code, offset, printer)
    }

    private fun printEndLabels(dexFile: DexFile, code: Code, offset: Int, instructionLength: Int) {
        TryCatchPrinter.printTryEndLabel(dexFile, code, offset, instructionLength, printer)
    }

    private fun printCommentIfLikelyFloat(printer: IndentingPrinter, `val`: Int) {
        if (Numbers.isLikelyFloat(`val`)) {
            printer.print("    # ")
            val fVal = java.lang.Float.intBitsToFloat(`val`)
            if (fVal == Float.POSITIVE_INFINITY) {
                printer.print("Float.POSITIVE_INFINITY")
            } else if (fVal == Float.NEGATIVE_INFINITY) {
                printer.print("Float.NEGATIVE_INFINITY")
            } else if (java.lang.Float.isNaN(fVal)) {
                printer.print("Float.NaN")
            } else if (fVal == Float.MAX_VALUE) {
                printer.print("Float.MAX_VALUE")
            } else if (fVal == Math.PI.toFloat()) {
                printer.print("(float)Math.PI")
            } else if (fVal == Math.E.toFloat()) {
                printer.print("(float)Math.E")
            } else {
                printer.print(fVal.toString())
                printer.print("f")
            }
        }
    }

    private fun printCommentIfLikelyDouble(printer: IndentingPrinter, `val`: Long) {
        if (Numbers.isLikelyDouble(`val`)) {
            printer.print("    # ")
            val dVal = java.lang.Double.longBitsToDouble(`val`)
            if (dVal == Double.POSITIVE_INFINITY) {
                printer.print("Double.POSITIVE_INFINITY")
            } else if (dVal == Double.NEGATIVE_INFINITY) {
                printer.print("Double.NEGATIVE_INFINITY")
            } else if (java.lang.Double.isNaN(dVal)) {
                printer.print("Double.NaN")
            } else if (dVal == Double.MAX_VALUE) {
                printer.print("Double.MAX_VALUE")
            } else if (dVal == Math.PI) {
                printer.print("Math.PI")
            } else if (dVal == Math.E) {
                printer.print("Math.E")
            } else {
                printer.print(dVal.toString())
            }
        }
    }

    companion object {
        // private utility methods.
        private fun toHexString(value: Long): String {
            return if (value < 0) String.format("-0x%x", -value) else String.format("0x%x", value)
        }

        private fun toHexString(value: Int): String {
            return if (value < 0) String.format("-0x%x", -value) else String.format("0x%x", value)
        }

        private fun toHexString(value: Short): String {
            return if (value < 0) String.format("-0x%x", -value) else String.format("0x%x", value)
        }

        private fun toHexString(value: Byte): String {
            return if (value < 0) String.format("-0x%x", -value) else String.format("0x%x", value)
        }
    }
}

private class AccessMethodFollower : InstructionVisitor {
    var explanation: String? = null
        private set

    override fun visitAnyInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: DexInstruction) {}

    override fun visitFieldInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: FieldInstruction) {
        val mnemonic = instruction.mnemonic
        val action = if (mnemonic.contains("get")) "getter" else "setter"
        val fieldID = instruction.getField(dexFile)
        explanation = action + " for: " + fieldID.getClassType(dexFile) + "->" + fieldID.getName(dexFile) + ":" + fieldID.getType(dexFile)
    }

    override fun visitMethodInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: MethodInstruction) {
        val methodID = instruction.getMethodID(dexFile)
        explanation = "invokes: " + methodID.getClassType(dexFile) + "->" + methodID.getName(dexFile) + methodID.getProtoID(dexFile).getDescriptor(dexFile)
    }
}
