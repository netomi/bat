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
import kotlin.reflect.KMutableProperty0

fun interface ReferencedConstantVisitor {
    fun visitAnyConstant(classFile: ClassFile, owner: Any, accessor: IDAccessor)

    fun visitAnyRefConstant(classFile: ClassFile, owner: Any, accessor: IDAccessor) {
        visitAnyConstant(classFile, owner, accessor)
    }

    fun visitIntegerConstant(classFile: ClassFile, owner: Any, accessor: IDAccessor) {
        visitAnyConstant(classFile, owner, accessor)
    }

    fun visitLongConstant(classFile: ClassFile, owner: Any, accessor: IDAccessor) {
        visitAnyConstant(classFile, owner, accessor)
    }

    fun visitFloatConstant(classFile: ClassFile, owner: Any, accessor: IDAccessor) {
        visitAnyConstant(classFile, owner, accessor)
    }

    fun visitDoubleConstant(classFile: ClassFile, owner: Any, accessor: IDAccessor) {
        visitAnyConstant(classFile, owner, accessor)
    }

    fun visitClassConstant(classFile: ClassFile, owner: Any, accessor: IDAccessor) {
        visitAnyConstant(classFile, owner, accessor)
    }

    fun visitFieldRefConstant(classFile: ClassFile, owner: Any, accessor: IDAccessor) {
        visitAnyRefConstant(classFile, owner, accessor)
    }

    fun visitMethodRefConstant(classFile: ClassFile, owner: Any, accessor: IDAccessor) {
        visitAnyRefConstant(classFile, owner, accessor)
    }

    fun visitInterfaceMethodRefConstant(classFile: ClassFile, owner: Any, accessor: IDAccessor) {
        visitAnyRefConstant(classFile, owner, accessor)
    }

    fun visitUtf8Constant(classFile: ClassFile, owner: Any, accessor: IDAccessor) {
        visitAnyConstant(classFile, owner, accessor)
    }

    fun visitStringConstant(classFile: ClassFile, owner: Any, accessor: IDAccessor) {
        visitAnyConstant(classFile, owner, accessor)
    }

    fun visitNameAndTypeConstant(classFile: ClassFile, owner: Any, accessor: IDAccessor) {
        visitAnyConstant(classFile, owner, accessor)
    }

    fun visitMethodTypeConstant(classFile: ClassFile, owner: Any, accessor: IDAccessor) {
        visitAnyConstant(classFile, owner, accessor)
    }

    fun visitMethodHandleConstant(classFile: ClassFile, owner: Any, accessor: IDAccessor) {
        visitAnyConstant(classFile, owner, accessor)
    }

    fun visitDynamicConstant(classFile: ClassFile, owner: Any, accessor: IDAccessor) {
        visitAnyConstant(classFile, owner, accessor)
    }

    fun visitInvokeDynamicConstant(classFile: ClassFile, owner: Any, accessor: IDAccessor) {
        visitAnyConstant(classFile, owner, accessor)
    }

    fun visitModuleConstant(classFile: ClassFile, owner: Any, accessor: IDAccessor) {
        visitAnyConstant(classFile, owner, accessor)
    }

    fun visitPackageConstant(classFile: ClassFile, owner: Any, accessor: IDAccessor) {
        visitAnyConstant(classFile, owner, accessor)
    }
}

interface IDAccessor {
    fun get(): Int
    fun set(value: Int)
}

internal class PropertyAccessor(private val property: KMutableProperty0<Int>): IDAccessor {
    override fun get(): Int {
        return property.get()
    }

    override fun set(value: Int) {
        property.set(value)
    }
}

class ListElementAccessor(private val list: MutableList<Int>, private val index: Int): IDAccessor {
    override fun get(): Int {
        return list[index]
    }

    override fun set(value: Int) {
        list[index] = value
    }
}

class ArrayElementAccessor(private val array: IntArray, private val index: Int): IDAccessor {
    override fun get(): Int {
        return array[index]
    }

    override fun set(value: Int) {
        array[index] = value
    }
}

internal class ReferencedConstantAdapter constructor(private val owner:    Any,
                                                     private val accessor: IDAccessor,
                                                     private val visitor:  ReferencedConstantVisitor): ConstantVisitor {

    override fun visitAnyConstant(classFile: ClassFile, index: Int, constant: Constant) {
        error("unhandled constant '$constant'")
    }

    override fun visitIntegerConstant(classFile: ClassFile, index: Int, constant: IntegerConstant) {
        visitor.visitIntegerConstant(classFile, owner, accessor)
    }

    override fun visitLongConstant(classFile: ClassFile, index: Int, constant: LongConstant) {
        visitor.visitLongConstant(classFile, owner, accessor)
    }

    override fun visitFloatConstant(classFile: ClassFile, index: Int, constant: FloatConstant) {
        visitor.visitFloatConstant(classFile, owner, accessor)
    }

    override fun visitDoubleConstant(classFile: ClassFile, index: Int, constant: DoubleConstant) {
        visitor.visitDoubleConstant(classFile, owner, accessor)
    }

    override fun visitClassConstant(classFile: ClassFile, index: Int, constant: ClassConstant) {
        visitor.visitClassConstant(classFile, owner, accessor)
    }

    override fun visitFieldRefConstant(classFile: ClassFile, index: Int, constant: FieldrefConstant) {
        visitor.visitFieldRefConstant(classFile, owner, accessor)
    }

    override fun visitMethodRefConstant(classFile: ClassFile, index: Int, constant: MethodrefConstant) {
        visitor.visitMethodRefConstant(classFile, owner, accessor)
    }

    override fun visitInterfaceMethodRefConstant(classFile: ClassFile, index: Int, constant: InterfaceMethodrefConstant) {
        visitor.visitInterfaceMethodRefConstant(classFile, owner, accessor)
    }

    override fun visitUtf8Constant(classFile: ClassFile, index: Int, constant: Utf8Constant) {
        visitor.visitUtf8Constant(classFile, owner, accessor)
    }

    override fun visitStringConstant(classFile: ClassFile, index: Int, constant: StringConstant) {
        visitor.visitStringConstant(classFile, owner, accessor)
    }

    override fun visitNameAndTypeConstant(classFile: ClassFile, index: Int, constant: NameAndTypeConstant) {
        visitor.visitNameAndTypeConstant(classFile, owner, accessor)
    }

    override fun visitMethodTypeConstant(classFile: ClassFile, index: Int, constant: MethodTypeConstant) {
        visitor.visitMethodTypeConstant(classFile, owner, accessor)
    }

    override fun visitMethodHandleConstant(classFile: ClassFile, index: Int, constant: MethodHandleConstant) {
        visitor.visitMethodHandleConstant(classFile, owner, accessor)
    }

    override fun visitDynamicConstant(classFile: ClassFile, index: Int, constant: DynamicConstant) {
        visitor.visitDynamicConstant(classFile, owner, accessor)
    }

    override fun visitInvokeDynamicConstant(classFile: ClassFile, index: Int, constant: InvokeDynamicConstant) {
        visitor.visitInvokeDynamicConstant(classFile, owner, accessor)
    }

    override fun visitModuleConstant(classFile: ClassFile, index: Int, constant: ModuleConstant) {
        visitor.visitModuleConstant(classFile, owner, accessor)
    }

    override fun visitPackageConstant(classFile: ClassFile, index: Int, constant: PackageConstant) {
        visitor.visitPackageConstant(classFile, owner, accessor)
    }
}