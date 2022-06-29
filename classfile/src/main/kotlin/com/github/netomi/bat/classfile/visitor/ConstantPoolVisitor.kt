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
package com.github.netomi.bat.classfile.visitor

import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.ConstantPool
import com.github.netomi.bat.classfile.constant.*

interface ConstantPoolVisitor {
    fun visitConstantPoolStart(classFile: ClassFile, constantPool: ConstantPool) {}

    fun visitConstantPoolEnd(classFile: ClassFile, constantPool: ConstantPool) {}

    fun visitAnyConstant(classFile: ClassFile, constantPool: ConstantPool, index: Int, constant: Constant) {}

    fun visitAnyRefConstant(classFile: ClassFile, constantPool: ConstantPool, index: Int, constant: RefConstant) {
        visitAnyConstant(classFile, constantPool, index, constant)
    }

    fun visitIntegerConstant(classFile: ClassFile, constantPool: ConstantPool, index: Int, constant: IntegerConstant) {
        visitAnyConstant(classFile, constantPool, index, constant)
    }

    fun visitLongConstant(classFile: ClassFile, constantPool: ConstantPool, index: Int, constant: LongConstant) {
        visitAnyConstant(classFile, constantPool, index, constant)
    }

    fun visitFloatConstant(classFile: ClassFile, constantPool: ConstantPool, index: Int, constant: FloatConstant) {
        visitAnyConstant(classFile, constantPool, index, constant)
    }

    fun visitDoubleConstant(classFile: ClassFile, constantPool: ConstantPool, index: Int, constant: DoubleConstant) {
        visitAnyConstant(classFile, constantPool, index, constant)
    }

    fun visitClassConstant(classFile: ClassFile, constantPool: ConstantPool, index: Int, constant: ClassConstant) {
        visitAnyConstant(classFile, constantPool, index, constant)
    }

    fun visitFieldRefConstant(classFile: ClassFile, constantPool: ConstantPool, index: Int, constant: FieldrefConstant) {
        visitAnyRefConstant(classFile, constantPool, index, constant)
    }

    fun visitMethodRefConstant(classFile: ClassFile, constantPool: ConstantPool, index: Int, constant: MethodrefConstant) {
        visitAnyRefConstant(classFile, constantPool, index, constant)
    }

    fun visitInterfaceMethodRefConstant(classFile: ClassFile, constantPool: ConstantPool, index: Int, constant: InterfaceMethodrefConstant) {
        visitAnyRefConstant(classFile, constantPool, index, constant)
    }

    fun visitUtf8Constant(classFile: ClassFile, constantPool: ConstantPool, index: Int, constant: Utf8Constant) {
        visitAnyConstant(classFile, constantPool, index, constant)
    }

    fun visitStringConstant(classFile: ClassFile, constantPool: ConstantPool, index: Int, constant: StringConstant) {
        visitAnyConstant(classFile, constantPool, index, constant)
    }

    fun visitNameAndTypeConstant(classFile: ClassFile, constantPool: ConstantPool, index: Int, constant: NameAndTypeConstant) {
        visitAnyConstant(classFile, constantPool, index, constant)
    }

    fun visitMethodTypeConstant(classFile: ClassFile, constantPool: ConstantPool, index: Int, constant: MethodTypeConstant) {
        visitAnyConstant(classFile, constantPool, index, constant)
    }

    fun visitMethodHandleConstant(classFile: ClassFile, constantPool: ConstantPool, index: Int, constant: MethodHandleConstant) {
        visitAnyConstant(classFile, constantPool, index, constant)
    }

    fun visitDynamicConstant(classFile: ClassFile, constantPool: ConstantPool, index: Int, constant: DynamicConstant) {
        visitAnyConstant(classFile, constantPool, index, constant)
    }

    fun visitInvokeDynamicConstant(classFile: ClassFile, constantPool: ConstantPool, index: Int, constant: InvokeDynamicConstant) {
        visitAnyConstant(classFile, constantPool, index, constant)
    }

    fun visitModuleConstant(classFile: ClassFile, constantPool: ConstantPool, index: Int, constant: ModuleConstant) {
        visitAnyConstant(classFile, constantPool, index, constant)
    }

    fun visitPackageConstant(classFile: ClassFile, constantPool: ConstantPool, index: Int, constant: PackageConstant) {
        visitAnyConstant(classFile, constantPool, index, constant)
    }
}