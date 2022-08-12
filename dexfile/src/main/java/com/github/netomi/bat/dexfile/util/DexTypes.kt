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

    fun getDefaultEncodedValueForType(): EncodedValue {
        when (type) {
            BYTE_TYPE    -> return EncodedByteValue.of(0x00.toByte())
            SHORT_TYPE   -> return EncodedShortValue.of(0x00.toShort())
            CHAR_TYPE    -> return EncodedCharValue.of(0x00.toChar())
            INT_TYPE     -> return EncodedIntValue.of(0)
            LONG_TYPE    -> return EncodedLongValue.of(0L)
            FLOAT_TYPE   -> return EncodedFloatValue.of(0.0f)
            DOUBLE_TYPE  -> return EncodedDoubleValue.of(0.0)
            BOOLEAN_TYPE -> return EncodedBooleanValue.of(false)
        }
        return if (type.startsWith("L") && type.endsWith(";")) {
            EncodedNullValue
        } else {
            EncodedNullValue
        }
    }

    companion object {
        fun of(type: String): DexType {
            return DexType(type)
        }
    }
}

fun toShortyFormat(parameterTypes: List<String>, returnType: String): String {
    var result = returnType.asDexType().toShortyFormat()

    for (parameter in parameterTypes) {
        result += parameter.asDexType().toShortyFormat()
    }

    return result
}

fun getArgumentSize(parameterTypes: Iterable<String>): Int {
    return parameterTypes.fold(0) { size, type -> size + getArgumentSizeForType(type) }
}

fun getArgumentSizeForType(type: String): Int {
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
