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

import com.github.netomi.bat.classfile.AccessFlag
import com.github.netomi.bat.classfile.AccessFlagTarget
import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.accessFlagsToSet
import com.github.netomi.bat.classfile.constant.visitor.ArrayElementAccessor
import com.github.netomi.bat.classfile.constant.visitor.ConstantVisitor
import com.github.netomi.bat.classfile.constant.visitor.PropertyAccessor
import com.github.netomi.bat.classfile.constant.visitor.ReferencedConstantVisitor
import com.github.netomi.bat.classfile.io.ClassDataInput
import com.github.netomi.bat.classfile.io.ClassDataOutput
import com.github.netomi.bat.classfile.io.ClassFileContent
import java.util.*

data class OpensEntry
    private constructor(private var _openedPackageIndex: Int = -1,
                        private var _flags:              Int =  0,
                        private var _opensToModules:     IntArray = IntArray(0)): ClassFileContent(), Sequence<Int> {

    val openedPackageIndex: Int
        get() = _openedPackageIndex

    val flags: Int
        get() = _flags

    val flagsAsSet: Set<AccessFlag>
        get() = accessFlagsToSet(flags, AccessFlagTarget.OPENED_MODULE)

    override val contentSize: Int
        get() = 6 + size * 2

    val size: Int
        get() = _opensToModules.size

    operator fun get(index: Int): Int {
        return _opensToModules[index]
    }

    override fun iterator(): Iterator<Int> {
        return _opensToModules.iterator()
    }

    fun getOpenedPackageName(classFile: ClassFile): String {
        return classFile.getPackage(openedPackageIndex).getPackageName(classFile)
    }

    fun getOpensToModuleNames(classFile: ClassFile): List<String> {
        return _opensToModules.map { classFile.getModule(it).getModuleName(classFile) }
    }

    private fun read(input: ClassDataInput) {
        _openedPackageIndex = input.readUnsignedShort()
        _flags              = input.readUnsignedShort()
        _opensToModules     = input.readShortIndexArray()
    }

    override fun write(output: ClassDataOutput) {
        output.writeShort(_openedPackageIndex)
        output.writeShort(_flags)
        output.writeShortIndexArray(_opensToModules)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is OpensEntry) return false

        return _openedPackageIndex == other._openedPackageIndex &&
               _flags              == other._flags &&
               _opensToModules.contentEquals(other._opensToModules)
    }

    override fun hashCode(): Int {
        return Objects.hash(_openedPackageIndex, _flags, _opensToModules.contentHashCode())
    }

    fun opensToModulesAccept(classFile: ClassFile, visitor: ConstantVisitor) {
        for (constantIndex in _opensToModules) {
            classFile.constantAccept(constantIndex, visitor)
        }
    }

    fun referencedConstantVisitor(classFile: ClassFile, visitor: ReferencedConstantVisitor) {
        visitor.visitPackageConstant(classFile, this, PropertyAccessor(::_openedPackageIndex))

        for (i in _opensToModules.indices) {
            visitor.visitModuleConstant(classFile, this, ArrayElementAccessor(_opensToModules, i))
        }
    }

    companion object {
        internal fun read(input: ClassDataInput): OpensEntry {
            val entry = OpensEntry()
            entry.read(input)
            return entry
        }
    }
}
