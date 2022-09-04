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
import java.io.DataInput
import java.io.DataOutput
import java.util.*

data class ExportsElement
    private constructor(private var _exportsIndex: Int = -1,
                        private var _exportsFlags: Int =  0,
                        private var _exportsTo:    IntArray = IntArray(0)): Sequence<Int> {

    val exportsIndex: Int
        get() = _exportsIndex

    val exportsFlags: Int
        get() = _exportsFlags

    internal val dataSize: Int
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

    private fun read(input: DataInput) {
        _exportsIndex = input.readUnsignedShort()
        _exportsFlags = input.readUnsignedShort()
        val exportsToCount = input.readUnsignedShort()
        _exportsTo = IntArray(exportsToCount)
        for (i in 0 until exportsToCount) {
            _exportsTo[i] = input.readUnsignedShort()
        }
    }

    internal fun write(output: DataOutput) {
        TODO("implement")
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

    companion object {
        internal fun read(input: DataInput): ExportsElement {
            val element = ExportsElement()
            element.read(input)
            return element
        }
    }
}