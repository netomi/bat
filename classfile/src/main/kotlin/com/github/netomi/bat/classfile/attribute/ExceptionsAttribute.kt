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
import com.github.netomi.bat.classfile.attribute.visitor.AttributeVisitor
import com.github.netomi.bat.util.JvmClassName
import com.github.netomi.bat.util.asInternalClassName
import java.io.DataInput
import java.io.DataOutput
import java.io.IOException
import java.util.*

/**
 * A class representing an Exceptions attribute in a class file.
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se17/html/jvms-4.html#jvms-4.7.5">Exceptions Attribute</a>
 */
data class ExceptionsAttribute internal constructor(override val attributeNameIndex: Int,
                                                     private var _exceptions:        IntArray = IntArray(0)) : Attribute(attributeNameIndex) {

    override val type: AttributeType
        get() = AttributeType.EXCEPTIONS

    val exceptions: IntArray
        get() = _exceptions

    fun getExceptionClassNames(classFile: ClassFile): List<JvmClassName> {
        return exceptions.map { classFile.getClassName(it) }
    }

    @Throws(IOException::class)
    override fun readAttributeData(input: DataInput) {
        val length = input.readInt()
        val numberOfExceptions = input.readUnsignedShort()
        _exceptions = IntArray(numberOfExceptions)
        for (index in 0 until numberOfExceptions) {
            _exceptions[index] = input.readUnsignedShort()
        }
    }

    @Throws(IOException::class)
    override fun writeAttributeData(output: DataOutput) {
        val length = 2 + exceptions.size * 2
        output.write(length)
        output.writeShort(exceptions.size)
        for (index in exceptions.indices) {
            output.writeShort(exceptions[index])
        }
    }

    override fun accept(classFile: ClassFile, visitor: AttributeVisitor) {
        visitor.visitExceptionsAttributes(classFile, this)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ExceptionsAttribute) return false

        return attributeNameIndex == other.attributeNameIndex &&
               _exceptions.contentEquals(other._exceptions)
    }

    override fun hashCode(): Int {
        return Objects.hash(attributeNameIndex, _exceptions.contentHashCode())
    }

    companion object {
        internal fun empty(attributeNameIndex: Int): ExceptionsAttribute {
            return ExceptionsAttribute(attributeNameIndex)
        }
    }
}