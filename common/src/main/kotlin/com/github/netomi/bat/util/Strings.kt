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

import com.google.common.escape.ArrayBasedCharEscaper
import com.google.common.escape.ArrayBasedEscaperMap
import com.google.common.escape.Escaper

internal object StringEscapers {

    private val ASCII_CTRL_CHARS_ESCAPE =
        mapOf(
            Pair('\b',     "\\b"),
            Pair('\n',     "\\n"),
            Pair('\t',     "\\t"),
            Pair('\u000c', "\\f"),
            Pair('\r',     "\\r"),
            Pair('\u0000', "\\0")
        )

    private val JAVA_CTRL_CHARS_ESCAPE =
        mapOf(
            Pair('\'',     "\\'"),
            Pair('\"',     "\\\""),
            Pair('\\',     "\\\\"),
            Pair('\n',     "\\n"),
            Pair('\r',     "\\r"),
            Pair('\t',     "\\t"),
            Pair('\u000c', "\\u000c"),
        )

    val printableAsciiEscaper: Escaper
    val javaEscaper: Escaper

    private fun escapeUnicode(cp: Int): CharArray {
        return buildString {
            append("\\u")
            append(Character.forDigit(cp shr 12, 16))
            append(Character.forDigit(cp shr 8 and 0x0f, 16))
            append(Character.forDigit(cp shr 4 and 0x0f, 16))
            append(Character.forDigit(cp and 0x0f, 16))
        }.toCharArray()
    }

    init {
        printableAsciiEscaper = object: ArrayBasedCharEscaper(ArrayBasedEscaperMap.create(ASCII_CTRL_CHARS_ESCAPE), 0x20.toChar(), 0x7e.toChar()) {
            override fun escapeUnsafe(c: Char): CharArray {
                return escapeUnicode(c.code)
            }
        }

        javaEscaper = object: ArrayBasedCharEscaper(ArrayBasedEscaperMap.create(JAVA_CTRL_CHARS_ESCAPE), 0x20.toChar(), 0x7e.toChar()) {
            override fun escapeUnsafe(c: Char): CharArray {
                return escapeUnicode(c.code)
            }
        }
    }
}

fun String.isAsciiPrintable(): Boolean {
    for (c in this.toCharArray()) {
        val codePoint = c.code
        if (codePoint < 0x20 || codePoint >= 0x7f) return false
    }
    return true
}

fun String.escapeAsJavaString(): String {
    return StringEscapers.javaEscaper.escape(this)
}

fun Char.escapeAsJavaString(): String {
    return this.toString().escapeAsJavaString()
}

/**
 * Unescapes a string that contains standard Java escape sequences.
 *
 *  * **&#92;b &#92;f &#92;n &#92;r &#92;t &#92;" &#92;'** :
 * BS, FF, NL, CR, TAB, double and single quote.
 *  * **&#92;X &#92;XX &#92;XXX** : Octal character
 * specification (0 - 377, 0x00 - 0xFF).
 *  * **&#92;uXXXX** : Hexadecimal based Unicode character.
 *
 * @return The translated string.
 */
fun String.unescapeJavaString(): String {
    val input = this
    return buildString(length) {
        var i = 0
        while (i < input.length) {
            var ch = input[i]
            if (ch == '\\') {
                val nextChar = if (i == input.lastIndex) '\\' else input[i + 1]
                // Octal escape?
                if (nextChar in '0'..'7') {
                    var code = nextChar.toString()
                    i++
                    if (i < input.length - 1 && input[i + 1] in '0' ..'7') {
                        code += input[i + 1]
                        i++
                        if (i < input.length - 1 && input[i + 1] in '0' .. '7') {
                            code += input[i + 1]
                            i++
                        }
                    }
                    append(code.toInt(8).toChar())
                    i++
                    continue
                }

                when (nextChar) {
                    '\\' -> ch = '\\'
                    'b'  -> ch = '\b'
                    'f'  -> ch = '\u000c'
                    'n'  -> ch = '\n'
                    'r'  -> ch = '\r'
                    't'  -> ch = '\t'
                    '\"' -> ch = '\"'
                    '\'' -> ch = '\''
                    'u'  -> {
                        if (i >= input.length - 5) {
                            ch = 'u'
                        } else {
                            val code = input.substring(i + 2, i + 6).toInt(16)
                            append(Character.toChars(code))
                            i += 6
                            continue
                        }
                    }
                }
                i++
            }
            append(ch)
            i++
        }
    }
}
