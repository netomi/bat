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
package com.github.netomi.bat.classfile.attribute.annotation

import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.attribute.annotation.visitor.ElementValueVisitor
import com.github.netomi.bat.util.JvmType
import com.github.netomi.bat.util.mutableListOfCapacity
import java.io.DataInput
import java.io.DataOutput
import java.io.IOException
import java.util.*

open class Annotation
    protected constructor(protected var _typeIndex:     Int                                  = -1,
                          protected var _elementValues: MutableList<Pair<Int, ElementValue>> = mutableListOfCapacity(0)) {

    open val dataSize: Int
        get() = 4 + _elementValues.fold(0) { acc, (_, value) -> acc + 2 + value.dataSize }

    val typeIndex: Int
        get() = _typeIndex

    val elementValues: List<Pair<Int, ElementValue>>
        get() = _elementValues

    fun getType(classFile: ClassFile): JvmType {
        return classFile.getType(typeIndex)
    }

    @Throws(IOException::class)
    protected open fun read(input: DataInput) {
        _typeIndex = input.readUnsignedShort()

        val elementValuesCount = input.readUnsignedShort()
        _elementValues = mutableListOfCapacity(elementValuesCount)
        for (i in 0 until elementValuesCount) {
            val elementNameIndex = input.readUnsignedShort()
            _elementValues.add(Pair(elementNameIndex, ElementValue.read(input)))
        }
    }

    @Throws(IOException::class)
    internal open fun write(output: DataOutput) {
        output.writeShort(typeIndex)
        output.writeShort(elementValues.size)
        elementValues.forEach { (elementNameIndex, elementValue) ->
            output.writeShort(elementNameIndex)
            elementValue.write(output)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Annotation) return false

        return _typeIndex     == other._typeIndex &&
               _elementValues == other._elementValues
    }

    override fun hashCode(): Int {
        return Objects.hash(_typeIndex, _elementValues)
    }

    fun elementValuesAccept(classFile: ClassFile, visitor: ElementValueVisitor) {
        elementValues.forEach { (_, elementValue) -> elementValue.accept(classFile, visitor) }
    }

    companion object {
        internal fun empty(): Annotation {
            return Annotation()
        }

        internal fun readAnnotation(input: DataInput): Annotation {
            val annotation = Annotation()
            annotation.read(input)
            return annotation
        }
    }
}