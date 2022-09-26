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
import com.github.netomi.bat.classfile.Field
import com.github.netomi.bat.classfile.Method
import com.github.netomi.bat.shrinker.visitor.AnalysisClassVisitor
import com.github.netomi.bat.shrinker.visitor.AnalysisFieldVisitor
import com.github.netomi.bat.shrinker.visitor.AnalysisMethodVisitor
import com.github.netomi.bat.util.mutableListOfCapacity

/**
 * A base class for whole program optimization.
 *
 * It is mainly used to distinguish between program and library classes and
 * to cache references for super / interface and subclasses.
 */
abstract class AnalysisClass: ClassFile() {

    internal var superClass:       AnalysisClass?             = null
    internal var interfaceClasses: MutableList<AnalysisClass> = mutableListOfCapacity(0)
    internal var subClasses:       MutableList<AnalysisClass> = mutableListOfCapacity(0)

    internal fun addSubClass(clazz: AnalysisClass) {
        subClasses.add(clazz)
    }

    abstract fun accept(visitor: AnalysisClassVisitor)

    fun fieldsAccept(visitor: AnalysisFieldVisitor) {
        for (field in fields) {
            field.accept(this, visitor)
        }
    }

    fun methodsAccept(visitor: AnalysisMethodVisitor) {
        for (method in methods) {
            method.accept(this, visitor)
        }
    }
}

fun Field.accept(clazz: AnalysisClass, visitor: AnalysisFieldVisitor) {
    when (clazz) {
        is ProgramClass -> visitor.visitProgramField(clazz, this)
        is LibraryClass -> visitor.visitLibraryField(clazz, this)
        else            -> error("unexpected clazz '$clazz'")
    }
}

fun Method.accept(clazz: AnalysisClass, visitor: AnalysisMethodVisitor) {
    when (clazz) {
        is ProgramClass -> visitor.visitProgramMethod(clazz, this)
        is LibraryClass -> visitor.visitLibraryMethod(clazz, this)
        else            -> error("unexpected clazz '$clazz'")
    }
}