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

package com.github.netomi.bat.dexfile.util

import com.github.netomi.bat.dexfile.value.*
import com.github.netomi.bat.util.*

fun String.asDexType(): DexType {
    return DexType.of(this)
}

class DexType private constructor(type: String): JavaType(type) {

    fun toShortyFormat(): String {
        return if (isClassType || isArrayType) "L" else type
    }

    fun getArgumentSize(): Int {
        return when (type) {
            BYTE_TYPE,
            SHORT_TYPE,
            CHAR_TYPE,
            INT_TYPE,
            BOOLEAN_TYPE,
            FLOAT_TYPE  -> 1

            LONG_TYPE,
            DOUBLE_TYPE -> 2

            else -> 1
        }
    }

    fun getDefaultEncodedValueForType(): EncodedValue {
        return if (isClassType) {
            EncodedNullValue
        } else {
            when (type) {
                BYTE_TYPE    -> EncodedByteValue.of(0x00.toByte())
                SHORT_TYPE   -> EncodedShortValue.of(0x00.toShort())
                CHAR_TYPE    -> EncodedCharValue.of(0x00.toChar())
                INT_TYPE     -> EncodedIntValue.of(0)
                LONG_TYPE    -> EncodedLongValue.of(0L)
                FLOAT_TYPE   -> EncodedFloatValue.of(0.0f)
                DOUBLE_TYPE  -> EncodedDoubleValue.of(0.0)
                BOOLEAN_TYPE -> EncodedBooleanValue.of(false)
                else         -> EncodedNullValue
            }
        }
    }

    companion object {
        fun of(type: String): DexType {
            return DexType(type)
        }
    }
}

internal fun toShortyFormat(parameterTypes: List<String>, returnType: String): String {
    var result = returnType.asDexType().toShortyFormat()

    for (parameter in parameterTypes) {
        result += parameter.asDexType().toShortyFormat()
    }

    return result
}

fun Iterable<DexType>.getArgumentSize(): Int {
    return fold(0) { size, type -> size + type.getArgumentSize() }
}