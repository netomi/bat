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
package com.github.netomi.bat.dexdump

import com.github.netomi.bat.dexfile.util.Mutf8
import com.github.netomi.bat.io.IndentingPrinter
import java.io.*

internal class Mutf8Printer constructor(private val outputStream: OutputStream)
    : IndentingPrinter(OutputStreamWriter(outputStream)) {

    fun printAsMutf8(text: String, escapeControlChars: Boolean) {
        var s = text
        try {
            if (escapeControlChars) {
                s = s.replace("\\\\".toRegex(), "\\\\\\\\")
                s = s.replace("\"".toRegex(), "\\\\\"")
                s = s.replace("\r".toRegex(), "\\\\r")
                s = s.replace("\n".toRegex(), "\\\\n")
                s = s.replace("\t".toRegex(), "\\\\t")
            }
            val arr = Mutf8.encode(s)
            super.flush()
            outputStream.write(arr, 0, arr.size)
        } catch (ex: IOException) {
            throw RuntimeException(ex)
        }
    }
}