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

package com.github.netomi.bat.classfile.attribute

import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.attribute.visitor.ClassAttributeVisitor
import com.github.netomi.bat.util.JvmClassName
import java.io.DataInput
import java.io.DataOutput
import java.util.*

/**
 * A class representing a NestMembers attribute in a class file.
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se18/html/jvms-4.html#jvms-4.7.29">NestMembers Attribute</a>
 */
data class NestMembersAttribute
    private constructor(override val attributeNameIndex: Int,
                         private var _nestMemberClasses: IntArray = IntArray(0)
    ): Attribute(attributeNameIndex), AttachedToClass {

    override val type: AttributeType
        get() = AttributeType.MODULE_MAIN_CLASS

    override val dataSize: Int
        get() = 2 + nestMemberClasses.size * 2

    val nestMemberClasses: IntArray
        get() = _nestMemberClasses

    fun getNestMemberClassNames(classFile: ClassFile): List<JvmClassName> {
        return nestMemberClasses.map { classFile.getClassName(it) }
    }

    override fun readAttributeData(input: DataInput, classFile: ClassFile) {
        @Suppress("UNUSED_VARIABLE")
        val length = input.readInt()
        val numberOfClasses = input.readUnsignedShort()
        _nestMemberClasses = IntArray(numberOfClasses)
        for (i in 0 until numberOfClasses) {
            _nestMemberClasses[i] = input.readUnsignedShort()
        }
    }

    override fun writeAttributeData(output: DataOutput) {
        output.writeInt(dataSize)
        output.writeShort(nestMemberClasses.size)
        for (classIndex in nestMemberClasses) {
            output.writeShort(classIndex)
        }
    }

    override fun accept(classFile: ClassFile, visitor: ClassAttributeVisitor) {
        visitor.visitNestMembersAttribute(classFile, this)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NestMembersAttribute) return false

        return attributeNameIndex == other.attributeNameIndex &&
               _nestMemberClasses.contentEquals(other._nestMemberClasses)
    }

    override fun hashCode(): Int {
        return Objects.hash(attributeNameIndex, _nestMemberClasses.contentHashCode())
    }

    companion object {
        internal fun empty(attributeNameIndex: Int): NestMembersAttribute {
            return NestMembersAttribute(attributeNameIndex)
        }
    }
}