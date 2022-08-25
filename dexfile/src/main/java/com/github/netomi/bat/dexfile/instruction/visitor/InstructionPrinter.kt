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

package com.github.netomi.bat.dexfile.instruction.visitor

import com.github.netomi.bat.dexfile.ClassDef
import com.github.netomi.bat.dexfile.Code
import com.github.netomi.bat.dexfile.DexFile
import com.github.netomi.bat.dexfile.EncodedMethod
import com.github.netomi.bat.dexfile.instruction.*
import com.github.netomi.bat.io.IndentingPrinter
import com.github.netomi.bat.util.escapeAsJavaString
import com.github.netomi.bat.util.toHexString
import com.github.netomi.bat.util.toSignedHexString
import com.github.netomi.bat.util.toSignedHexStringWithPrefix

internal class InstructionPrinter(private val printer: IndentingPrinter): InstructionVisitor {

    override fun visitAnyInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: DexInstruction) {
        printCommon(code, offset, instruction, useBrackets = false, appendNewLine = true)
    }

    override fun visitArithmeticLiteralInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: ArithmeticLiteralInstruction) {
        printCommon(code, offset, instruction, useBrackets = false, appendNewLine = false)
        printer.print(", ")
        printer.println(toSignedHexStringWithPrefix(instruction.literal))
    }

    override fun visitBranchInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: BranchInstruction) {
        printCommon(code, offset, instruction, useBrackets = false, appendNewLine = false)
        if (instruction.registers.isNotEmpty()) {
            printer.print(", ")
        } else {
            printer.print(" ")
        }
        printer.println(toSignedHexString(instruction.branchOffset, 4))
    }

    override fun visitFieldInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: FieldInstruction) {
        printCommon(code, offset, instruction, useBrackets = false, appendNewLine = false)
        printer.print(", ")
        val fieldID = instruction.getField(dexFile)
        printer.print(fieldID.getClassType(dexFile).toString())
        printer.print("->")
        printer.print(fieldID.getName(dexFile))
        printer.print(":")
        printer.println(fieldID.getType(dexFile).toString())
    }

    override fun visitLiteralInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: LiteralInstruction) {
        printCommon(code, offset, instruction, useBrackets = false, appendNewLine = false)
        printer.print(", ")
        printer.print(toSignedHexStringWithPrefix(instruction.literal))
        val opCode = instruction.opCode
        val instructionFormat = opCode.format

        // FIXME: this is a hack and should be made clean.
        if (instructionFormat == InstructionFormat.FORMAT_21h && opCode.targetsWideRegister ||
            instructionFormat == InstructionFormat.FORMAT_51l
        ) {
            printer.print("L")
        }

        printer.println()
    }

    override fun visitAnyMethodInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: MethodInstruction) {
        val methodID = instruction.getMethodID(dexFile)

        printCommon(code, offset, instruction, useBrackets = true, appendNewLine = false)

        printer.print(", ")
        printer.print(methodID.getClassType(dexFile).toString())
        printer.print("->")
        printer.print(methodID.getName(dexFile))
        printer.print(methodID.getProtoID(dexFile).getDescriptor(dexFile))

        if (instruction is MethodProtoInstruction) {
            printer.println(", " + instruction.getProtoID(dexFile).getDescriptor(dexFile))
        } else {
            printer.println()
        }
    }

    override fun visitAnyPayloadInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: PayloadInstruction<*>) {
        printCommon(code, offset, instruction, useBrackets = false, appendNewLine = false)
        if (instruction.registers.isNotEmpty()) {
            printer.print(", ")
        } else {
            printer.print(" ")
        }
        printer.println(toSignedHexString(instruction.payloadOffset, 4))
    }

    override fun visitStringInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: StringInstruction) {
        printCommon(code, offset, instruction, useBrackets = false, appendNewLine = false)
        val str = instruction.getString(dexFile)
        // escape some chars
        printer.println(", \"${str.escapeAsJavaString()}\"")
    }

    override fun visitTypeInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: TypeInstruction) {
        printCommon(code, offset, instruction, useBrackets = false, appendNewLine = false)
        printer.print(", ")
        val typeID = instruction.getTypeID(dexFile)
        printer.println(typeID.getType(dexFile).toString())
    }

    override fun visitArrayTypeInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: ArrayTypeInstruction) {
        printCommon(code, offset, instruction, useBrackets = true, appendNewLine = false)
        printer.print(", ")
        val typeID = instruction.getTypeID(dexFile)
        printer.println(typeID.getType(dexFile).toString())
    }

    override fun visitFillArrayPayload(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, payload: FillArrayPayload) {
        printer.print("${toHexString(offset, 4)}: ")
        printer.resetIndentation(printer.currentPosition)
        printer.println(".array-data " + payload.elementWidth)
        printer.levelUp()
        for (i in 0 until payload.elements) {
            when (payload.elementWidth) {
                1 -> {
                    val byteValue = payload.getElementAsByte(i)
                    printer.print(toSignedHexStringWithPrefix(byteValue) + "t")
                }
                2 -> {
                    val shortValue = payload.getElementAsShort(i)
                    printer.print(toSignedHexStringWithPrefix(shortValue) + "s")
                }
                4 -> {
                    val intValue = payload.getElementAsInt(i)
                    printer.print(toSignedHexStringWithPrefix(intValue))
                }
                8 -> {
                    val longValue = payload.getElementAsLong(i)
                    val hexString = toSignedHexStringWithPrefix(longValue)
                    printer.print(hexString)
                    if (longValue < Int.MIN_VALUE || longValue > Int.MAX_VALUE) {
                        printer.print("L")
                    }
                }
            }
            printer.println()
        }
        printer.levelDown()
        printer.println(".end array-data")
        printer.levelDown()
    }

    override fun visitPackedSwitchPayload(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, payload: PackedSwitchPayload) {
        printer.print("${toHexString(offset, 4)}: ")
        printer.resetIndentation(printer.currentPosition)
        printer.println(".packed-switch " + toSignedHexStringWithPrefix(payload.firstKey))
        printer.levelUp()
        for (branchTarget in payload.branchTargets) {
            printer.println(toSignedHexString(branchTarget, 4))
        }
        printer.levelDown()
        printer.println(".end packed-switch")
        printer.levelDown()
    }

    override fun visitSparseSwitchPayload(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, payload: SparseSwitchPayload) {
        printer.print("${toHexString(offset, 4)}: ")
        printer.resetIndentation(printer.currentPosition)
        printer.println(".sparse-switch")
        printer.levelUp()
        for (i in payload.keys.indices) {
            val key    = payload.keys[i]
            val target = payload.branchTargets[i]

            printer.print(toSignedHexStringWithPrefix(key))
            printer.print(" -> ")
            printer.println(toSignedHexString(target, 4))
        }
        printer.levelDown()
        printer.println(".end sparse-switch")
        printer.levelDown()
    }

    private fun printCommon(code: Code, offset: Int, instruction: DexInstruction, useBrackets: Boolean, appendNewLine: Boolean) {
        printer.print("${toHexString(offset, 4)}: ")
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
            val firstRegisterString = "v$firstRegister"
            printer.print(firstRegisterString)
            printer.print(" .. ")
            // make sure that the first and last register in the range use the same format.
            val lastRegisterString = "v$lastRegister"
            printer.print(lastRegisterString)
        } else {
            for (idx in instruction.registers.indices) {
                if (idx > 0) {
                    printer.print(", ")
                }
                val registerNum = instruction.registers[idx]
                printer.print("v$registerNum")
            }
        }
    }
}