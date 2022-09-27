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

package com.github.netomi.bat.shrinker.util

import com.github.netomi.bat.classfile.util.ClassPool
import com.github.netomi.bat.shrinker.classfile.AnyClass
import com.github.netomi.bat.shrinker.classfile.LibraryClass
import com.github.netomi.bat.shrinker.classfile.ProgramClass
import com.github.netomi.bat.shrinker.visitor.AnyClassVisitor
import com.github.netomi.bat.util.JvmClassName

class ProgramView {
    private val programClassPool: ClassPool<ProgramClass> = ClassPool.empty()
    private val libraryClassPool: ClassPool<LibraryClass> = ClassPool.empty()

    val programClassCount: Int
        get() = programClassPool.size

    val libraryClassCount: Int
        get() = libraryClassPool.size

    fun getClass(internalClassName: String): AnyClass? {
        return programClassPool.getClass(internalClassName)
            ?: libraryClassPool.getClass(internalClassName)
    }

    fun getClass(className: JvmClassName): AnyClass? {
        val internalClassName = className.toInternalClassName()
        return getClass(internalClassName)
    }

    fun addProgramClass(classFile: ProgramClass) {
        programClassPool.addClass(classFile)
    }

    fun addLibraryClass(classFile: LibraryClass) {
        libraryClassPool.addClass(classFile)
    }

    fun programClassesAccept(visitor: AnyClassVisitor) {
        programClassPool.classesAccept(visitor)
    }

    fun libraryClassesAccept(visitor: AnyClassVisitor) {
        libraryClassPool.classesAccept(visitor)
    }

    fun init() {
        val cleaner = ClassHierarchyCleaner()
        libraryClassPool.accept(cleaner)
        programClassPool.accept(cleaner)

        val initializer = ClassHierarchyInitializer(this)
        libraryClassPool.accept(initializer)
        programClassPool.accept(initializer)
    }
}

fun <T: AnyClass> ClassPool<T>.classesAccept(visitor: AnyClassVisitor) {
    for (clazz in this) {
        clazz.accept(visitor)
    }
}