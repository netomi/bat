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
package com.github.netomi.bat.classfile.attribute

import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.Method
import com.github.netomi.bat.classfile.attribute.visitor.MethodAttributeVisitor
import com.github.netomi.bat.classfile.io.ClassDataInput
import com.github.netomi.bat.classfile.io.ClassDataOutput
import com.github.netomi.bat.util.JvmClassName
import java.io.IOException
import java.util.*

/**
 * A class representing an Exceptions attribute in a class file.
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se17/html/jvms-4.html#jvms-4.7.5">Exceptions Attribute</a>
 */
data class ExceptionsAttribute
    private constructor(override val attributeNameIndex: Int,
                         private var exceptions:         IntArray = IntArray(0))
    : Attribute(attributeNameIndex), AttachedToMethod, Sequence<Int> {

    override val type: AttributeType
        get() = AttributeType.EXCEPTIONS

    override val dataSize: Int
        get() = 2 + exceptions.size * 2

    val size: Int
        get() = exceptions.size

    operator fun get(index: Int): Int {
        return exceptions[index]
    }

    override fun iterator(): Iterator<Int> {
        return exceptions.iterator()
    }

    fun getExceptionClassNames(classFile: ClassFile): List<JvmClassName> {
        return exceptions.map { classFile.getClassName(it) }
    }

    @Throws(IOException::class)
    override fun readAttributeData(input: ClassDataInput, length: Int) {
        exceptions = input.readShortIndexArray()
    }

    @Throws(IOException::class)
    override fun writeAttributeData(output: ClassDataOutput) {
        output.writeShortIndexArray(exceptions)
    }

    override fun accept(classFile: ClassFile, method: Method, visitor: MethodAttributeVisitor) {
        visitor.visitExceptionsAttribute(classFile, method, this)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ExceptionsAttribute) return false

        return attributeNameIndex == other.attributeNameIndex &&
               exceptions.contentEquals(other.exceptions)
    }

    override fun hashCode(): Int {
        return Objects.hash(attributeNameIndex, exceptions.contentHashCode())
    }

    companion object {
        internal fun empty(attributeNameIndex: Int): ExceptionsAttribute {
            return ExceptionsAttribute(attributeNameIndex)
        }
    }
}