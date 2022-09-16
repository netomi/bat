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

package com.github.netomi.bat.classdump

import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.Method
import com.github.netomi.bat.classfile.attribute.CodeAttribute
import com.github.netomi.bat.classfile.instruction.*
import com.github.netomi.bat.classfile.instruction.visitor.InstructionVisitor
import com.github.netomi.bat.io.IndentingPrinter
import java.util.*

internal class InstructionPrinter constructor(private val printer: IndentingPrinter): InstructionVisitor {

    private val constantPrinter = ConstantPrinter(printer, printConstantType = true, alwaysIncludeClassName = false)

    override fun visitAnyInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: JvmInstruction) {}

    override fun visitAnySimpleInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: JvmInstruction) {
        printer.println("%4d: %s".format(offset, instruction.mnemonic))
    }

    override fun visitLiteralInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: LiteralInstruction) {
        if (instruction.valueIsImplicit) {
            printer.println("%4d: %s".format(offset, instruction.mnemonic))
        } else {
            printer.println("%4d: %-13s %d".format(offset, instruction.mnemonic, instruction.value))
        }
    }

    override fun visitAnyConstantInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: ConstantInstruction) {
        printer.print("%4d: %-13s #%-18d // ".format(offset, instruction.mnemonic, instruction.constantIndex))
        instruction.constantAccept(classFile, constantPrinter)
        printer.println()
    }

    override fun visitInterfaceMethodInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: InterfaceMethodInstruction) {
        val instructionData = "%d, %2d".format(instruction.constantIndex, instruction.argumentCount)
        printer.print("%4d: %-13s #%-16s // ".format(offset, instruction.mnemonic, instructionData))
        instruction.constantAccept(classFile, constantPrinter)
        printer.println()
    }

    override fun visitInvokeDynamicInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: InvokeDynamicInstruction) {
        printer.print("%4d: %-13s #%-18s // ".format(offset, instruction.mnemonic, "${instruction.constantIndex},  0"))
        instruction.constantAccept(classFile, constantPrinter)
        printer.println()
    }

    override fun visitArrayClassInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: ArrayClassInstruction) {
        if (instruction.dimensionIsImplicit) {
            printer.print("%4d: %-13s #%-18d // ".format(offset, instruction.mnemonic, instruction.constantIndex))
        } else {
            printer.print("%4d: %-13s #%-17s // ".format(offset, instruction.mnemonic, "${instruction.constantIndex},  ${instruction.dimension}"))
        }
        instruction.constantAccept(classFile, constantPrinter)
        printer.println()
    }

    override fun visitBranchInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: BranchInstruction) {
        printer.println("%4d: %-13s %d".format(offset, instruction.mnemonic, offset + instruction.branchOffset))
    }

    override fun visitArrayPrimitiveTypeInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: ArrayPrimitiveTypeInstruction) {
        printer.println("%4d: %-13s  %s".format(offset, instruction.mnemonic, instruction.arrayType.toString().lowercase(Locale.getDefault())))
    }

    override fun visitLiteralVariableInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: LiteralVariableInstruction) {
        printer.println("%4d: %-13s %d, %d".format(offset, instruction.mnemonic, instruction.variable, instruction.value))
    }

    override fun visitVariableInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: VariableInstruction) {
        if (instruction.variableIsImplicit) {
            printer.println("%4d: %s".format(offset, instruction.mnemonic))
        } else {
            printer.println("%4d: %-13s %d".format(offset, instruction.mnemonic, instruction.variable))
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
        printer.print("%4d: ".format(offset))

        val currPos = printer.currentPosition

        printer.println("%-13s { // %d".format(instruction.mnemonic, instruction.size))
        printer.resetIndentation(currPos)
        visitAnySwitchInstruction(classFile, method, code, offset, instruction)
    }

    override fun visitTableSwitchInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: TableSwitchInstruction) {
        printer.print("%4d: ".format(offset))

        val currPos = printer.currentPosition

        printer.println("%-13s { // %d to %d".format(instruction.mnemonic, instruction.lowValue, instruction.highValue))
        printer.resetIndentation(currPos)
        visitAnySwitchInstruction(classFile, method, code, offset, instruction)
    }
}