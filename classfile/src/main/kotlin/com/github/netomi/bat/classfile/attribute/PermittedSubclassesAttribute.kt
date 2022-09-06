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
 * A class representing a PermittedSubclasses attribute in a class file.
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se18/html/jvms-4.html#jvms-4.7.31">PermittedSubclasses Attribute</a>
 */
data class PermittedSubclassesAttribute
    private constructor(override val attributeNameIndex: Int,
                         private var permittedClasses:   IntArray = IntArray(0)
    ): Attribute(attributeNameIndex), AttachedToClass, Sequence<Int> {

    override val type: AttributeType
        get() = AttributeType.PERMITTED_SUBCLASSES

    override val dataSize: Int
        get() = 2 + permittedClasses.size * 2

    val size: Int
        get() = permittedClasses.size

    operator fun get(index: Int): Int {
        return permittedClasses[index]
    }

    override fun iterator(): Iterator<Int> {
        return permittedClasses.iterator()
    }

    fun getPermittedClassNames(classFile: ClassFile): List<JvmClassName> {
        return permittedClasses.map { classFile.getClassName(it) }
    }

    override fun readAttributeData(input: ClassDataInput, length: Int) {
        permittedClasses = input.readShortIndexArray()
    }

    override fun writeAttributeData(output: ClassDataOutput) {
        output.writeShortIndexArray(permittedClasses)
    }

    override fun accept(classFile: ClassFile, visitor: ClassAttributeVisitor) {
        visitor.visitPermittedSubclassesAttribute(classFile, this)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PermittedSubclassesAttribute) return false

        return attributeNameIndex == other.attributeNameIndex &&
               permittedClasses.contentEquals(other.permittedClasses)
    }

    override fun hashCode(): Int {
        return Objects.hash(attributeNameIndex, permittedClasses.contentHashCode())
    }

    companion object {
        internal fun empty(attributeNameIndex: Int): PermittedSubclassesAttribute {
            return PermittedSubclassesAttribute(attributeNameIndex)
        }
    }
}