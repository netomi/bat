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

package com.github.netomi.bat.shrinker.classfile

import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.shrinker.visitor.AnyClassVisitor
import com.github.netomi.bat.shrinker.visitor.AnyFieldVisitor
import com.github.netomi.bat.shrinker.visitor.AnyMethodVisitor
import com.github.netomi.bat.util.mutableListOfCapacity

/**
 * A base class for whole program optimization.
 *
 * It is mainly used to distinguish between program and library classes and
 * to cache references for super / interface and subclasses.
 */
abstract class AnyClass: ClassFile() {

    internal var superClass:       AnyClass?             = null
    internal var interfaceClasses: MutableList<AnyClass> = mutableListOfCapacity(0)
    internal var subClasses:       MutableList<AnyClass> = mutableListOfCapacity(0)

    @Suppress("UNCHECKED_CAST")
    override val fields: List<AnyField>
        get() = super.fields as List<AnyField>

    @Suppress("UNCHECKED_CAST")
    override val methods: List<AnyMethod>
        get() = super.methods as List<AnyMethod>

    internal fun addSubClass(clazz: AnyClass) {
        subClasses.add(clazz)
    }

    abstract fun accept(visitor: AnyClassVisitor)

    fun fieldsAccept(visitor: AnyFieldVisitor) {
        for (field in fields) {
            field.accept(this, visitor)
        }
    }

    fun methodsAccept(visitor: AnyMethodVisitor) {
        for (method in methods) {
            method.accept(this, visitor)
        }
    }
}
