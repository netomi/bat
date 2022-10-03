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

data class ExportsEntry
    private constructor(private var _exportedPackageIndex: Int = -1,
                        private var _flags:                Int =  0,
                        private var _exportedToModules:    IntArray = IntArray(0)): ClassFileContent(), Sequence<Int> {

    val exportedPackageIndex: Int
        get() = _exportedPackageIndex

    val flags: Int
        get() = _flags

    val flagsAsSet: Set<AccessFlag>
        get() = accessFlagsToSet(flags, AccessFlagTarget.EXPORTED_PACKAGE)

    override val contentSize: Int
        get() = 6 + size * 2

    val size: Int
        get() = _exportedToModules.size

    operator fun get(index: Int): Int {
        return _exportedToModules[index]
    }

    override fun iterator(): Iterator<Int> {
        return _exportedToModules.iterator()
    }

    fun getExportedPackageName(classFile: ClassFile): String {
        return classFile.getPackage(exportedPackageIndex).getPackageName(classFile)
    }

    fun getExportedToModuleNames(classFile: ClassFile): List<String> {
        return _exportedToModules.map { classFile.getModule(it).getModuleName(classFile) }
    }

    private fun read(input: ClassDataInput) {
        _exportedPackageIndex = input.readUnsignedShort()
        _flags                = input.readUnsignedShort()
        _exportedToModules    = input.readShortIndexArray()
    }

    override fun write(output: ClassDataOutput) {
        output.writeShort(_exportedPackageIndex)
        output.writeShort(_flags)
        output.writeShortIndexArray(_exportedToModules)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ExportsEntry) return false

        return _exportedPackageIndex == other._exportedPackageIndex &&
               _flags                == other._flags &&
               _exportedToModules.contentEquals(other._exportedToModules)
    }

    override fun hashCode(): Int {
        return Objects.hash(_exportedPackageIndex, _flags, _exportedToModules.contentHashCode())
    }

    fun exportedPackageConstantAccept(classFile: ClassFile, visitor: ConstantVisitor) {
        classFile.constantAccept(exportedPackageIndex, visitor)
    }

    fun exportedToModulesAccept(classFile: ClassFile, visitor: ConstantVisitor) {
        for (constantIndex in _exportedToModules) {
            classFile.constantAccept(constantIndex, visitor)
        }
    }

    fun referencedConstantsAccept(classFile: ClassFile, visitor: ReferencedConstantVisitor) {
        visitor.visitPackageConstant(classFile, this, PropertyAccessor(::_exportedPackageIndex))

        for (i in _exportedToModules.indices) {
            visitor.visitModuleConstant(classFile, this, ArrayElementAccessor(_exportedToModules, i))
        }
    }

    companion object {
        internal fun read(input: ClassDataInput): ExportsEntry {
            val entry = ExportsEntry()
            entry.read(input)
            return entry
        }
    }
}