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
import com.github.netomi.bat.classfile.attribute.visitor.RecordComponentAttributeVisitor
import com.github.netomi.bat.classfile.io.ClassDataInput
import com.github.netomi.bat.util.mutableListOfCapacity
import java.io.DataOutput

/**
 * A class representing a Record attribute in a class file.
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se18/html/jvms-4.html#jvms-4.7.30">Record Attribute</a>
 */
data class RecordAttribute
    private constructor(override val attributeNameIndex: Int,
                         private var components:         MutableList<RecordComponent> = mutableListOfCapacity(0)
    ): Attribute(attributeNameIndex), AttachedToClass, Sequence<RecordComponent> {

    override val type: AttributeType
        get() = AttributeType.RECORD

    override val dataSize: Int
        get() = 2 + components.fold(0) { acc, component -> acc + component.dataSize }

    val size: Int
        get() = components.size

    operator fun get(index: Int): RecordComponent {
        return components[index]
    }

    override fun iterator(): Iterator<RecordComponent> {
        return components.iterator()
    }

    override fun readAttributeData(input: ClassDataInput) {
        @Suppress("UNUSED_VARIABLE")
        val length = input.readInt()
        val componentsCount = input.readUnsignedShort()
        components = mutableListOfCapacity(componentsCount)
        for (i in 0 until componentsCount) {
            components.add(RecordComponent.read(input))
        }
    }

    override fun writeAttributeData(output: DataOutput) {
        output.write(dataSize)
        output.writeShort(components.size)
        for (component in components) {
            component.write(output)
        }
    }

    override fun accept(classFile: ClassFile, visitor: ClassAttributeVisitor) {
        visitor.visitRecordAttribute(classFile, this)
    }

    companion object {
        internal fun empty(attributeNameIndex: Int): RecordAttribute {
            return RecordAttribute(attributeNameIndex)
        }
    }
}

data class RecordComponent
    private constructor(private var _nameIndex:       Int                    = -1,
                        private var _descriptorIndex: Int                    = -1,
                        private var _attributes:      MutableList<Attribute> = mutableListOfCapacity(0)) {

    val nameIndex: Int
        get() = _nameIndex

    val desciptorIndex: Int
        get() = _descriptorIndex

    internal val dataSize: Int
        get() = 6 + _attributes.fold(0) { acc, attribute -> acc + attribute.dataSize }

    val attributes: List<Attribute>
        get() = _attributes

    fun getName(classFile: ClassFile): String {
        return classFile.getString(nameIndex)
    }

    fun getDescriptor(classFile: ClassFile): String {
        return classFile.getString(desciptorIndex)
    }

    private fun read(input: ClassDataInput) {
        _nameIndex       = input.readUnsignedShort()
        _descriptorIndex = input.readUnsignedShort()
        _attributes      = input.readAttributes()
    }

    internal fun write(output: DataOutput) {
        output.write(dataSize)
        output.writeShort(nameIndex)
        output.writeShort(desciptorIndex)
        output.writeShort(_attributes.size)
        for (attribute in _attributes) {
            attribute.writeAttribute(output)
        }
    }

    fun attributesAccept(classFile: ClassFile, record: RecordAttribute, visitor: RecordComponentAttributeVisitor) {
        for (attribute in attributes.filterIsInstance(AttachedToRecordComponent::class.java)) {
            attribute.accept(classFile, record, this, visitor)
        }
    }

    companion object {
        internal fun read(input: ClassDataInput): RecordComponent {
            val element = RecordComponent()
            element.read(input)
            return element
        }
    }
}