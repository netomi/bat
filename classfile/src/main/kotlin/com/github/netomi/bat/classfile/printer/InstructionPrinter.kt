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

package com.github.netomi.bat.classfile.printer

import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.Method
import com.github.netomi.bat.classfile.attribute.CodeAttribute
import com.github.netomi.bat.classfile.instruction.*
import com.github.netomi.bat.classfile.instruction.visitor.InstructionVisitor
import com.github.netomi.bat.io.IndentingPrinter

internal class InstructionPrinter constructor(private val printer: IndentingPrinter): InstructionVisitor {

    private val constantPrinter = ConstantPrinter(printer, false)

    override fun visitAnyInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: JvmInstruction) {}

    override fun visitAnySimpleInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: JvmInstruction) {
        printer.println("%4d: %s".format(offset, instruction.mnemonic))
    }

    override fun visitClassInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: ClassInstruction) {
        printer.print("%4d: %-13s #%-18d // class ".format(offset, instruction.mnemonic, instruction.classIndex))
        instruction.classAccept(classFile, constantPrinter)
        printer.println()
    }

    override fun visitBranchInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: BranchInstruction) {
        printer.println("%4d: %-13s %-18d".format(offset, instruction.mnemonic, offset + instruction.branchOffset))
    }

    override fun visitFieldInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: FieldInstruction) {
        printer.print("%4d: %-13s #%-18d // Field ".format(offset, instruction.mnemonic, instruction.fieldIndex))
        instruction.fieldAccept(classFile, constantPrinter)
        printer.println()
    }

    override fun visitMethodInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: MethodInstruction) {
        printer.print("%4d: %-13s #%-18d // Method ".format(offset, instruction.mnemonic, instruction.methodIndex))
        instruction.methodAccept(classFile, constantPrinter)
        printer.println()
    }

    override fun visitInterfaceMethodInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: InterfaceMethodInstruction) {
        val instructionData = "%d, %2d".format(instruction.methodIndex, instruction.argumentCount)
        printer.print("%4d: %-13s #%-16s // InterfaceMethod ".format(offset, instruction.mnemonic, instructionData))
        instruction.methodAccept(classFile, constantPrinter)
        printer.println()
    }

    override fun visitVariableInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: VariableInstruction) {
        if (instruction.length == 1) {
            printer.println("%4d: %-14s".format(offset, instruction.mnemonic))

        } else {
            printer.println("%4d: %-14s %d".format(offset, instruction.mnemonic, instruction.variable))
        }
    }
}