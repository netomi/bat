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
package com.github.netomi.bat.dexfile

import com.github.netomi.bat.dexfile.io.DexDataInput
import com.github.netomi.bat.dexfile.io.DexDataOutput
import com.github.netomi.bat.dexfile.value.EncodedArrayValue
import com.github.netomi.bat.dexfile.visitor.EncodedValueVisitor
import com.github.netomi.bat.dexfile.visitor.ReferencedIDVisitor
import java.util.*

@DataItemAnn(
    type          = TYPE_ENCODED_ARRAY_ITEM,
    dataAlignment = 1,
    dataSection   = true)
open class EncodedArray protected constructor(val array: EncodedArrayValue = EncodedArrayValue.empty()) : DataItem() {

    override val isEmpty: Boolean
        get() = array.isEmpty()

    override fun read(input: DexDataInput) {
        array.readValue(input, 0)
    }

    override fun write(output: DexDataOutput) {
        array.writeValue(output, 0)
    }

    fun accept(dexFile: DexFile, visitor: EncodedValueVisitor) {
        for (i in 0 until array.values.size) {
            array.values[i].accept(dexFile, visitor)
        }
    }

    fun accept(dexFile: DexFile, index: Int, visitor: EncodedValueVisitor) {
        if (index >= 0 && index < array.values.size) {
            array.values[index].accept(dexFile, visitor)
        }
    }

    internal fun referencedIDsAccept(dexFile: DexFile, visitor: ReferencedIDVisitor) {
        array.referencedIDsAccept(dexFile, visitor)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val o = other as EncodedArray
        return array == o.array
    }

    override fun hashCode(): Int {
        return Objects.hash(array)
    }

    override fun toString(): String {
        return "EncodedArray[array=${array}]"
    }

    companion object {
        fun empty(): EncodedArray {
            return EncodedArray()
        }

        fun readContent(input: DexDataInput): EncodedArray {
            val encodedArray = EncodedArray()
            encodedArray.read(input)
            return encodedArray
        }
    }
}