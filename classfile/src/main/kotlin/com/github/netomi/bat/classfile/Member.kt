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
import com.github.netomi.bat.classfile.attribute.visitor.MemberAttributeVisitor
import com.github.netomi.bat.classfile.io.ClassDataInput
import com.github.netomi.bat.classfile.io.ClassDataOutput
import com.github.netomi.bat.classfile.visitor.MemberVisitor
import com.github.netomi.bat.util.mutableListOfCapacity
import java.io.IOException
import java.util.*

abstract class Member protected constructor(accessFlags:               Int =  0,
                                            nameIndex:                 Int = -1,
                                            descriptorIndex:           Int = -1,
                                            protected var _attributes: MutableList<Attribute> = mutableListOfCapacity(0)) {

    var accessFlags: Int = accessFlags
        private set(value) {
            field = value
            visibility = Visibility.of(value)
            updateModifiers(value)
        }

    var nameIndex: Int = nameIndex
        private set

    var descriptorIndex: Int = descriptorIndex
        private set

    val attributes: List<Attribute>
        get() = _attributes

    var visibility: Visibility = Visibility.of(accessFlags)
        private set

    protected abstract fun updateModifiers(accessFlags: Int)
    abstract val accessFlagTarget: AccessFlagTarget

    abstract val isStatic: Boolean

    fun getName(classFile: ClassFile): String {
        return classFile.getString(nameIndex)
    }

    fun getDescriptor(classFile: ClassFile): String {
        return classFile.getString(descriptorIndex)
    }

    abstract fun accept(classFile: ClassFile, index: Int, visitor: MemberVisitor)
    abstract fun attributesAccept(classFile: ClassFile, visitor: MemberAttributeVisitor)

    @Throws(IOException::class)
    internal fun read(input: ClassDataInput) {
        accessFlags     = input.readUnsignedShort()
        nameIndex       = input.readUnsignedShort()
        descriptorIndex = input.readUnsignedShort()
        _attributes     = input.readAttributes()
    }

    internal fun write(output: ClassDataOutput) {
        output.writeShort(accessFlags)
        output.writeShort(nameIndex)
        output.writeShort(descriptorIndex)
        output.writeAttributes(_attributes)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Member) return false

        return accessFlags     == other.accessFlags &&
               nameIndex       == other.nameIndex &&
               descriptorIndex == other.descriptorIndex &&
               _attributes     == other._attributes
    }

    override fun hashCode(): Int {
        return Objects.hash(accessFlags, nameIndex, descriptorIndex, _attributes)
    }
}