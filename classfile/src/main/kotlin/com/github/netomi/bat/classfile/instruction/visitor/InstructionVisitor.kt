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

    fun visitArithmeticInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: ArithmeticInstruction) {
        visitAnySimpleInstruction(classFile, method, code, offset, instruction)
    }

    fun visitArrayInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: ArrayInstruction) {
        visitAnySimpleInstruction(classFile, method, code, offset, instruction)
    }

    fun visitConversionInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: ConversionInstruction) {
        visitAnySimpleInstruction(classFile, method, code, offset, instruction)
    }

    fun visitCompareInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: CompareInstruction) {
        visitAnySimpleInstruction(classFile, method, code, offset, instruction)
    }

    fun visitMonitorInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: MonitorInstruction) {
        visitAnySimpleInstruction(classFile, method, code, offset, instruction)
    }

    fun visitReturnInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: ReturnInstruction) {
        visitAnySimpleInstruction(classFile, method, code, offset, instruction)
    }

    fun visitExceptionInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: ExceptionInstruction) {
        visitAnySimpleInstruction(classFile, method, code, offset, instruction)
    }

    fun visitNullReferenceInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: NullReferenceInstruction) {
        visitAnySimpleInstruction(classFile, method, code, offset, instruction)
    }

    fun visitNopInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: NopInstruction) {
        visitAnySimpleInstruction(classFile, method, code, offset, instruction)
    }

    fun visitStackInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: StackInstruction) {
        visitAnySimpleInstruction(classFile, method, code, offset, instruction)
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

    fun visitArrayPrimitiveTypeInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: ArrayPrimitiveTypeInstruction) {
        visitAnyInstruction(classFile, method, code, offset, instruction)
    }

    fun visitBranchInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: BranchInstruction) {
        visitAnyInstruction(classFile, method, code, offset, instruction)
    }

    fun visitLiteralInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: LiteralInstruction) {
        visitAnyInstruction(classFile, method, code, offset, instruction)
    }

    fun visitAnyConstantInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: ConstantInstruction) {
        visitAnyInstruction(classFile, method, code, offset, instruction)
    }

    fun visitAnyClassInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: ClassInstruction) {
        visitAnyConstantInstruction(classFile, method, code, offset, instruction)
    }

    fun visitClassInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: ClassInstruction) {
        visitAnyClassInstruction(classFile, method, code, offset, instruction)
    }

    fun visitArrayClassInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: ArrayClassInstruction) {
        visitAnyClassInstruction(classFile, method, code, offset, instruction)
    }

    fun visitFieldInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: FieldInstruction) {
        visitAnyConstantInstruction(classFile, method, code, offset, instruction)
    }

    fun visitLiteralConstantInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: LiteralConstantInstruction) {
        visitAnyConstantInstruction(classFile, method, code, offset, instruction)
    }

    fun visitAnyInvocationInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: InvocationInstruction) {
        visitAnyConstantInstruction(classFile, method, code, offset, instruction)
    }

    fun visitInterfaceMethodInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: InterfaceMethodInstruction) {
        visitAnyInvocationInstruction(classFile, method, code, offset, instruction)
    }

    fun visitMethodInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: MethodInstruction) {
        visitAnyInvocationInstruction(classFile, method, code, offset, instruction)
    }

    fun visitInvokeDynamicInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: InvokeDynamicInstruction) {
        visitAnyInvocationInstruction(classFile, method, code, offset, instruction)
    }

    fun visitAnyVariableInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: VariableInstruction) {
        visitAnyInstruction(classFile, method, code, offset, instruction)
    }

    fun visitVariableInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: VariableInstruction) {
        visitAnyVariableInstruction(classFile, method, code, offset, instruction)
    }

    fun visitLiteralVariableInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: LiteralVariableInstruction) {
        visitAnyVariableInstruction(classFile, method, code, offset, instruction)
    }
}