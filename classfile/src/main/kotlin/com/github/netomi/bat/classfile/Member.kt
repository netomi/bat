/*
 *  Copyright (c) 2020 Thomas Neidhart.
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
package com.github.netomi.bat.classfile

import com.github.netomi.bat.classfile.attribute.Attribute
import com.github.netomi.bat.classfile.constant.ConstantPool
import java.io.DataInput
import java.io.IOException
import java.util.*

abstract class Member {
    var accessFlags: Int = 0
        private set
    val visibility: Visibility
        get() = Visibility.of(accessFlags)
    val modifiers: EnumSet<AccessFlag>
        get() = accessFlagModifiers(accessFlags, accessFlagTarget)
    var nameIndex: Int = 0
        private set
    var descriptorIndex: Int = 0
        private set

    protected val attributes = mutableListOf<Attribute>()

    abstract val accessFlagTarget: AccessFlagTarget

    fun name(classFile: ClassFile): String {
        return classFile.cp.getString(nameIndex)
    }

    fun descriptor(classFile: ClassFile): String {
        return classFile.cp.getString(descriptorIndex)
    }

    @Throws(IOException::class)
    protected fun read(input: DataInput, constantPool: ConstantPool) {
        accessFlags     = input.readUnsignedShort()
        nameIndex       = input.readUnsignedShort()
        descriptorIndex = input.readUnsignedShort()

        val attributeCount = input.readUnsignedShort()
        for (i in 0 until attributeCount) {
            attributes.add(Attribute.readAttribute(input, constantPool))
        }
    }
}