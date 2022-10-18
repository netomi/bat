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

package com.github.netomi.bat.jasm.disassemble

import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.Method
import com.github.netomi.bat.classfile.attribute.CodeAttribute
import com.github.netomi.bat.classfile.instruction.*
import com.github.netomi.bat.classfile.instruction.visitor.InstructionVisitor
import com.github.netomi.bat.io.IndentingPrinter
import java.util.*

internal class InstructionPrinter constructor(private val printer:         IndentingPrinter,
                                              private val constantPrinter: ConstantPrinter,
                                              private val debugState:      Map<Int, List<String>>): InstructionVisitor {

    override fun visitAnyInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: JvmInstruction) {}

    override fun visitAnySimpleInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: JvmInstruction) {
        printCommon(offset, instruction, true)
    }

    override fun visitLiteralInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: LiteralInstruction) {
        if (instruction.valueIsImplicit) {
            printer.println("%s".format(instruction.mnemonic))
        } else {
            printer.println("%-13s %d".format(instruction.mnemonic, instruction.value))
        }
    }

    override fun visitAnyConstantInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: ConstantInstruction) {
        printCommon(offset, instruction, false)
        printer.print(" ")
        instruction.constantAccept(classFile, constantPrinter)
        printer.println()
    }

    override fun visitInterfaceMethodInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: InterfaceMethodInstruction) {
        val instructionData = "%d, %2d".format(instruction.constantIndex, instruction.argumentCount)
        printer.print("%-13s #%-16s // ".format(instruction.mnemonic, instructionData))
        instruction.constantAccept(classFile, constantPrinter)
        printer.println()
    }

    override fun visitInvokeDynamicInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: InvokeDynamicInstruction) {
        printer.print("%-13s #%-18s // ".format(instruction.mnemonic, "${instruction.constantIndex},  0"))
        instruction.constantAccept(classFile, constantPrinter)
        printer.println()
    }

    override fun visitArrayClassInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: ArrayClassInstruction) {
        if (instruction.dimensionIsImplicit) {
            printer.print("%-13s #%-18d // ".format(instruction.mnemonic, instruction.constantIndex))
        } else {
            printer.print("%-13s #%-17s // ".format(instruction.mnemonic, "${instruction.constantIndex},  ${instruction.dimension}"))
        }
        instruction.constantAccept(classFile, constantPrinter)
        printer.println()
    }

    override fun visitBranchInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: BranchInstruction) {
        printer.println("%-13s %d".format(instruction.mnemonic, offset + instruction.branchOffset))
    }

    override fun visitArrayPrimitiveTypeInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: ArrayPrimitiveTypeInstruction) {
        printer.println("%-13s  %s".format(instruction.mnemonic, instruction.arrayType.toString().lowercase(Locale.getDefault())))
    }

    override fun visitLiteralVariableInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: LiteralVariableInstruction) {
        if (instruction.wide) {
            printer.println("%s".format(JvmOpCode.WIDE.mnemonic))
        }
        printer.println("%-13s %d, %d".format(instruction.mnemonic, instruction.variable, instruction.value))
    }

    override fun visitVariableInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: VariableInstruction) {
        if (instruction.wide) {
            printer.println("%s".format(JvmOpCode.WIDE.mnemonic))
        }
        if (instruction.variableIsImplicit) {
            printer.println("%s".format(instruction.mnemonic))
        } else {
            printer.println("%-13s %d".format(instruction.mnemonic, instruction.variable))
        }
    }

    override fun visitAnySwitchInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: SwitchInstruction) {
        for (pair in instruction) {
            printer.println("%12d: %d".format(pair.match, pair.offset + offset))
        }
        printer.println("%12s: %d".format("default", instruction.defaultOffset + offset))
        printer.levelDown()
        printer.println("      }")
    }

    override fun visitLookupSwitchInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: LookupSwitchInstruction) {
        val currPos = printer.currentPosition

        printer.println("%-13s { // %d".format(instruction.mnemonic, instruction.size))
        printer.resetIndentation(currPos)
        visitAnySwitchInstruction(classFile, method, code, offset, instruction)
    }

    override fun visitTableSwitchInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: TableSwitchInstruction) {
        val currPos = printer.currentPosition

        printer.println("%-13s { // %d to %d".format(instruction.mnemonic, instruction.lowValue, instruction.highValue))
        printer.resetIndentation(currPos)
        visitAnySwitchInstruction(classFile, method, code, offset, instruction)
    }

    private fun printCommon(offset: Int, instruction: JvmInstruction, appendNewLine: Boolean) {
        printer.println()
        printDebugInfo(offset)
        printer.print(instruction.mnemonic)
        if (appendNewLine) {
            printer.println()
        }
    }

    private fun printDebugInfo(offset: Int) {
        val debugInfos = debugState[offset]
        if (debugInfos != null) {
            for (info in debugInfos) {
                printer.println(info)
            }
        }
    }
}