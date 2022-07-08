/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.netomi.bat.dexfile.util

import java.io.UTFDataFormatException

/**
 * Modified UTF-8 as described in the dex file format spec.
 *
 * Derived from libcore's MUTF-8 encoder at java.nio.charset.ModifiedUtf8.
 */
object Mutf8 {
    /**
     * Decodes bytes from `in` into `out` until a delimiter 0x00 is
     * encountered. Returns a new string containing the decoded characters.
     */
    @JvmStatic
    @Throws(RuntimeException::class)
    fun decode(`in`: ByteArray, utf16len: Int): String {
        val out = CharArray(utf16len)
        var i = 0
        var s = 0
        while (true) {
            val a = (`in`[i++].toInt() and 0xff).toChar()
            if (a.code == 0) {
                return String(out, 0, s)
            }
            out[s] = a
            if (a < '\u0080') {
                s++
            } else if (a.code and 0xe0 == 0xc0) {
                val b = `in`[i++].toInt() and 0xff
                if (b and 0xC0 != 0x80) {
                    throw RuntimeException(UTFDataFormatException("bad second byte"))
                }
                out[s++] = (a.code and 0x1F shl 6 or (b and 0x3F)).toChar()
            } else if (a.code and 0xf0 == 0xe0) {
                val b = `in`[i++].toInt() and 0xff
                val c = `in`[i++].toInt() and 0xff
                if (b and 0xC0 != 0x80 || c and 0xC0 != 0x80) {
                    throw RuntimeException(UTFDataFormatException("bad second or third byte"))
                }
                out[s++] = (a.code and 0x0F shl 12 or (b and 0x3F shl 6) or (c and 0x3F)).toChar()
            } else {
                throw RuntimeException(UTFDataFormatException("bad byte"))
            }
        }
    }

    /**
     * Returns the number of bytes the modified UTF8 representation of 's' would take.
     */
    @Throws(RuntimeException::class)
    private fun countBytes(s: String, shortLength: Boolean): Long {
        var result: Long = 0
        val length = s.length
        for (i in 0 until length) {
            val ch = s[i]
            if (ch.code != 0 && ch.code <= 127) { // U+0000 uses two bytes.
                ++result
            } else if (ch.code <= 2047) {
                result += 2
            } else {
                result += 3
            }
            if (shortLength && result > 65535) {
                throw RuntimeException(UTFDataFormatException("String more than 65535 UTF bytes long"))
            }
        }
        return result
    }

    /**
     * Encodes the modified UTF-8 bytes corresponding to `s` into `dst`, starting at `offset`.
     */
    private fun encode(dst: ByteArray, offset: Int, s: String) {
        var offset = offset
        val length = s.length
        for (i in 0 until length) {
            val ch = s[i]
            if (ch.code != 0 && ch.code <= 127) { // U+0000 uses two bytes.
                dst[offset++] = ch.code.toByte()
            } else if (ch.code <= 2047) {
                dst[offset++] = (0xc0 or (0x1f and (ch.code shr 6))).toByte()
                dst[offset++] = (0x80 or (0x3f and ch.code)).toByte()
            } else {
                dst[offset++] = (0xe0 or (0x0f and (ch.code shr 12))).toByte()
                dst[offset++] = (0x80 or (0x3f and (ch.code shr 6))).toByte()
                dst[offset++] = (0x80 or (0x3f and ch.code)).toByte()
            }
        }
    }

    /**
     * Returns an array containing the *modified UTF-8* form of `s`.
     */
    @JvmStatic
    fun encode(s: String): ByteArray {
        val utfCount = countBytes(s, true).toInt()
        val result   = ByteArray(utfCount)
        encode(result, 0, s)
        return result
    }
}