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
package com.github.netomi.bat.dexdump

import com.github.netomi.bat.dexfile.ClassDef
import com.github.netomi.bat.dexfile.Code
import com.github.netomi.bat.dexfile.DexFile
import com.github.netomi.bat.dexfile.EncodedMethod
import com.github.netomi.bat.dexfile.instruction.*
import com.github.netomi.bat.dexfile.visitor.InstructionVisitor
import com.github.netomi.bat.util.Primitives
import java.lang.Double.longBitsToDouble
import java.lang.Float.intBitsToFloat

internal class InstructionPrinter(private val printer: Mutf8Printer) : InstructionVisitor {

    override fun visitAnyInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: DexInstruction) {
        printGeneric(instruction)
    }

    override fun visitArithmeticLiteralInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: ArithmeticLiteralInstruction) {
        printGeneric(instruction)

        val literal = instruction.literal
        printer.print(", #int $literal // #")

        when (instruction.opcode.format) {
            InstructionFormat.FORMAT_22s -> printer.print(Primitives.asHexValue(literal.toShort()))
            InstructionFormat.FORMAT_22b -> printer.print(Primitives.asHexValue(literal.toByte()))
            else -> error("unexpected format ${instruction.opcode.format} for arithmetic literal instruction")
        }
    }

    override fun visitBranchInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: BranchInstruction) {
        printGeneric(instruction)

        if (instruction.registers.isNotEmpty()) {
            printer.print(", ")
        } else {
            printer.print(" ")
        }

        if (instruction.opcode == DexOpCode.GOTO_32) {
            printer.print("#")
            if (instruction.branchOffset < 0) {
                printer.print("-")
                printer.print(Primitives.asHexValue(-instruction.branchOffset, 8))
            } else {
                printer.print(Primitives.asHexValue(instruction.branchOffset, 8))
            }
        } else {
            printer.print(Primitives.asHexValue(offset + instruction.branchOffset, 4))
            printer.print(" // ")
            if (instruction.branchOffset < 0) {
                printer.print("-")
                printer.print(Primitives.asHexValue(-instruction.branchOffset, 4))
            } else {
                printer.print("+")
                printer.print(Primitives.asHexValue(instruction.branchOffset, 4))
            }
        }
    }

    override fun visitCallSiteInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: CallSiteInstruction) {
        printer.print(instruction.mnemonic)
        printRegisters(instruction, true)
        printer.print(", call_site@${Primitives.asHexValue(instruction.callSiteIndex, 4)}")
    }

    override fun visitFieldInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: FieldInstruction) {
        printGeneric(instruction)
        val fieldID = instruction.getField(dexFile)
        printer.print(", ${fieldID.getClassType(dexFile)}.${fieldID.getName(dexFile)}:${fieldID.getType(dexFile)}")
        printer.print(" // field@${Primitives.asHexValue(instruction.fieldIndex, 4)}")
    }

    override fun visitLiteralInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: LiteralInstruction) {
        printGeneric(instruction)
        printer.print(", ")

        val value = instruction.value
        when (instruction.opcode.format) {
            InstructionFormat.FORMAT_11n,
            InstructionFormat.FORMAT_22b -> printer.print("#int %d // #%x".format(value, value.toByte()))

            InstructionFormat.FORMAT_21h -> {
                // The printed format varies a bit based on the actual opcode.
                if (instruction.opcode == DexOpCode.CONST_HIGH16) {
                    val v = (value shr 16).toShort()
                    printer.print("#int %d // #%x".format(value, v))
                } else {
                    val v = (value shr 48).toShort()
                    printer.print("#long %d // #%x".format(value, v))
                }
            }

            InstructionFormat.FORMAT_21s,
            InstructionFormat.FORMAT_22s -> printer.print("#int %d // #%x".format(value, value.toShort()))

            InstructionFormat.FORMAT_31i -> printer.print("#float %g // #%08x".format(intBitsToFloat(value.toInt()), value.toInt()))

            InstructionFormat.FORMAT_51l -> printer.print("#double %g // #%016x".format(longBitsToDouble(value), value))

            else -> error("unexpected format ${instruction.opcode.format} for literal instruction")
        }
    }

    override fun visitAnyMethodInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: MethodInstruction) {
        printer.print(instruction.mnemonic)
        printRegisters(instruction, true)
        printer.print(", ")

        val polymorphicMethodInstruction = if (instruction is MethodProtoInstruction) instruction else null
        val methodID = instruction.getMethodID(dexFile)

        printer.print("${methodID.getClassType(dexFile)}.${methodID.getName(dexFile)}:${methodID.getProtoID(dexFile).getDescriptor(dexFile)}")

        polymorphicMethodInstruction?.apply {
            printer.print(", " + this.getProtoID(dexFile).getDescriptor(dexFile))
        }

        printer.print(" // method@")
        printer.print(Primitives.asHexValue(instruction.methodIndex, 4))

        polymorphicMethodInstruction?.apply {
            printer.print(", proto@")
            printer.print(Primitives.asHexValue(polymorphicMethodInstruction.protoIndex, 4))
        }
    }

    override fun visitMethodHandleRefInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: MethodHandleRefInstruction) {
        printGeneric(instruction)
        printer.print(", method_handle@${Primitives.asHexValue(instruction.methodHandleIndex, 4)}")
    }

    override fun visitMethodTypeRefInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: MethodTypeRefInstruction) {
        printGeneric(instruction)
        printer.print(", ")
        printer.print(instruction.getProtoID(dexFile).getDescriptor(dexFile))
        printer.print(" // proto@")
        printer.print(Primitives.asHexValue(instruction.protoIndex, 4))
    }

    override fun visitPayloadInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: PayloadInstruction) {
        printGeneric(instruction)
        if (instruction.registers.isNotEmpty()) {
            printer.print(", ")
        } else {
            printer.print(" ")
        }
        printer.print(Primitives.asHexValue(offset + instruction.payloadOffset, 8))
        printer.print(" // ")
        if (instruction.payloadOffset < 0) {
            printer.print("-")
            printer.print(Primitives.asHexValue(-instruction.payloadOffset, 8))
        } else {
            printer.print("+")
            printer.print(Primitives.asHexValue(instruction.payloadOffset, 8))
        }
    }

    override fun visitStringInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: StringInstruction) {
        printGeneric(instruction)

        printer.print(", \"")
        printer.printAsMutf8(instruction.getString(dexFile), false)
        printer.print("\" // string@")

        if (instruction.opcode == DexOpCode.CONST_STRING) {
            printer.print(Primitives.asHexValue(instruction.stringIndex, 4))
        } else {
            printer.print(Primitives.asHexValue(instruction.stringIndex, 8))
        }
    }

    override fun visitTypeInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: TypeInstruction) {
        printGeneric(instruction)
        printer.print(", ")
        val typeID = instruction.getTypeID(dexFile)
        printer.print(typeID.getType(dexFile))
        printer.print(" // type@")
        printer.print(Primitives.asHexValue(instruction.typeIndex, 4))
    }

    override fun visitArrayTypeInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: ArrayTypeInstruction) {
        printer.print(instruction.mnemonic)
        printRegisters(instruction, true)
        printer.print(", ")
        val typeID = instruction.getTypeID(dexFile)
        printer.print(typeID.getType(dexFile))
        printer.print(" // type@")
        printer.print(Primitives.asHexValue(instruction.typeIndex, 4))
    }

    override fun visitAnyPayload(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, payload: Payload) {
        printer.print(payload.toString())
    }

    private fun printGeneric(instruction: DexInstruction) {
        printer.print(instruction.mnemonic)
        if (instruction.opcode == DexOpCode.NOP) {
            printer.print(" // spacer")
        }
        printRegisters(instruction, false)
    }

    private fun printRegisters(instruction: DexInstruction, useBrackets: Boolean) {
        if (useBrackets) {
            if (instruction.registers.isNotEmpty()) {
                val registers = instruction.registers.indices.joinToString(", ", "{", "}", transform = { idx -> "v${instruction.registers[idx]}"})
                printer.print(" $registers")
            } else {
                printer.print(" {}")
            }
        } else {
            if (instruction.registers.isNotEmpty()) {
                val registers = instruction.registers.indices.joinToString(", ", transform = { idx -> "v${instruction.registers[idx]}"})
                printer.print(" $registers")
            }
        }
    }
}