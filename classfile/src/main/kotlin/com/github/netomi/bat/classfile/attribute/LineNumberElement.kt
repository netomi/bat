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

package com.github.netomi.bat.classfile.attribute

import java.io.DataInput
import java.io.DataOutput

class LineNumberElement private constructor(startPC:    Int = -1,
                                            lineNumber: Int = -1) {

    var startPC = startPC
        private set

    var lineNumber = lineNumber
        private set

    private fun read(input: DataInput) {
        startPC    = input.readUnsignedShort()
        lineNumber = input.readUnsignedShort()
    }

    internal fun write(output: DataOutput) {
        output.writeShort(startPC)
        output.writeShort(lineNumber)
    }

    companion object {
        internal const val DATA_SIZE = 4

        internal fun read(input: DataInput): LineNumberElement {
            val element = LineNumberElement()
            element.read(input)
            return element
        }
    }
}