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
import com.github.netomi.bat.classfile.AccessFlagTarget.*
import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.accessFlagModifiers
import com.github.netomi.bat.classfile.constant.ModuleConstant
import com.github.netomi.bat.classfile.io.ClassDataInput
import com.github.netomi.bat.classfile.io.ClassDataOutput
import com.github.netomi.bat.classfile.io.ClassFileContent

data class RequiresElement
    private constructor(private var _requiresIndex:        Int = -1,
                        private var _requiresFlags:        Int =  0,
                        private var _requiresVersionIndex: Int = -1): ClassFileContent() {

    val requiresIndex: Int
        get() = _requiresIndex

    val requiresFlags: Int
        get() = _requiresFlags

    val modifiers: Set<AccessFlag>
        get() = accessFlagModifiers(requiresFlags, REQUIRED_MODULE)

    val requiresVersionIndex: Int
        get() = _requiresVersionIndex

    override val contentSize: Int
        get() = 6

    fun getRequiredModule(classFile: ClassFile): ModuleConstant {
        return classFile.getModule(requiresIndex)
    }

    fun getRequiredModuleName(classFile: ClassFile): String {
        return getRequiredModule(classFile).getModuleName(classFile)
    }

    fun getRequiredVersion(classFile: ClassFile): String {
        return classFile.getString(requiresVersionIndex)
    }

    private fun read(input: ClassDataInput) {
        _requiresIndex        = input.readUnsignedShort()
        _requiresFlags        = input.readUnsignedShort()
        _requiresVersionIndex = input.readUnsignedShort()
    }

    override fun write(output: ClassDataOutput) {
        output.writeShort(requiresIndex)
        output.writeShort(requiresFlags)
        output.writeShort(requiresVersionIndex)
    }

    companion object {
        internal fun read(input: ClassDataInput): RequiresElement {
            val element = RequiresElement()
            element.read(input)
            return element
        }
    }
}