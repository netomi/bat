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

internal class InstructionPrinter constructor(private val printer:             IndentingPrinter,
                                              private val constantPrinter:     ConstantPrinter,
                                              private val branchTargetPrinter: BranchTargetPrinter,
                                              private val debugState:          Map<Int, List<String>>): InstructionVisitor {

    override fun visitAnyInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: JvmInstruction) {}

    override fun visitAnySimpleInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: JvmInstruction) {
        printCommon(offset, instruction, wide = false, appendNewLine = true)
    }

    override fun visitLiteralInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: LiteralInstruction) {
        if (instruction.valueIsImplicit) {
            printCommon(offset, instruction, wide = false, appendNewLine = true)
        } else {
            printCommon(offset, instruction, wide = false, appendNewLine = false)
            printer.println(" %d".format(instruction.value))
        }
    }

    override fun visitAnyConstantInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: ConstantInstruction) {
        printCommon(offset, instruction, wide = false, appendNewLine = false)
        printer.print(" ")
        instruction.constantAccept(classFile, constantPrinter)
        printer.println()
    }

    override fun visitInvokeDynamicInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: InvokeDynamicInstruction) {
        printer.print("%-13s #%-18s // ".format(instruction.mnemonic, "${instruction.constantIndex},  0"))
        instruction.constantAccept(classFile, constantPrinter)
        printer.println()
    }

    override fun visitArrayClassInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: ArrayClassInstruction) {
        printCommon(offset, instruction, wide = false, appendNewLine = false)
        printer.print(" ")
        instruction.constantAccept(classFile, constantPrinter)

        if (!instruction.dimensionIsImplicit) {
            printer.print(", ${instruction.dimension}")
        }

        printer.println()
    }

    override fun visitBranchInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: BranchInstruction) {
        printCommon(offset, instruction, wide = false, appendNewLine = false)
        printer.print(" ")
        printer.println(branchTargetPrinter.formatBranchInstructionTarget(offset, instruction))
    }

    override fun visitArrayPrimitiveTypeInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: ArrayPrimitiveTypeInstruction) {
        printCommon(offset, instruction, wide = false, appendNewLine = false)
        printer.println(" %s".format(instruction.arrayType.toString().lowercase(Locale.getDefault())))
    }

    override fun visitLiteralVariableInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: LiteralVariableInstruction) {
        if (instruction.wide) {
            printer.println("%s".format(JvmOpCode.WIDE.mnemonic))
        }
        printer.println("%-13s %d, %d".format(instruction.mnemonic, instruction.variable, instruction.value))
    }

    override fun visitVariableInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: VariableInstruction) {
        if (instruction.variableIsImplicit) {
            printCommon(offset, instruction, instruction.wide, true)
        } else {
            printCommon(offset, instruction, instruction.wide, false)
            printer.println(" %d".format(instruction.variable))
        }
    }

    override fun visitAnySwitchInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: SwitchInstruction) {
        for (pair in instruction) {
            printer.println("%12d -> %s".format(pair.match, branchTargetPrinter.formatLookupSwitchTarget(offset, pair.offset)))
        }
        printer.println("%12s -> %s".format("default", branchTargetPrinter.formatLookupSwitchTarget(offset, instruction.defaultOffset)))
        printer.levelDown()
        printer.println("}")
    }

    override fun visitLookupSwitchInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: LookupSwitchInstruction) {
        val currPos = printer.currentPosition

        printCommon(offset, instruction, wide = false, appendNewLine = false)
        printer.println(" {")
        printer.resetIndentation(currPos)
        visitAnySwitchInstruction(classFile, method, code, offset, instruction)
    }

    override fun visitTableSwitchInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: TableSwitchInstruction) {
        val currPos = printer.currentPosition

        printer.println("%-13s { // %d to %d".format(instruction.mnemonic, instruction.lowValue, instruction.highValue))
        printer.resetIndentation(currPos)
        visitAnySwitchInstruction(classFile, method, code, offset, instruction)
    }

    private fun printCommon(offset: Int, instruction: JvmInstruction, wide: Boolean = false, appendNewLine: Boolean = true) {
        printer.println()
        printDebugInfo(offset)
        printLabels(offset)

        if (wide) {
            printer.println(JvmOpCode.WIDE.mnemonic)
        }

        printer.print(instruction.mnemonic)

        if (appendNewLine) {
            printer.println()
        }
    }

    private fun printLabels(offset: Int) {
        branchTargetPrinter.printLabels(offset)
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