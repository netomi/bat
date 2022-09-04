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
import com.github.netomi.bat.util.JvmClassName
import java.io.DataInput
import java.io.DataOutput

data class UsesElement
    private constructor(private var _uses: IntArray = IntArray(0)): Sequence<Int> {

    internal val dataSize: Int
        get() = 4 + size * 2

    val size: Int
        get() = _uses.size

    operator fun get(index: Int): Int {
        return _uses[index]
    }

    override fun iterator(): Iterator<Int> {
        return _uses.iterator()
    }

    fun getUsedClasses(classFile: ClassFile): List<ClassConstant> {
        return _uses.map { classFile.getClass(it) }
    }

    fun getUsedClassNames(classFile: ClassFile): List<JvmClassName> {
        return getUsedClasses(classFile).map { it.getClassName(classFile) }
    }

    private fun read(input: DataInput) {
        val usesCount = input.readUnsignedShort()
        _uses = IntArray(usesCount)
        for (i in 0 until usesCount) {
            _uses[i] = input.readUnsignedShort()
        }
    }

    internal fun write(output: DataOutput) {
        TODO("implement")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UsesElement) return false

        return _uses.contentEquals(other._uses)
    }

    override fun hashCode(): Int {
        return _uses.contentHashCode()
    }

    companion object {
        internal fun empty(): UsesElement {
            return UsesElement()
        }

        internal fun read(input: DataInput): UsesElement {
            val element = UsesElement()
            element.read(input)
            return element
        }
    }
}
