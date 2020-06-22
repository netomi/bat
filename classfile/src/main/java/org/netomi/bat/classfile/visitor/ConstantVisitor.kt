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
package org.netomi.bat.classfile.visitor

import org.netomi.bat.classfile.ClassFile
import org.netomi.bat.classfile.constant.*

interface ConstantVisitor {
    @JvmDefault
    fun visitAnyConstant(classFile: ClassFile, constant: Constant) {}

    @JvmDefault
    fun visitAnyRefConstant(classFile: ClassFile, refConstant: RefConstant) {
        visitAnyConstant(classFile, refConstant)
    }

    @JvmDefault
    fun visitIntegerConstant(classFile: ClassFile, constant: IntegerConstant) {
        visitAnyConstant(classFile, constant)
    }

    @JvmDefault
    fun visitLongConstant(classFile: ClassFile, constant: LongConstant) {
        visitAnyConstant(classFile, constant)
    }

    @JvmDefault
    fun visitFloatConstant(classFile: ClassFile, constant: FloatConstant) {
        visitAnyConstant(classFile, constant)
    }

    @JvmDefault
    fun visitDoubleConstant(classFile: ClassFile, constant: DoubleConstant) {
        visitAnyConstant(classFile, constant)
    }

    @JvmDefault
    fun visitClassConstant(classFile: ClassFile, constant: ClassConstant) {
        visitAnyConstant(classFile, constant)
    }

    @JvmDefault
    fun visitFieldRefConstant(classFile: ClassFile, constant: FieldrefConstant) {
        visitAnyRefConstant(classFile, constant)
    }

    @JvmDefault
    fun visitMethodRefConstant(classFile: ClassFile, constant: MethodrefConstant) {
        visitAnyRefConstant(classFile, constant)
    }

    @JvmDefault
    fun visitInterfaceMethodRefConstant(classFile: ClassFile, constant: InterfaceMethodrefConstant) {
        visitAnyRefConstant(classFile, constant)
    }

    @JvmDefault
    fun visitUtf8Constant(classFile: ClassFile, constant: Utf8Constant) {
        visitAnyConstant(classFile, constant)
    }

    @JvmDefault
    fun visitStringConstant(classFile: ClassFile, constant: StringConstant) {
        visitAnyConstant(classFile, constant)
    }

    @JvmDefault
    fun visitNameAndTypeConstant(classFile: ClassFile, constant: NameAndTypeConstant) {
        visitAnyConstant(classFile, constant)
    }

    @JvmDefault
    fun visitMethodTypeConstant(classFile: ClassFile, constant: MethodTypeConstant) {
        visitAnyConstant(classFile, constant)
    }

    @JvmDefault
    fun visitMethodHandleConstant(classFile: ClassFile, constant: MethodHandleConstant) {
        visitAnyConstant(classFile, constant)
    }

    @JvmDefault
    fun visitDynamicConstant(classFile: ClassFile, constant: DynamicConstant) {
        visitAnyConstant(classFile, constant)
    }

    @JvmDefault
    fun visitInvokeDynamicConstant(classFile: ClassFile, constant: InvokeDynamicConstant) {
        visitAnyConstant(classFile, constant)
    }

    @JvmDefault
    fun visitModuleConstant(classFile: ClassFile, constant: ModuleConstant) {
        visitAnyConstant(classFile, constant)
    }

    @JvmDefault
    fun visitPackageConstant(classFile: ClassFile, constant: PackageConstant) {
        visitAnyConstant(classFile, constant)
    }
}