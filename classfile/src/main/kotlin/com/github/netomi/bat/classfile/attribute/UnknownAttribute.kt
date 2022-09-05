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
import com.github.netomi.bat.classfile.Field
import com.github.netomi.bat.classfile.Method
import com.github.netomi.bat.classfile.attribute.visitor.ClassAttributeVisitor
import com.github.netomi.bat.classfile.attribute.visitor.FieldAttributeVisitor
import com.github.netomi.bat.classfile.attribute.visitor.MethodAttributeVisitor
import com.github.netomi.bat.classfile.io.ClassDataInput
import com.github.netomi.bat.classfile.io.ClassDataOutput
import java.io.IOException
import java.util.*

/**
 * A class representing an unknown attribute in a class file.
 */
data class UnknownAttribute internal constructor(override val attributeNameIndex: Int,
                                                  private var _data:              ByteArray = ByteArray(0))
    : Attribute(attributeNameIndex), AttachedToClass, AttachedToField, AttachedToMethod {

    override val type: AttributeType
        get() = AttributeType.UNKNOWN

    override val dataSize: Int
        get() = data.size

    val data: ByteArray
        get() = _data

    @Throws(IOException::class)
    override fun readAttributeData(input: ClassDataInput) {
        val length = input.readInt()
        _data = ByteArray(length)
        input.readFully(_data)
    }

    @Throws(IOException::class)
    override fun writeAttributeData(output: ClassDataOutput) {
        output.writeInt(dataSize)
        output.write(data)
    }

    override fun accept(classFile: ClassFile, visitor: ClassAttributeVisitor) {
        visitor.visitUnknownAttribute(classFile, this)
    }

    override fun accept(classFile: ClassFile, field: Field, visitor: FieldAttributeVisitor) {
        visitor.visitUnknownAttribute(classFile, this)
    }

    override fun accept(classFile: ClassFile, method: Method, visitor: MethodAttributeVisitor) {
        visitor.visitUnknownAttribute(classFile, this)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UnknownAttribute

        return attributeNameIndex == other.attributeNameIndex &&
               _data.contentEquals(other._data)
    }

    override fun hashCode(): Int {
        return Objects.hash(attributeNameIndex, _data.contentHashCode())
    }

    companion object {
        internal fun empty(attributeNameIndex: Int): UnknownAttribute {
            return UnknownAttribute(attributeNameIndex)
        }
    }
}