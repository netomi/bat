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

fun ByteArray.toPrintableAsciiString(): String {
    return StringEscapers.printableAsciiEscaper.escape(String(this, Charsets.US_ASCII))
}

fun ByteArray.contentToHexString(): String = joinToString(separator = ",", prefix = "[", postfix = "]") { b -> "%02x".format(b) }

fun IntArray.compareTo(other: IntArray): Int {
    return LexicographicalComparator.compare(this, other)
}

/**
 * The code below (LexicographicalComparator) is copied from the guava project (Ints.java):
 *
 * Copyright (C) 2008 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
private object LexicographicalComparator : Comparator<IntArray> {
    override fun compare(left: IntArray, right: IntArray): Int {
        val minLength = left.size.coerceAtMost(right.size)
        for (i in 0 until minLength) {
            val result = left[i].compareTo(right[i])
            if (result != 0) {
                return result
            }
        }
        return left.size - right.size
    }

    override fun toString(): String {
        return "LexicographicalComparator"
    }
}
