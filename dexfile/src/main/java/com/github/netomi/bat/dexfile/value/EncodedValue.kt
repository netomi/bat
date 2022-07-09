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
package com.github.netomi.bat.dexfile.value

import com.github.netomi.bat.dexfile.DexFile
import com.github.netomi.bat.dexfile.io.DexDataInput
import com.github.netomi.bat.dexfile.io.DexDataOutput
import com.github.netomi.bat.dexfile.io.DexFormatException
import com.github.netomi.bat.dexfile.visitor.EncodedValueVisitor
import com.github.netomi.bat.util.Primitives

/**
 * An abstract base class representing an encoded value inside a dex file.
 *
 * @see [value encoding @ dex format](https://source.android.com/devices/tech/dalvik/dex-format.encoding)
 */
abstract class EncodedValue {
    abstract val valueType: Int

    abstract fun readValue(input: DexDataInput, valueArg: Int)

    protected fun writeType(output: DexDataOutput, valueArg: Int): Int {
        val typeAndArg = valueArg shl 5 or valueType
        output.writeUnsignedByte(typeAndArg.toShort())
        return valueArg
    }

    protected abstract fun writeType(output: DexDataOutput): Int

    fun write(output: DexDataOutput) {
        val valueArg = writeType(output)
        writeValue(output, valueArg)
    }

    abstract fun writeValue(output: DexDataOutput, valueArg: Int)

    abstract fun accept(dexFile: DexFile, visitor: EncodedValueVisitor)

    abstract override fun hashCode(): Int
    abstract override fun equals(other: Any?): Boolean

    companion object {
        // Value types.
        const val VALUE_BYTE          = 0x00
        const val VALUE_SHORT         = 0x02
        const val VALUE_CHAR          = 0x03
        const val VALUE_INT           = 0x04
        const val VALUE_LONG          = 0x06
        const val VALUE_FLOAT         = 0x10
        const val VALUE_DOUBLE        = 0x11
        const val VALUE_METHOD_TYPE   = 0x15
        const val VALUE_METHOD_HANDLE = 0x16
        const val VALUE_STRING        = 0x17
        const val VALUE_TYPE          = 0x18
        const val VALUE_FIELD         = 0x19
        const val VALUE_METHOD        = 0x1a
        const val VALUE_ENUM          = 0x1b
        const val VALUE_ARRAY         = 0x1c
        const val VALUE_ANNOTATION    = 0x1d
        const val VALUE_NULL          = 0x1e
        const val VALUE_BOOLEAN       = 0x1f

        @JvmStatic
        fun read(input: DexDataInput): EncodedValue {
            val typeAndArg = input.readUnsignedByte().toInt()
            val valueArg   = typeAndArg ushr 5
            val valueType  = typeAndArg and 0x1f
            val encodedValue = create(valueType)
            encodedValue.readValue(input, valueArg)
            return encodedValue
        }

        private fun create(valueType: Int): EncodedValue {
            return when (valueType) {
                VALUE_BYTE          -> EncodedByteValue()
                VALUE_SHORT         -> EncodedShortValue()
                VALUE_CHAR          -> EncodedCharValue()
                VALUE_INT           -> EncodedIntValue()
                VALUE_LONG          -> EncodedLongValue()
                VALUE_FLOAT         -> EncodedFloatValue()
                VALUE_DOUBLE        -> EncodedDoubleValue()
                VALUE_METHOD_TYPE   -> EncodedMethodTypeValue()
                VALUE_METHOD_HANDLE -> EncodedMethodHandleValue()
                VALUE_STRING        -> EncodedStringValue()
                VALUE_TYPE          -> EncodedTypeValue()
                VALUE_FIELD         -> EncodedFieldValue()
                VALUE_METHOD        -> EncodedMethodValue()
                VALUE_ENUM          -> EncodedEnumValue()
                VALUE_ARRAY         -> EncodedArrayValue()
                VALUE_ANNOTATION    -> EncodedAnnotationValue()
                VALUE_NULL          -> EncodedNullValue
                VALUE_BOOLEAN       -> EncodedBooleanValue()
                else -> throw DexFormatException("Unexpected EncodedValue type: ${Primitives.toHexString(valueType.toShort())}")
            }
        }

        @JvmStatic
        fun readAnnotationValue(input: DexDataInput): EncodedAnnotationValue {
            val annotation = EncodedAnnotationValue()
            annotation.readValue(input, 0)
            return annotation
        }

        @JvmStatic
        fun requiredBytesForSignedInt(value: Int): Int {
            return when (value) {
                value shl 24 shr 24 -> 1
                value shl 16 shr 16 -> 2
                value shl  8 shr  8 -> 3
                else                -> 4
            }
        }

        @JvmStatic
        fun requiredBytesForSignedLong(value: Long): Int {
            return when (value) {
                value shl 56 shr 56 -> 1
                value shl 48 shr 48 -> 2
                value shl 40 shr 40 -> 3
                value shl 32 shr 32 -> 4
                value shl 24 shr 24 -> 5
                value shl 16 shr 16 -> 6
                value shl  8 shr  8 -> 7
                else                -> 8
            }
        }

        @JvmStatic
        fun requiredBytesForSignedShort(value: Short): Int {
            val v = value.toInt()
            return if (v shl 24 shr 24 == v) 1 else 2
        }

        @JvmStatic
        fun requiredBytesForUnsignedChar(value: Char): Int {
            return if (value.code shl 24 ushr 24 == value.code) 1 else 2
        }

        @JvmStatic
        fun requiredBytesForUnsignedInt(value: Int): Int {
            return when (value) {
                value shl 24 ushr 24 -> 1
                value shl 16 ushr 16 -> 2
                value shl  8 ushr  8 -> 3
                else                 -> 4
            }
        }

        @JvmStatic
        fun requiredBytesForFloat(value: Float): Int {
            val bits = java.lang.Float.floatToIntBits(value)

            return      if (bits shl  8 == 0) 1
                   else if (bits shl 16 == 0) 2
                   else if (bits shl 24 == 0) 3
                   else                       4
        }

        @JvmStatic
        fun requiredBytesForDouble(value: Double): Int {
            val bits = java.lang.Double.doubleToLongBits(value)

            return      if (bits shl  8 == 0L) 1
                   else if (bits shl 16 == 0L) 2
                   else if (bits shl 24 == 0L) 3
                   else if (bits shl 32 == 0L) 4
                   else if (bits shl 40 == 0L) 5
                   else if (bits shl 48 == 0L) 6
                   else if (bits shl 56 == 0L) 7
                   else                        8
        }
    }
}