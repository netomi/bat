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
import java.io.DataInput
import java.io.DataOutput

data class RequiresElement
    private constructor(private var _requiresIndex:        Int = -1,
                        private var _requiresFlags:        Int =  0,
                        private var _requiresVersionIndex: Int = -1) {

    val requiresIndex: Int
        get() = _requiresIndex

    val requiresFlags: Int
        get() = _requiresFlags

    val requiresVersionIndex: Int
        get() = _requiresVersionIndex

    internal val dataSize: Int
        get() = DATA_SIZE

    fun getRequiredModule(classFile: ClassFile): ModuleConstant {
        return classFile.getModule(requiresIndex)
    }

    fun getRequiredModuleName(classFile: ClassFile): String {
        return getRequiredModule(classFile).getModuleName(classFile)
    }

    fun getRequiredVersion(classFile: ClassFile): String {
        return classFile.getString(requiresVersionIndex)
    }

    private fun read(input: DataInput) {
        _requiresIndex = input.readUnsignedShort()
        _requiresFlags = input.readUnsignedShort()
        _requiresVersionIndex = input.readUnsignedShort()
    }

    internal fun write(output: DataOutput) {
        output.writeShort(requiresIndex)
        output.writeShort(requiresFlags)
        output.writeShort(requiresVersionIndex)
    }

    companion object {
        private const val DATA_SIZE = 6

        internal fun read(input: DataInput): RequiresElement {
            val element = RequiresElement()
            element.read(input)
            return element
        }
    }
}