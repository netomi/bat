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

package com.github.netomi.bat.classfile.annotation

import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.annotation.visitor.ElementValueVisitor
import com.github.netomi.bat.util.mutableListOfCapacity
import java.io.DataInput
import java.io.DataOutput
import java.io.IOException

data class ArrayElementValue
    private constructor(private var _elementValues: MutableList<ElementValue> = mutableListOfCapacity(0)) : ElementValue() {

    override val type: ElementValueType
        get() = ElementValueType.ARRAY

    val elementValues: List<ElementValue>
        get() = _elementValues

    @Throws(IOException::class)
    override fun readElementValue(input: DataInput) {
        val elementValueCount = input.readUnsignedShort()
        _elementValues = mutableListOfCapacity(elementValueCount)
        for (i in 0 until elementValueCount) {
            _elementValues.add(read(input))
        }
    }

    @Throws(IOException::class)
    override fun writeElementValue(output: DataOutput) {
        output.writeShort(elementValues.size)
        elementValues.forEach { it.write(output) }
    }

    override fun accept(classFile: ClassFile, visitor: ElementValueVisitor) {
        return visitor.visitArrayElementValue(classFile, this)
    }

    fun elementValuesAccept(classFile: ClassFile, visitor: ElementValueVisitor) {
        for (elementValue in elementValues) {
            elementValue.accept(classFile, visitor)
        }
    }

    companion object {
        internal fun empty(): ArrayElementValue {
            return ArrayElementValue()
        }
    }
}