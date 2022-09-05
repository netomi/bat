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
package com.github.netomi.bat.classfile.constant.visitor

import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.constant.*

fun filteredByConstantType(acceptedTypes: Set<ConstantType>, visitor: ConstantVisitor): ConstantVisitor {
    return ConstantVisitor { classFile, constant ->
        if (acceptedTypes.contains(constant.type)) constant.accept(classFile, visitor)
    }
}

fun interface ConstantVisitor {
    fun visitAnyConstant(classFile: ClassFile, constant: Constant)

    fun visitAnyRefConstant(classFile: ClassFile, refConstant: RefConstant) {
        visitAnyConstant(classFile, refConstant)
    }

    fun visitIntegerConstant(classFile: ClassFile, constant: IntegerConstant) {
        visitAnyConstant(classFile, constant)
    }

    fun visitLongConstant(classFile: ClassFile, constant: LongConstant) {
        visitAnyConstant(classFile, constant)
    }

    fun visitFloatConstant(classFile: ClassFile, constant: FloatConstant) {
        visitAnyConstant(classFile, constant)
    }

    fun visitDoubleConstant(classFile: ClassFile, constant: DoubleConstant) {
        visitAnyConstant(classFile, constant)
    }

    fun visitClassConstant(classFile: ClassFile, constant: ClassConstant) {
        visitAnyConstant(classFile, constant)
    }

    fun visitFieldRefConstant(classFile: ClassFile, constant: FieldrefConstant) {
        visitAnyRefConstant(classFile, constant)
    }

    fun visitMethodRefConstant(classFile: ClassFile, constant: MethodrefConstant) {
        visitAnyRefConstant(classFile, constant)
    }

    fun visitInterfaceMethodRefConstant(classFile: ClassFile, constant: InterfaceMethodrefConstant) {
        visitAnyRefConstant(classFile, constant)
    }

    fun visitUtf8Constant(classFile: ClassFile, constant: Utf8Constant) {
        visitAnyConstant(classFile, constant)
    }

    fun visitStringConstant(classFile: ClassFile, constant: StringConstant) {
        visitAnyConstant(classFile, constant)
    }

    fun visitNameAndTypeConstant(classFile: ClassFile, constant: NameAndTypeConstant) {
        visitAnyConstant(classFile, constant)
    }

    fun visitMethodTypeConstant(classFile: ClassFile, constant: MethodTypeConstant) {
        visitAnyConstant(classFile, constant)
    }

    fun visitMethodHandleConstant(classFile: ClassFile, constant: MethodHandleConstant) {
        visitAnyConstant(classFile, constant)
    }

    fun visitDynamicConstant(classFile: ClassFile, constant: DynamicConstant) {
        visitAnyConstant(classFile, constant)
    }

    fun visitInvokeDynamicConstant(classFile: ClassFile, constant: InvokeDynamicConstant) {
        visitAnyConstant(classFile, constant)
    }

    fun visitModuleConstant(classFile: ClassFile, constant: ModuleConstant) {
        visitAnyConstant(classFile, constant)
    }

    fun visitPackageConstant(classFile: ClassFile, constant: PackageConstant) {
        visitAnyConstant(classFile, constant)
    }

    fun asIndexedVisitor(): ConstantVisitorIndexed {
        return ConstantVisitorIndexed { classFile, _, constant ->
            constant.accept(classFile, this@ConstantVisitor)
        }
    }
}
