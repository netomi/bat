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
import com.github.netomi.bat.dexfile.value.visitor.EncodedValueVisitor
import com.github.netomi.bat.dexfile.visitor.ReferencedIDVisitor
import kotlin.collections.ArrayList

/**
 * A class representing an array of values inside a dex file.
 */
data class EncodedArrayValue internal constructor(val values: ArrayList<EncodedValue> = ArrayList(0)) : EncodedValue() {

    override val valueType: EncodedValueType
        get() = EncodedValueType.ARRAY

    fun isEmpty(): Boolean {
        return values.isEmpty()
    }

    fun addEncodedValue(value: EncodedValue) {
        values.add(value)
    }

    fun setEncodedValue(idx: Int, value: EncodedValue) {
        values[idx] = value
    }

    override fun readValue(input: DexDataInput, valueArg: Int) {
        val size = input.readUleb128()
        values.clear()
        values.ensureCapacity(size)
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
        return "EncodedArrayValue[values=${values}]"
    }

    companion object {
        fun empty(): EncodedArrayValue {
            return EncodedArrayValue()
        }

        fun of(vararg values: EncodedValue): EncodedArrayValue {
            return EncodedArrayValue(arrayListOf(*values))
        }
    }
}