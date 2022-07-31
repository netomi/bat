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

fun String.isAsciiPrintable(): Boolean {
    for (c in this.toCharArray()) {
        val codePoint = c.code
        if (codePoint < 32 || codePoint >= 128) return false
    }
    return true
}

fun String.escapeAsJavaString(): String {
    val input = this
    return buildString(input.length * 3 / 2) {
        for (i in input.indices) {
            append(input[i].escapeAsJavaString())
        }
    }
}

fun Char.escapeAsJavaString(): String {
    val c = this
    if (c.code < 0x7f) {
        when (c) {
            '\'' -> return "\\'"
            '\"' -> return "\\\""
            '\\' -> return "\\\\"
            '\n' -> return "\\n"
            '\r' -> return "\\r"
            '\t' -> return "\\t"
        }
    }

    if ((c >= ' ') && (c.code < 0x7f)) {
        return c.toString()
    }

    return buildString {
        append("\\u")
        append(Character.forDigit(c.code shr 12, 16))
        append(Character.forDigit(c.code shr 8 and 0x0f, 16))
        append(Character.forDigit(c.code shr 4 and 0x0f, 16))
        append(Character.forDigit(c.code and 0x0f, 16))
    }
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
                            i += 5
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
