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

package com.github.netomi.bat.shrinker.wpo

import com.github.netomi.bat.classfile.util.ClassPool
import com.github.netomi.bat.shrinker.wpo.classfile.LibraryClass
import com.github.netomi.bat.shrinker.wpo.classfile.ProgramClass
import com.github.netomi.bat.shrinker.wpo.classfile.WPOClass
import com.github.netomi.bat.shrinker.wpo.util.WPOClassCleaner
import com.github.netomi.bat.shrinker.wpo.util.WPOClassInitializer
import com.github.netomi.bat.shrinker.wpo.visitor.WPOClassVisitor
import com.github.netomi.bat.util.JvmClassName

class WPOContext {
    private val programClassPool: ClassPool<ProgramClass> = ClassPool.empty()
    private val libraryClassPool: ClassPool<LibraryClass> = ClassPool.empty()

    val programClassCount: Int
        get() = programClassPool.size

    val libraryClassCount: Int
        get() = libraryClassPool.size

    fun getClass(internalClassName: String): WPOClass? {
        return programClassPool.getClass(internalClassName)
            ?: libraryClassPool.getClass(internalClassName)
    }

    fun getClass(className: JvmClassName): WPOClass? {
        val internalClassName = className.toInternalClassName()
        return getClass(internalClassName)
    }

    fun addProgramClass(classFile: ProgramClass) {
        programClassPool.addClass(classFile)
    }

    fun addLibraryClass(classFile: LibraryClass) {
        libraryClassPool.addClass(classFile)
    }

    fun programClassesAccept(visitor: WPOClassVisitor) {
        programClassPool.classesAccept(visitor)
    }

    fun libraryClassesAccept(visitor: WPOClassVisitor) {
        libraryClassPool.classesAccept(visitor)
    }

    fun init() {
        val cleaner = WPOClassCleaner()
        libraryClassPool.accept(cleaner)
        programClassPool.accept(cleaner)

        val initializer = WPOClassInitializer(this)
        libraryClassPool.accept(initializer)
        programClassPool.accept(initializer)
    }
}

fun <T: WPOClass> ClassPool<T>.classesAccept(visitor: WPOClassVisitor) {
    for (clazz in this) {
        clazz.accept(visitor)
    }
}