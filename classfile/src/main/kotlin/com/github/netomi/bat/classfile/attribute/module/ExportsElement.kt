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
import com.github.netomi.bat.classfile.constant.ModuleConstant
import com.github.netomi.bat.classfile.constant.PackageConstant
import com.github.netomi.bat.classfile.constant.visitor.ConstantVisitor
import com.github.netomi.bat.classfile.io.ClassDataInput
import com.github.netomi.bat.classfile.io.ClassDataOutput
import com.github.netomi.bat.classfile.io.ClassFileContent
import java.util.*

data class ExportsElement
    private constructor(private var _exportsIndex: Int = -1,
                        private var _exportsFlags: Int =  0,
                        private var _exportsTo:    IntArray = IntArray(0)): ClassFileContent(), Sequence<Int> {

    val exportsIndex: Int
        get() = _exportsIndex

    val exportsFlags: Int
        get() = _exportsFlags

    override val dataSize: Int
        get() = 6 + size * 2

    val size: Int
        get() = _exportsTo.size

    operator fun get(index: Int): Int {
        return _exportsTo[index]
    }

    override fun iterator(): Iterator<Int> {
        return _exportsTo.iterator()
    }

    fun getExportedPackage(classFile: ClassFile): PackageConstant {
        return classFile.getPackage(exportsIndex)
    }

    fun getExportedPackageName(classFile: ClassFile): String {
        return getExportedPackage(classFile).getPackageName(classFile)
    }

    fun getExportedToModules(classFile: ClassFile): List<ModuleConstant> {
        return _exportsTo.map { classFile.getModule(it) }
    }

    fun getExportedToModuleNames(classFile: ClassFile): List<String> {
        return getExportedToModules(classFile).map { it.getModuleName(classFile) }
    }

    private fun read(input: ClassDataInput) {
        _exportsIndex = input.readUnsignedShort()
        _exportsFlags = input.readUnsignedShort()
        _exportsTo    = input.readShortIndexArray()
    }

    override fun write(output: ClassDataOutput) {
        output.writeShort(_exportsIndex)
        output.writeShort(_exportsFlags)
        output.writeShortIndexArray(_exportsTo)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ExportsElement) return false

        return _exportsIndex == other._exportsIndex &&
               _exportsFlags == other._exportsFlags &&
               _exportsTo.contentEquals(other._exportsTo)
    }

    override fun hashCode(): Int {
        return Objects.hash(_exportsIndex, _exportsFlags, _exportsTo.contentHashCode())
    }

    fun exportedToModulesAccept(classFile: ClassFile, visitor: ConstantVisitor) {
        for (constantIndex in _exportsTo) {
            classFile.constantAccept(constantIndex, visitor)
        }
    }

    companion object {
        internal fun read(input: ClassDataInput): ExportsElement {
            val element = ExportsElement()
            element.read(input)
            return element
        }
    }
}