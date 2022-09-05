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

package com.github.netomi.bat.classfile.attribute.module

import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.constant.ClassConstant
import com.github.netomi.bat.classfile.constant.visitor.ConstantVisitor
import com.github.netomi.bat.util.JvmClassName
import java.io.DataInput
import java.io.DataOutput
import java.util.*

data class ProvidesElement
    private constructor(private var _providesIndex: Int = -1,
                        private var _providesWith:  IntArray = IntArray(0)): Sequence<Int> {

    val providesIndex: Int
        get() = _providesIndex

    internal val dataSize: Int
        get() = 4 + size * 2

    val size: Int
        get() = _providesWith.size

    operator fun get(index: Int): Int {
        return _providesWith[index]
    }

    override fun iterator(): Iterator<Int> {
        return _providesWith.iterator()
    }

    fun getProvidedClass(classFile: ClassFile): ClassConstant {
        return classFile.getClass(providesIndex)
    }

    fun getProvidedClassName(classFile: ClassFile): JvmClassName {
        return getProvidedClass(classFile).getClassName(classFile)
    }

    fun getProvidesWithClasses(classFile: ClassFile): List<ClassConstant> {
        return _providesWith.map { classFile.getClass(it) }
    }

    fun getProvidesWithClassNames(classFile: ClassFile): List<JvmClassName> {
        return getProvidesWithClasses(classFile).map { it.getClassName(classFile) }
    }

    private fun read(input: DataInput) {
        _providesIndex = input.readUnsignedShort()
        val providesWithCount = input.readUnsignedShort()
        _providesWith = IntArray(providesWithCount)
        for (i in 0 until providesWithCount) {
            _providesWith[i] = input.readUnsignedShort()
        }
    }

    internal fun write(output: DataOutput) {
        output.writeShort(_providesIndex)
        output.writeShort(_providesWith.size)
        for (element in _providesWith) {
            output.writeShort(element)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ProvidesElement) return false

        return _providesIndex == other._providesIndex &&
               _providesWith.contentEquals(other._providesWith)
    }

    override fun hashCode(): Int {
        return Objects.hash(_providesIndex, _providesWith.contentHashCode())
    }

    fun providesWithClassesAccept(classFile: ClassFile, visitor: ConstantVisitor) {
        for (constantIndex in _providesWith) {
            classFile.constantAccept(constantIndex, visitor)
        }
    }

    companion object {
        internal fun read(input: DataInput): ProvidesElement {
            val element = ProvidesElement()
            element.read(input)
            return element
        }
    }
}