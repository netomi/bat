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

package com.github.netomi.bat.tinydvm.data

import com.github.netomi.bat.dexfile.*
import com.github.netomi.bat.tinydvm.data.PrimitiveType.*

class DvmPrimitiveValue private constructor(private val _value: Long, private val primitiveType: PrimitiveType): DvmValue() {

    override val type: String
        get() = primitiveType.typeString

    override val value: Any
        get() = valueOfType(type)

    override fun valueOfType(type: String): Any {
        return when (type) {
            INT_TYPE           -> _value.toInt()
            LONG_TYPE          -> _value
            SHORT_TYPE         -> _value.toShort()
            BYTE_TYPE          -> _value.toByte()
            CHAR_TYPE          -> _value.toInt().toChar()
            BOOLEAN_TYPE       -> _value.toInt() != 0
            FLOAT_TYPE         -> Float.fromBits(_value.toInt())
            DOUBLE_TYPE        -> Double.fromBits(_value)
            UNKNOWN.typeString -> _value
            else -> throw IllegalArgumentException("unexpected primitive type $type")
        }
    }

    override fun withType(newType: String): DvmValue {
        return if (newType != type) {
            DvmPrimitiveValue(_value, PrimitiveType.of(newType))
        } else {
            this
        }
    }

    companion object {
        fun of(value: Byte): DvmPrimitiveValue {
            return DvmPrimitiveValue(value.toLong(), BYTE)
        }

        fun of(value: Char): DvmPrimitiveValue {
            return DvmPrimitiveValue(value.code.toLong(), CHAR)
        }

        fun of(value: Short): DvmPrimitiveValue {
            return DvmPrimitiveValue(value.toLong(), SHORT)
        }

        fun of(value: Boolean): DvmPrimitiveValue {
            return DvmPrimitiveValue(if (value) 1L else 0L, BOOLEAN)
        }

        fun of(value: Int): DvmPrimitiveValue {
            return DvmPrimitiveValue(value.toLong(), INT)
        }

        fun of(value: Long): DvmPrimitiveValue {
            return DvmPrimitiveValue(value, LONG)
        }

        fun of(value: Float): DvmPrimitiveValue {
            return DvmPrimitiveValue(value.toBits().toLong(), FLOAT)
        }

        fun of(value: Double): DvmPrimitiveValue {
            return DvmPrimitiveValue(value.toBits(), DOUBLE)
        }

        fun ofUnknownType(value: Long): DvmPrimitiveValue {
            return DvmPrimitiveValue(value, UNKNOWN)
        }
    }
}

enum class PrimitiveType constructor(val typeString: String) {
    INT    (INT_TYPE),
    LONG   (LONG_TYPE),
    SHORT  (SHORT_TYPE),
    BYTE   (BYTE_TYPE),
    CHAR   (CHAR_TYPE),
    BOOLEAN(BOOLEAN_TYPE),
    FLOAT  (FLOAT_TYPE),
    DOUBLE (DOUBLE_TYPE),
    // TODO: support imprecise types instead of just using unknown
    //       we need to distinguish between wide values and normal ones.
    UNKNOWN("?");

    companion object {
        fun of(type: String): PrimitiveType {
            for (item in values()) {
                if (type == item.typeString) {
                    return item
                }
            }

            throw RuntimeException("unexpected primitive type $type")
        }
    }
}