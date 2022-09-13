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

import java.io.Writer

open class IndentingPrinter(private val delegateWriter: Writer, private val spacesPerLevel: Int = 4) : Appendable, AutoCloseable {
    private var indentedLine       = false
    private val indentationStack   = ArrayDeque<Int>()
    private var currentIndentation = ""

    var currentPosition = 0
        private set

    init {
        indentationStack.addFirst(0)
        updateCurrentIndentation()
    }

    private fun updateCurrentIndentation() {
        currentIndentation = " ".repeat(indentationStack.first())
    }

    fun levelUp() {
        indentationStack.addFirst(indentationStack.first() + spacesPerLevel)
        updateCurrentIndentation()
    }

    fun levelDown() {
        indentationStack.removeFirst()
        updateCurrentIndentation()
    }

    fun resetIndentation(indentation: Int) {
        indentationStack.addFirst(indentation)
        updateCurrentIndentation()
    }

    fun print(obj: Any?) {
        print(java.lang.String.valueOf(obj))
    }

    fun print(text: String) {
        // if there are any newline character in the string,
        // split up the input into separate lines to make sure
        // indentation is correctly added.
        var newLinePos = text.indexOf('\n')
        if (newLinePos != -1) {
            var startPos = 0
            while (newLinePos != -1) {
                printInternal(text.substring(startPos, newLinePos))
                println()
                startPos = newLinePos + 1
                newLinePos = text.indexOf('\n', startPos)
            }
            val remainder = text.substring(startPos)
            if (remainder.isNotEmpty()) {
                printInternal(remainder)
            }
        } else {
            printInternal(text)
        }
    }

    private fun printInternal(text: String) {
        printIndentation()
        delegateWriter.write(text)
        currentPosition += text.length
    }

    fun padToPosition(position: Int) {
        val spacesToPad = (position - currentPosition).coerceAtLeast(0)
        val padding     = " ".repeat(spacesToPad)
        if (padding.isNotEmpty()) {
            delegateWriter.write(padding)
            currentPosition += padding.length
        }
    }

    fun println(obj: Any?) {
        println(java.lang.String.valueOf(obj))
    }

    fun println(text: CharSequence) {
        print(text.toString())
        println()
    }

    fun println() {
        currentPosition = 0

        delegateWriter.write('\n'.code)
        indentedLine = false
        flush()
    }

    override fun append(csq: CharSequence, start: Int, end: Int): java.lang.Appendable {
        print(csq.subSequence(start, end))
        return this
    }

    override fun append(csq: CharSequence): java.lang.Appendable {
        return append(csq, 0, csq.length)
    }

    override fun append(c: Char): java.lang.Appendable {
        print(c.toString())
        return this
    }

    fun flush() {
        delegateWriter.flush()
    }

    @Throws(Exception::class)
    override fun close() {
        delegateWriter.close()
    }

    private fun printIndentation() {
        if (!indentedLine && currentIndentation.isNotEmpty()) {
            delegateWriter.write(currentIndentation)
            currentPosition += currentIndentation.length
        }

        indentedLine = true
    }
}