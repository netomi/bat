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
import com.github.netomi.bat.classfile.io.ClassDataInput
import com.github.netomi.bat.classfile.io.ClassDataOutput
import com.github.netomi.bat.util.JvmClassName
import java.util.*

/**
 * A class representing a NestMembers attribute in a class file.
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se18/html/jvms-4.html#jvms-4.7.29">NestMembers Attribute</a>
 */
data class NestMembersAttribute
    private constructor(override val attributeNameIndex: Int,
                         private var nestMemberClasses:  IntArray = IntArray(0)
    ): Attribute(attributeNameIndex), AttachedToClass, Sequence<Int> {

    override val type: AttributeType
        get() = AttributeType.NEST_MEMBERS

    override val dataSize: Int
        get() = 2 + nestMemberClasses.size * 2

    val size: Int
        get() = nestMemberClasses.size

    operator fun get(index: Int): Int {
        return nestMemberClasses[index]
    }

    override fun iterator(): Iterator<Int> {
        return nestMemberClasses.iterator()
    }

    fun getNestMemberClassNames(classFile: ClassFile): List<JvmClassName> {
        return nestMemberClasses.map { classFile.getClassName(it) }
    }

    override fun readAttributeData(input: ClassDataInput, length: Int) {
        nestMemberClasses = input.readShortIndexArray()
    }

    override fun writeAttributeData(output: ClassDataOutput) {
        output.writeShortIndexArray(nestMemberClasses)
    }

    override fun accept(classFile: ClassFile, visitor: ClassAttributeVisitor) {
        visitor.visitNestMembersAttribute(classFile, this)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NestMembersAttribute) return false

        return attributeNameIndex == other.attributeNameIndex &&
               nestMemberClasses.contentEquals(other.nestMemberClasses)
    }

    override fun hashCode(): Int {
        return Objects.hash(attributeNameIndex, nestMemberClasses.contentHashCode())
    }

    companion object {
        internal fun empty(attributeNameIndex: Int): NestMembersAttribute {
            return NestMembersAttribute(attributeNameIndex)
        }
    }
}