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

open class IndentingPrinter(protected val delegateWriter: Writer, private val spacesPerLevel: Int = 4) : AutoCloseable {
    private var indentedLine      = false
    private val indentationStack  = ArrayDeque<Int>()

    private val currentIndentation
        get() = indentationStack.first()

    var currentPosition = 0
        private set

    init {
        indentationStack.addFirst(0)
    }

    fun levelUp() {
        val currentIndentation = indentationStack.first()
        indentationStack.addFirst(currentIndentation + spacesPerLevel)
    }

    fun levelDown() {
        indentationStack.removeFirst()
    }

    fun resetIndentation(indentation: Int) {
        indentationStack.addFirst(indentation)
    }

    fun print(text: CharSequence) {
        printIndentation()
        delegateWriter.write(text.toString())
        currentPosition += text.length
    }

    fun println(text: CharSequence) {
        printIndentation()
        delegateWriter.write(text.toString())
        println()
    }

    fun println() {
        currentPosition = 0

        delegateWriter.write('\n'.code)
        indentedLine = false
    }

    fun flush() {
        delegateWriter.flush()
    }

    @Throws(Exception::class)
    override fun close() {
        delegateWriter.close()
    }

    private fun printIndentation() {
        if (!indentedLine && currentIndentation > 0) {
            val spaces = currentIndentation
            delegateWriter.write(String.format("%" + spaces + "s", ""))

            indentedLine = true
            currentPosition += spaces
        }
    }
}