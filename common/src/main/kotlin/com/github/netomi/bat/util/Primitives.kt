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

package com.github.netomi.bat.util

fun toHexString(value: Byte): String {
    return "%02x".format(value)
}

fun toHexStringWithPrefix(value: Byte): String {
    return "0x" + toHexString(value)
}

fun toHexString(value: Short): String {
    return "%04x".format(value)
}

fun toHexStringWithPrefix(value: Short): String {
    return "0x" + toHexString(value)
}

fun toHexString(value: Int): String {
    return "%08x".format(value)
}

fun toSignedHexString(value: Int, digits: Int): String {
    return buildString {
        val hexValue: String = if (value < 0) {
            Integer.toHexString(-value)
        } else {
            Integer.toHexString(value)
        }
        val leadingZeros = digits - hexValue.length
        append(if (value < 0) "-" else "+")
        append("0".repeat(0.coerceAtLeast(leadingZeros)))
        append(hexValue)
    }
}

fun toSignedHexString(value: Int): String {
    return buildString {
        val hexValue: String = if (value < 0) {
            Integer.toHexString(-value)
        } else {
            Integer.toHexString(value)
        }
        append(if (value < 0) "-" else "+")
        append(hexValue)
    }
}

fun toSignedHexString(value: Long): String {
    return buildString {
        val hexValue: String = if (value < 0) {
            java.lang.Long.toHexString(-value)
        } else {
            java.lang.Long.toHexString(value)
        }
        append(if (value < 0) "-" else "+")
        append(hexValue)
    }
}

fun toHexString(value: Int, digits: Int): String {
    return buildString {
        val hexValue     = Integer.toHexString(value)
        val leadingZeros = digits - hexValue.length
        append("0".repeat(0.coerceAtLeast(leadingZeros)))
        append(hexValue)
    }
}

fun toHexStringWithPrefix(value: Int): String {
    return "0x" + toHexString(value)
}

fun toHexString(value: Long): String {
    return "%08x".format(value)
}

fun toHexString(value: Long, digits: Int): String {
    return buildString {
        val hexValue     = java.lang.Long.toHexString(value)
        val leadingZeros = digits - hexValue.length
        append("0".repeat(0.coerceAtLeast(leadingZeros)))
        append(hexValue)
    }
}

fun toHexStringWithPrefix(value: Long): String {
    return "0x" + toHexString(value)
}

fun toSignedHexStringWithPrefix(value: Long): String {
    return if (value < 0) "-0x%x".format(-value) else "0x%x".format(value)
}

fun toSignedHexStringWithPrefix(value: Int): String {
    return if (value < 0) "-0x%x".format(-value) else "0x%x".format(value)
}

fun toSignedHexStringWithPrefix(value: Short): String {
    return if (value < 0) "-0x%x".format(-value) else "0x%x".format(value)
}

fun toSignedHexStringWithPrefix(value: Byte): String {
    return if (value < 0) "-0x%x".format(-value) else "0x%x".format(value)
}
