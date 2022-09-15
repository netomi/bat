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

// do not use KMutableProperty as it would add a dependency to kotlin-reflect which we want to avoid
class PropertyAccessor(private val getter: () -> Int, private val setter: (Int) -> Unit): IDAccessor {
    override fun get(): Int {
        return getter.invoke()
    }

    override fun set(value: Int) {
        setter.invoke(value)
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