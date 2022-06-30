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
package com.github.netomi.bat.io

import java.io.IOException
import java.io.Writer

class IndentingPrinter(private val delegateWriter: Writer, private val spacesPerLevel: Int = 4) : AutoCloseable {
    private var level = 0
    private var indentedLine = false

    fun levelUp() {
        level++
    }

    fun levelDown() {
        level--
    }

    fun print(text: CharSequence) {
        try {
            printIndentation()
            delegateWriter.write(text.toString())
        } catch (ioe: IOException) {
            throw RuntimeException(ioe)
        }
    }

    fun println(text: CharSequence) {
        try {
            printIndentation()
            delegateWriter.write(text.toString())
            println()
        } catch (ioe: IOException) {
            throw RuntimeException(ioe)
        }
    }

    fun println() {
        indentedLine = try {
            delegateWriter.write('\n'.code)
            false
        } catch (ioe: IOException) {
            throw RuntimeException(ioe)
        }
    }

    fun flush() {
        try {
            delegateWriter.flush()
        } catch (ex: IOException) {
            throw RuntimeException(ex)
        }
    }

    @Throws(Exception::class)
    override fun close() {
        delegateWriter.close()
    }

    @Throws(IOException::class)
    private fun printIndentation() {
        if (level > 0 && !indentedLine) {
            val spaces = level * spacesPerLevel
            delegateWriter.write(String.format("%" + spaces + "s", ""))
            indentedLine = true
        }
    }
}