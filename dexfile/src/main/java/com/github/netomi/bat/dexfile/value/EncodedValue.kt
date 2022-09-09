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

import com.github.netomi.bat.dexfile.*
import com.github.netomi.bat.dexfile.io.DexDataInput
import com.github.netomi.bat.dexfile.io.DexDataOutput
import com.github.netomi.bat.dexfile.io.DexFormatException
import com.github.netomi.bat.dexfile.value.visitor.EncodedValueVisitor
import com.github.netomi.bat.dexfile.visitor.ReferencedIDVisitor
import com.github.netomi.bat.util.toHexStringWithPrefix

/**
 * An abstract base class representing an encoded value inside a dex file.
 *
 * @see <a href="https://source.android.com/devices/tech/dalvik/dex-format#encoding">value encoding @ dex format</a>
 */
abstract class EncodedValue {
    abstract val valueType: EncodedValueType

    internal abstract fun readValue(input: DexDataInput, valueArg: Int)

    protected fun writeType(output: DexDataOutput, valueArg: Int): Int {
        val typeAndArg = valueArg shl 5 or valueType.value
        output.writeUnsignedByte(typeAndArg.toShort())
        return valueArg
    }

    protected abstract fun writeType(output: DexDataOutput): Int

    internal fun write(output: DexDataOutput) {
        val valueArg = writeType(output)
        writeValue(output, valueArg)
    }

    internal abstract fun writeValue(output: DexDataOutput, valueArg: Int)

    abstract fun accept(dexFile: DexFile, visitor: EncodedValueVisitor)

    internal abstract fun referencedIDsAccept(dexFile: DexFile, visitor: ReferencedIDVisitor)

    abstract override fun hashCode(): Int

    abstract override fun equals(other: Any?): Boolean

    companion object {
        fun read(input: DexDataInput): EncodedValue {
            val typeAndArg = input.readUnsignedByte().toInt()
            val valueArg   = typeAndArg ushr 5
            val valueType  = typeAndArg and 0x1f
            val encodedValue = create(valueType)
            encodedValue.readValue(input, valueArg)
            return encodedValue
        }

        private fun create(valueType: Int): EncodedValue {
            return EncodedValueType.of(valueType)?.supplier?.invoke()
                ?: throw DexFormatException("unexpected encoded value type: ${toHexStringWithPrefix(valueType.toByte())}")
        }

        fun readAnnotationValue(input: DexDataInput): EncodedAnnotationValue {
            val annotation = EncodedAnnotationValue.empty()
            annotation.readValue(input, 0)
            return annotation
        }

        fun requiredBytesForSignedInt(value: Int): Int {
            return when (value) {
                value shl 24 shr 24 -> 1
                value shl 16 shr 16 -> 2
                value shl  8 shr  8 -> 3
                else                -> 4
            }
        }

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

        fun requiredBytesForSignedShort(value: Short): Int {
            val v = value.toInt()
            return if (v shl 24 shr 24 == v) 1 else 2
        }

        fun requiredBytesForUnsignedChar(value: Char): Int {
            return if (value.code shl 24 ushr 24 == value.code) 1 else 2
        }

        fun requiredBytesForUnsignedInt(value: Int): Int {
            return when (value) {
                value shl 24 ushr 24 -> 1
                value shl 16 ushr 16 -> 2
                value shl  8 ushr  8 -> 3
                else                 -> 4
            }
        }

        fun requiredBytesForFloat(value: Float): Int {
            val bits = java.lang.Float.floatToIntBits(value)

            return      if (bits shl  8 == 0) 1
                   else if (bits shl 16 == 0) 2
                   else if (bits shl 24 == 0) 3
                   else                       4
        }

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

enum class EncodedValueType constructor(val value: Int, val supplier: () -> EncodedValue) {
    BYTE          (VALUE_BYTE,          { EncodedByteValue.empty() }),
    SHORT         (VALUE_SHORT,         { EncodedShortValue.empty() }),
    CHAR          (VALUE_CHAR,          { EncodedCharValue.empty() }),
    INT           (VALUE_INT,           { EncodedIntValue.empty() }),
    LONG          (VALUE_LONG,          { EncodedLongValue.empty() }),
    FLOAT         (VALUE_FLOAT,         { EncodedFloatValue.empty() }),
    DOUBLE        (VALUE_DOUBLE,        { EncodedDoubleValue.empty() }),
    METHOD_TYPE   (VALUE_METHOD_TYPE,   { EncodedMethodTypeValue.empty() }),
    METHOD_HANDLE (VALUE_METHOD_HANDLE, { EncodedMethodHandleValue.empty() }),
    STRING        (VALUE_STRING,        { EncodedStringValue.empty() }),
    TYPE          (VALUE_TYPE,          { EncodedTypeValue.empty() }),
    FIELD         (VALUE_FIELD,         { EncodedFieldValue.empty() }),
    METHOD        (VALUE_METHOD,        { EncodedMethodValue.empty() }),
    ENUM          (VALUE_ENUM,          { EncodedEnumValue.empty() }),
    ARRAY         (VALUE_ARRAY,         { EncodedArrayValue.empty() }),
    ANNOTATION    (VALUE_ANNOTATION,    { EncodedAnnotationValue.empty() }),
    NULL          (VALUE_NULL,          { EncodedNullValue }),
    BOOLEAN       (VALUE_BOOLEAN,       { EncodedBooleanValue.empty() });

    companion object {
        private val valueToTypeMap: Map<Int, EncodedValueType> by lazy {
            values().associateBy { it.value }
        }

        fun of(valueType: Int) : EncodedValueType? {
            return valueToTypeMap[valueType]
        }
    }
}