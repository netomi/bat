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
import com.github.netomi.bat.classfile.constant.visitor.PropertyAccessor
import com.github.netomi.bat.classfile.constant.visitor.ReferencedConstantVisitor
import com.github.netomi.bat.classfile.io.ClassDataInput
import com.github.netomi.bat.classfile.io.ClassDataOutput
import com.github.netomi.bat.classfile.io.ClassFileContent
import com.github.netomi.bat.util.JvmClassName
import java.util.*

data class ProvidesEntry
    private constructor(private var _providedClassIndex:  Int      = -1,
                        private var _providesWithClasses: IntArray = IntArray(0)): ClassFileContent(), Sequence<Int> {

    val providedClassIndex: Int
        get() = _providedClassIndex

    override val contentSize: Int
        get() = 4 + size * 2

    val size: Int
        get() = _providesWithClasses.size

    operator fun get(index: Int): Int {
        return _providesWithClasses[index]
    }

    override fun iterator(): Iterator<Int> {
        return _providesWithClasses.iterator()
    }

    fun getProvidedClassName(classFile: ClassFile): JvmClassName {
        return classFile.getClass(providedClassIndex).getClassName(classFile)
    }

    fun getProvidesWithClassNames(classFile: ClassFile): List<JvmClassName> {
        return _providesWithClasses.map { classFile.getClass(it).getClassName(classFile) }
    }

    private fun read(input: ClassDataInput) {
        _providedClassIndex  = input.readUnsignedShort()
        _providesWithClasses = input.readShortIndexArray()
    }

    override fun write(output: ClassDataOutput) {
        output.writeShort(_providedClassIndex)
        output.writeShortIndexArray(_providesWithClasses)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ProvidesEntry) return false

        return _providedClassIndex == other._providedClassIndex &&
                _providesWithClasses.contentEquals(other._providesWithClasses)
    }

    override fun hashCode(): Int {
        return Objects.hash(_providedClassIndex, _providesWithClasses.contentHashCode())
    }

    fun providesWithClassesAccept(classFile: ClassFile, visitor: ConstantVisitor) {
        for (classIndex in _providesWithClasses) {
            classFile.constantAccept(classIndex, visitor)
        }
    }

    fun referencedConstantVisitor(classFile: ClassFile, visitor: ReferencedConstantVisitor) {
        visitor.visitClassConstant(classFile, this, PropertyAccessor(::_providedClassIndex))

        for (i in _providesWithClasses.indices) {
            visitor.visitClassConstant(classFile, this, ArrayElementAccessor(_providesWithClasses, i))
        }
    }

    companion object {
        internal fun read(input: ClassDataInput): ProvidesEntry {
            val entry = ProvidesEntry()
            entry.read(input)
            return entry
        }
    }
}