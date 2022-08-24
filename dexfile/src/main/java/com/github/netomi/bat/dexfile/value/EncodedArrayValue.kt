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

package com.github.netomi.bat.dexfile.value

import com.github.netomi.bat.dexfile.DexFile
import com.github.netomi.bat.dexfile.io.DexDataInput
import com.github.netomi.bat.dexfile.io.DexDataOutput
import com.github.netomi.bat.dexfile.value.visitor.EncodedArrayVisitor
import com.github.netomi.bat.dexfile.value.visitor.EncodedValueVisitor
import com.github.netomi.bat.dexfile.visitor.ReferencedIDVisitor
import com.github.netomi.bat.util.mutableListOfCapacity

/**
 * A class representing an array of values inside a dex file.
 */
data class EncodedArrayValue
    internal constructor(private var values: MutableList<EncodedValue> = mutableListOfCapacity(0)) : EncodedValue(), Sequence<EncodedValue> {

    override val valueType: EncodedValueType
        get() = EncodedValueType.ARRAY

    val isEmpty: Boolean
        get() = values.isEmpty()

    val size: Int
        get() = values.size

    operator fun get(index: Int): EncodedValue {
        return values[index]
    }

    internal fun add(value: EncodedValue) {
        values.add(value)
    }

    internal operator fun set(idx: Int, value: EncodedValue) {
        values[idx] = value
    }

    override fun iterator(): Iterator<EncodedValue> {
        return values.iterator()
    }

    override fun readValue(input: DexDataInput, valueArg: Int) {
        val size = input.readUleb128()
        values   = mutableListOfCapacity(size)
        for (i in 0 until size) {
            values.add(read(input))
        }
    }

    override fun writeType(output: DexDataOutput): Int {
        return writeType(output, 0)
    }

    override fun writeValue(output: DexDataOutput, valueArg: Int) {
        output.writeUleb128(values.size)
        for (value in values) {
            value.write(output)
        }
    }

    fun accept(dexFile: DexFile, visitor: EncodedArrayVisitor) {
        values.forEachIndexed { index, encodedValue -> visitor.visitEncodedValue(dexFile, this, index, encodedValue ) }
    }

    override fun accept(dexFile: DexFile, visitor: EncodedValueVisitor) {
        visitor.visitArrayValue(dexFile, this)
    }

    override fun referencedIDsAccept(dexFile: DexFile, visitor: ReferencedIDVisitor) {
        values.forEach { it.referencedIDsAccept(dexFile, visitor) }
    }

    fun valuesAccept(dexFile: DexFile, visitor: EncodedValueVisitor) {
        for (value in values) {
            value.accept(dexFile, visitor)
        }
    }

    fun valueAccept(dexFile: DexFile, index: Int, visitor: EncodedValueVisitor) {
        if (index >= 0 && index < values.size) {
            values[index].accept(dexFile, visitor)
        }
    }

    override fun toString(): String {
        return "EncodedArrayValue[values=${values.size} items]"
    }

    companion object {
        internal fun empty(): EncodedArrayValue {
            return EncodedArrayValue()
        }

        fun of(values: List<EncodedValue>): EncodedArrayValue {
            return EncodedArrayValue(values.toMutableList())
        }

        fun of(vararg values: EncodedValue): EncodedArrayValue {
            val array = EncodedArrayValue()
            array.values.addAll(values)
            return array
        }
    }
}