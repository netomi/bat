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

data class OpensElement
    private constructor(private var _opensIndex: Int = -1,
                        private var _opensFlags: Int =  0,
                        private var _opensTo:    IntArray = IntArray(0)): ClassFileContent(), Sequence<Int> {

    val opensIndex: Int
        get() = _opensIndex

    val opensFlags: Int
        get() = _opensFlags

    override val contentSize: Int
        get() = 6 + size * 2

    val size: Int
        get() = _opensTo.size

    operator fun get(index: Int): Int {
        return _opensTo[index]
    }

    override fun iterator(): Iterator<Int> {
        return _opensTo.iterator()
    }

    fun getOpenedPackage(classFile: ClassFile): PackageConstant {
        return classFile.getPackage(opensIndex)
    }

    fun getOpenedPackageName(classFile: ClassFile): String {
        return getOpenedPackage(classFile).getPackageName(classFile)
    }

    fun getOpenedToModules(classFile: ClassFile): List<ModuleConstant> {
        return _opensTo.map { classFile.getModule(it) }
    }

    fun getOpenedToModuleNames(classFile: ClassFile): List<String> {
        return getOpenedToModules(classFile).map { it.getModuleName(classFile) }
    }

    private fun read(input: ClassDataInput) {
        _opensIndex = input.readUnsignedShort()
        _opensFlags = input.readUnsignedShort()
        _opensTo    = input.readShortIndexArray()
    }

    override fun write(output: ClassDataOutput) {
        output.writeShort(_opensIndex)
        output.writeShort(_opensFlags)
        output.writeShortIndexArray(_opensTo)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is OpensElement) return false

        return _opensIndex == other._opensIndex &&
               _opensFlags == other._opensFlags &&
               _opensTo.contentEquals(other._opensTo)
    }

    override fun hashCode(): Int {
        return Objects.hash(_opensIndex, _opensFlags, _opensTo.contentHashCode())
    }

    fun openedToModulesAccept(classFile: ClassFile, visitor: ConstantVisitor) {
        for (constantIndex in _opensTo) {
            classFile.constantAccept(constantIndex, visitor)
        }
    }

    companion object {
        internal fun read(input: ClassDataInput): OpensElement {
            val element = OpensElement()
            element.read(input)
            return element
        }
    }
}
