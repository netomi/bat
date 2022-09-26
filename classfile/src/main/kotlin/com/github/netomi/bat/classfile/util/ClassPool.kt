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

package com.github.netomi.bat.classfile.util

import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.visitor.ClassFileVisitor
import com.github.netomi.bat.classfile.visitor.ClassPoolVisitor
import com.github.netomi.bat.util.JvmClassName
import java.util.*

class ClassPool<T: ClassFile> private constructor(): Sequence<T> {
    private val classMap: SortedMap<String, T> = TreeMap()

    val size: Int
        get() = classMap.size

    override fun iterator(): Iterator<T> {
        return classMap.values.iterator()
    }

    fun getClass(internalClassName: String): T? {
        return classMap[internalClassName]
    }

    fun getClass(className: JvmClassName): T? {
        return getClass(className.toInternalClassName())
    }

    fun addClass(classFile: T) {
        classMap[classFile.className.toInternalClassName()] = classFile
    }

    fun accept(visitor: ClassPoolVisitor<in T>) {
        visitor.visitClassPool(this)
    }

    fun classesAccept(visitor: ClassFileVisitor) {
        for ((_, clazz) in classMap) {
            clazz.accept(visitor)
        }
    }

    companion object {
        fun <T: ClassFile> empty(): ClassPool<T> {
            return ClassPool()
        }
    }
}

typealias ClassFilePool = ClassPool<ClassFile>