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

import com.github.netomi.bat.classfile.io.ClassDataInput
import com.github.netomi.bat.classfile.io.ClassDataOutput
import com.github.netomi.bat.classfile.io.ClassFileContent
import com.github.netomi.bat.classfile.io.contentSize
import com.github.netomi.bat.util.mutableListOfCapacity
import java.util.EnumMap

data class AttributeMap constructor(private var _attributes: MutableList<Attribute> = mutableListOfCapacity(0))
    : ClassFileContent(), Sequence<Attribute> {

    private val typeToAttributeMap: MutableMap<AttributeType, Attribute> by lazy { EnumMap(AttributeType::class.java) }

    init {
        for (attribute in _attributes) {
            addAttributeToTypeMap(attribute)
        }
    }

    override val contentSize: Int
        get() = _attributes.contentSize()

    val size: Int
        get() = _attributes.size

    operator fun get(index: Int): Attribute {
        return _attributes[index]
    }

    internal inline operator fun <reified T: Attribute> get(type: AttributeType): T? {
        require(type != AttributeType.UNKNOWN) { "attributeType 'UNKNOWN' not supported for retrieval" }
        return typeToAttributeMap[type] as T?
    }

    override fun iterator(): Iterator<Attribute> {
        return _attributes.iterator()
    }

    private fun addAttribute(attribute: Attribute) {
        _attributes.add(attribute)
        addAttributeToTypeMap(attribute)
    }

    private fun addAttributeToTypeMap(attribute: Attribute) {
        // UNKNOWN attributes are not supported by the type cache
        if (attribute.type != AttributeType.UNKNOWN) {
            typeToAttributeMap[attribute.type] = attribute
        }
    }

    private fun read(input: ClassDataInput) {
        val attributeCount = input.readUnsignedShort()
        _attributes = mutableListOfCapacity(attributeCount)
        for (i in 0 until attributeCount) {
            addAttribute(Attribute.readAttribute(input, input.classFile))
        }
    }

    override fun write(output: ClassDataOutput) {
        output.writeShort(_attributes.size)
        for (attribute in _attributes) {
            attribute.write(output)
        }
    }

    companion object {
        internal fun empty(): AttributeMap {
            return AttributeMap()
        }

        internal fun read(input: ClassDataInput): AttributeMap {
            val attributeMap = AttributeMap()
            attributeMap.read(input)
            return attributeMap
        }
    }
}