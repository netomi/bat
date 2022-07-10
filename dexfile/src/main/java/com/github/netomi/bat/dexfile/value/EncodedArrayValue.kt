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
import com.github.netomi.bat.dexfile.visitor.EncodedValueVisitor
import dev.ahmedmourad.nocopy.annotations.NoCopy
import kotlin.collections.ArrayList

/**
 * A class representing an array of values inside a dex file.
 */
@NoCopy
data class EncodedArrayValue internal constructor(private val values_: ArrayList<EncodedValue> = ArrayList(0)) : EncodedValue() {

    override val valueType: Int
        get() = VALUE_ARRAY

    val encodedValues: List<EncodedValue>
        get() = values_

    val encodedValueCount: Int
        get() = values_.size

    private constructor(vararg values: EncodedValue) : this(arrayListOf(*values))

    fun getEncodedValue(index: Int): EncodedValue {
        return values_[index]
    }

    fun addEncodedValue(value: EncodedValue) {
        values_.add(value)
    }

    fun setEncodedValue(idx: Int, value: EncodedValue) {
        values_[idx] = value
    }

    override fun readValue(input: DexDataInput, valueArg: Int) {
        val size = input.readUleb128()
        values_.clear()
        values_.ensureCapacity(size)
        for (i in 0 until size) {
            values_.add(read(input))
        }
    }

    override fun writeType(output: DexDataOutput): Int {
        return writeType(output, 0)
    }

    override fun writeValue(output: DexDataOutput, valueArg: Int) {
        output.writeUleb128(values_.size)
        for (value in values_) {
            value.write(output)
        }
    }

    override fun accept(dexFile: DexFile, visitor: EncodedValueVisitor) {
        visitor.visitArrayValue(dexFile, this)
    }

    fun valuesAccept(dexFile: DexFile, visitor: EncodedValueVisitor) {
        for (value in values_) {
            value.accept(dexFile, visitor)
        }
    }

    fun valueAccept(dexFile: DexFile, index: Int, visitor: EncodedValueVisitor) {
        if (index >= 0 && index < values_.size) {
            values_[index].accept(dexFile, visitor)
        }
    }

    override fun toString(): String {
        return "EncodedArrayValue[values=${values_}]"
    }

    companion object {
        @JvmStatic
        fun empty(): EncodedArrayValue {
            return EncodedArrayValue()
        }

        @JvmStatic
        fun of(vararg values: EncodedValue): EncodedArrayValue {
            return EncodedArrayValue(*values)
        }
    }
}