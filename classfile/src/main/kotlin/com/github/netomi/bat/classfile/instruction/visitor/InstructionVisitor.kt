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

package com.github.netomi.bat.classfile.instruction.visitor

import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.Method
import com.github.netomi.bat.classfile.attribute.CodeAttribute
import com.github.netomi.bat.classfile.instruction.*

fun interface InstructionVisitor {
    fun visitAnyInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: JvmInstruction)

    fun visitAnySimpleInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: JvmInstruction) {
        visitAnyInstruction(classFile, method, code, offset, instruction)
    }

    fun visitAnySwitchInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: SwitchInstruction) {
        visitAnyInstruction(classFile, method, code, offset, instruction)
    }

    fun visitLookupSwitchInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: LookupSwitchInstruction) {
        visitAnySwitchInstruction(classFile, method, code, offset, instruction)
    }

    fun visitTableSwitchInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: TableSwitchInstruction) {
        visitAnySwitchInstruction(classFile, method, code, offset, instruction)
    }

    fun visitArithmeticInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: ArithmeticInstruction) {
        visitAnySimpleInstruction(classFile, method, code, offset, instruction)
    }

    fun visitArrayInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: ArrayInstruction) {
        visitAnySimpleInstruction(classFile, method, code, offset, instruction)
    }

    fun visitArrayTypeInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: ArrayTypeInstruction) {
        visitAnyInstruction(classFile, method, code, offset, instruction)
    }

    fun visitBranchInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: BranchInstruction) {
        visitAnyInstruction(classFile, method, code, offset, instruction)
    }

    fun visitAnyClassInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: ClassInstruction) {
        visitAnyInstruction(classFile, method, code, offset, instruction)
    }

    fun visitClassInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: ClassInstruction) {
        visitAnyClassInstruction(classFile, method, code, offset, instruction)
    }

    fun visitArrayClassInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: ArrayClassInstruction) {
        visitAnyClassInstruction(classFile, method, code, offset, instruction)
    }

    fun visitConstantInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: ConstantInstruction) {
        visitAnyInstruction(classFile, method, code, offset, instruction)
    }

    fun visitConversionInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: ConversionInstruction) {
        visitAnySimpleInstruction(classFile, method, code, offset, instruction)
    }

    fun visitCompareInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: CompareInstruction) {
        visitAnySimpleInstruction(classFile, method, code, offset, instruction)
    }

    fun visitFieldInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: FieldInstruction) {
        visitAnyInstruction(classFile, method, code, offset, instruction)
    }

    fun visitInvokeDynamicInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: InvokeDynamicInstruction) {
        visitAnyInstruction(classFile, method, code, offset, instruction)
    }

    fun visitAnyMethodInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: AnyMethodInstruction) {
        visitAnyInstruction(classFile, method, code, offset, instruction)
    }

    fun visitInterfaceMethodInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: InterfaceMethodInstruction) {
        visitAnyMethodInstruction(classFile, method, code, offset, instruction)
    }

    fun visitLiteralInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: LiteralInstruction) {
        visitAnySimpleInstruction(classFile, method, code, offset, instruction)
    }

    fun visitMethodInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: MethodInstruction) {
        visitAnyMethodInstruction(classFile, method, code, offset, instruction)
    }

    fun visitMonitorInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: MonitorInstruction) {
        visitAnySimpleInstruction(classFile, method, code, offset, instruction)
    }

    fun visitReturnInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: ReturnInstruction) {
        visitAnySimpleInstruction(classFile, method, code, offset, instruction)
    }

    fun visitStackInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: StackInstruction) {
        visitAnySimpleInstruction(classFile, method, code, offset, instruction)
    }

    fun visitAnyVariableInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: VariableInstruction) {
        visitAnySimpleInstruction(classFile, method, code, offset, instruction)
    }

    fun visitLiteralVariableInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: LiteralVariableInstruction) {
        visitAnyVariableInstruction(classFile, method, code, offset, instruction)
    }

    fun visitVariableInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: VariableInstruction) {
        visitAnyVariableInstruction(classFile, method, code, offset, instruction)
    }
}