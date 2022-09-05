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

fun interface ConstantVisitorIndexed {
    fun visitAnyConstant(classFile: ClassFile, index: Int, constant: Constant)

    fun visitAnyRefConstant(classFile: ClassFile, index: Int, constant: RefConstant) {
        visitAnyConstant(classFile, index, constant)
    }

    fun visitIntegerConstant(classFile: ClassFile, index: Int, constant: IntegerConstant) {
        visitAnyConstant(classFile, index, constant)
    }

    fun visitLongConstant(classFile: ClassFile, index: Int, constant: LongConstant) {
        visitAnyConstant(classFile, index, constant)
    }

    fun visitFloatConstant(classFile: ClassFile, index: Int, constant: FloatConstant) {
        visitAnyConstant(classFile, index, constant)
    }

    fun visitDoubleConstant(classFile: ClassFile, index: Int, constant: DoubleConstant) {
        visitAnyConstant(classFile, index, constant)
    }

    fun visitClassConstant(classFile: ClassFile, index: Int, constant: ClassConstant) {
        visitAnyConstant(classFile, index, constant)
    }

    fun visitFieldRefConstant(classFile: ClassFile, index: Int, constant: FieldrefConstant) {
        visitAnyRefConstant(classFile, index, constant)
    }

    fun visitMethodRefConstant(classFile: ClassFile, index: Int, constant: MethodrefConstant) {
        visitAnyRefConstant(classFile, index, constant)
    }

    fun visitInterfaceMethodRefConstant(classFile: ClassFile, index: Int, constant: InterfaceMethodrefConstant) {
        visitAnyRefConstant(classFile, index, constant)
    }

    fun visitUtf8Constant(classFile: ClassFile, index: Int, constant: Utf8Constant) {
        visitAnyConstant(classFile, index, constant)
    }

    fun visitStringConstant(classFile: ClassFile, index: Int, constant: StringConstant) {
        visitAnyConstant(classFile, index, constant)
    }

    fun visitNameAndTypeConstant(classFile: ClassFile, index: Int, constant: NameAndTypeConstant) {
        visitAnyConstant(classFile, index, constant)
    }

    fun visitMethodTypeConstant(classFile: ClassFile, index: Int, constant: MethodTypeConstant) {
        visitAnyConstant(classFile, index, constant)
    }

    fun visitMethodHandleConstant(classFile: ClassFile, index: Int, constant: MethodHandleConstant) {
        visitAnyConstant(classFile, index, constant)
    }

    fun visitDynamicConstant(classFile: ClassFile, index: Int, constant: DynamicConstant) {
        visitAnyConstant(classFile, index, constant)
    }

    fun visitInvokeDynamicConstant(classFile: ClassFile, index: Int, constant: InvokeDynamicConstant) {
        visitAnyConstant(classFile, index, constant)
    }

    fun visitModuleConstant(classFile: ClassFile, index: Int, constant: ModuleConstant) {
        visitAnyConstant(classFile, index, constant)
    }

    fun visitPackageConstant(classFile: ClassFile, index: Int, constant: PackageConstant) {
        visitAnyConstant(classFile, index, constant)
    }
}