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
import com.github.netomi.bat.classfile.constant.visitor.ArrayElementAccessor
import com.github.netomi.bat.classfile.constant.visitor.ConstantVisitor
import com.github.netomi.bat.classfile.constant.visitor.ReferencedConstantVisitor
import com.github.netomi.bat.classfile.io.ClassDataInput
import com.github.netomi.bat.classfile.io.ClassDataOutput
import com.github.netomi.bat.classfile.io.ClassFileContent
import com.github.netomi.bat.util.JvmClassName

data class UsesEntry
    private constructor(private var _usedClasses: IntArray = IntArray(0)): ClassFileContent(), Sequence<Int> {

    override val contentSize: Int
        get() = 2 + size * 2

    val size: Int
        get() = _usedClasses.size

    operator fun get(index: Int): Int {
        return _usedClasses[index]
    }

    override fun iterator(): Iterator<Int> {
        return _usedClasses.iterator()
    }

    fun getUsedClassNames(classFile: ClassFile): List<JvmClassName> {
        return _usedClasses.map { classFile.getClassName(it) }
    }

    private fun read(input: ClassDataInput) {
        _usedClasses = input.readShortIndexArray()
    }

    override fun write(output: ClassDataOutput) {
        output.writeShortIndexArray(_usedClasses)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UsesEntry) return false

        return _usedClasses.contentEquals(other._usedClasses)
    }

    override fun hashCode(): Int {
        return _usedClasses.contentHashCode()
    }

    fun usedClassConstantsAccept(classFile: ClassFile, visitor: ConstantVisitor) {
        for (classIndex in _usedClasses) {
            classFile.constantAccept(classIndex, visitor)
        }
    }

    fun referencedConstantsAccept(classFile: ClassFile, visitor: ReferencedConstantVisitor) {
        for (i in _usedClasses.indices) {
            visitor.visitClassConstant(classFile, this, ArrayElementAccessor(_usedClasses, i))
        }
    }

    companion object {
        internal fun empty(): UsesEntry {
            return UsesEntry()
        }

        internal fun read(input: ClassDataInput): UsesEntry {
            val entry = UsesEntry()
            entry.read(input)
            return entry
        }
    }
}
