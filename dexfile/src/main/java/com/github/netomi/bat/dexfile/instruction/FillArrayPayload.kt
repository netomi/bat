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
package com.github.netomi.bat.dexfile.instruction

import com.github.netomi.bat.dexfile.ClassDef
import com.github.netomi.bat.dexfile.Code
import com.github.netomi.bat.dexfile.DexFile
import com.github.netomi.bat.dexfile.EncodedMethod
import com.github.netomi.bat.dexfile.instruction.visitor.InstructionVisitor

class FillArrayPayload private constructor(_elementWidth: Int = 0, _values: ByteArray = EMPTY_VALUES) : Payload(DexOpCode.NOP) {

    var elementWidth = _elementWidth
        internal set

    var values: ByteArray = _values
        internal set

    override val length: Int
        get() = (values.size + 1) / 2 + 4

    val elements: Int
        get() = values.size / elementWidth

    fun getElementAsLong(index: Int): Long {
        var result: Long = 0
        var currentIndex = index * elementWidth

        var shift = 0
        var i     = 0

        while (i < elementWidth) {
            result = result or (values[currentIndex].toLong() and 0xffL shl shift)
            shift += 8
            i++
            currentIndex++
        }

        return result
    }

    fun getElementAsInt(index: Int): Int {
        var result = 0
        var currentIndex = index * elementWidth

        var shift = 0
        var i     = 0

        while (i < elementWidth) {
            result = (result.toLong() or (values[currentIndex].toLong() and 0xffL shl shift)).toInt()
            shift += 8
            i++
            currentIndex++
        }

        return result
    }

    fun getElementAsShort(index: Int): Short {
        var result: Short = 0
        var currentIndex = index * elementWidth

        var shift = 0
        var i     = 0

        while (i < elementWidth) {
            result = (result.toLong() or (values[currentIndex].toLong() and 0xffL shl shift)).toShort()
            shift += 8
            i++
            currentIndex++
        }

        return result
    }

    fun getElementAsByte(index: Int): Byte {
        return values[index]
    }

    override fun read(instructions: ShortArray, offset: Int) {
        var currOffset = offset

        elementWidth =  instructions[++currOffset].toInt() and 0xffff

        val elements = (instructions[++currOffset].toInt() and 0xffff) or
                       (instructions[++currOffset].toInt() shl 16)

        val size = elements * elementWidth
        values = ByteArray(size)

        var idx = 0
        while (idx < size) {
            values[idx++] = instructions[++currOffset].toByte()
            if (idx < size) {
                values[idx++] = (instructions[currOffset].toInt() shr 8).toByte()
            }
        }
    }

    override fun writeData(): ShortArray {
        val data = ShortArray(length)
        data[0] = ((opCode.opCode.toInt() and 0xff) or (IDENT shl 8)).toShort()
        data[1] = elementWidth.toShort()
        data[2] = elements.toShort()
        data[3] = (elements shr 16).toShort()

        var i = 0
        var j = 4
        while (i < values.size) {
            data[j] = values[i++].toShort()
            if (i < values.size) {
                data[j] = (data[j].toInt() or ((values[i++].toInt() and 0xff) shl 8)).toShort()
            }
            j++
        }

        return data
    }

    override fun accept(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, visitor: InstructionVisitor) {
        visitor.visitFillArrayPayload(dexFile, classDef, method, code, offset, this)
    }

    override fun toString(): String {
        return "array-data (${length} units)"
    }

    companion object {
        internal const val IDENT        = 0x03
        private        val EMPTY_VALUES = ByteArray(0)

        fun empty(): FillArrayPayload {
            return FillArrayPayload()
        }

        fun of(values: ByteArray): FillArrayPayload {
            return FillArrayPayload(1, values)
        }

        fun of(values: ShortArray): FillArrayPayload {
            val elementWidth = 2
            val byteValues   = ByteArray(values.size * elementWidth)

            var index = 0
            for (i in values.indices) {
                var element = values[i]
                var j = 0
                while (j++ < elementWidth) {
                    byteValues[index++] = (element.toInt() and 0xff).toByte()
                    element = (element.toInt() shr 8).toShort()
                }
            }

            return FillArrayPayload(elementWidth, byteValues)
        }

        fun of(values: IntArray): FillArrayPayload {
            val elementWidth = 4
            val byteValues   = ByteArray(values.size * elementWidth)

            var index = 0
            for (i in values.indices) {
                var element = values[i]
                var j = 0
                while (j++ < elementWidth) {
                    byteValues[index++] = (element and 0xff).toByte()
                    element = element shr 8
                }
            }

            return FillArrayPayload(elementWidth, byteValues)
        }

        fun of(values: LongArray): FillArrayPayload {
            val elementWidth = 8
            val byteValues   = ByteArray(values.size * elementWidth)

            var index = 0
            for (i in values.indices) {
                var element = values[i]
                var j = 0
                while (j++ < elementWidth) {
                    byteValues[index++] = (element and 0xff).toByte()
                    element = element shr 8
                }
            }

            return FillArrayPayload(elementWidth, byteValues)
        }
    }
}