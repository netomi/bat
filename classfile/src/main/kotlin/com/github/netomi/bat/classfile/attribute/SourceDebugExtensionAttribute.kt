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
import java.io.DataInput
import java.io.DataOutput
import java.util.*

/**
 * A class representing a SourceDebugExtension attribute in a class file.
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se17/html/jvms-4.html#jvms-4.7.11">SourceDebugExtension Attribute</a>
 */
data class SourceDebugExtensionAttribute
    private constructor(override val attributeNameIndex: Int,
                         private var _debugExtension:    ByteArray = ByteArray(0)): Attribute(attributeNameIndex), AttachedToClass {

    override val type: AttributeType
        get() = AttributeType.SOURCE_DEBUG_EXTENSION

    override val dataSize: Int
        get() = debugExtension.size

    val debugExtension: ByteArray
        get() = _debugExtension

    override fun readAttributeData(input: DataInput, classFile: ClassFile) {
        val length = input.readInt()
        _debugExtension = ByteArray(length)
        input.readFully(_debugExtension)
    }

    override fun writeAttributeData(output: DataOutput) {
        output.writeInt(dataSize)
        output.write(debugExtension)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SourceDebugExtensionAttribute) return false

        return attributeNameIndex == other.attributeNameIndex &&
               debugExtension.contentEquals(other.debugExtension)
    }

    override fun hashCode(): Int {
        return Objects.hash(attributeNameIndex, debugExtension.contentHashCode())
    }

    override fun accept(classFile: ClassFile, visitor: ClassAttributeVisitor) {
        visitor.visitSourceDebugExtensionAttribute(classFile, this)
    }

    companion object {
        internal fun empty(attributeNameIndex: Int): SourceDebugExtensionAttribute {
            return SourceDebugExtensionAttribute(attributeNameIndex)
        }
    }
}