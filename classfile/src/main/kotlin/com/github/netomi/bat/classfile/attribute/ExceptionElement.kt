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

class ExceptionElement private constructor(startPC:   Int = -1,
                                           endPC:     Int = -1,
                                           handlerPC: Int = -1,
                                           catchType: Int = -1) {

    var startPC: Int = startPC
        private set

    var endPC: Int = endPC
        private set

    var handlerPC: Int = handlerPC
        private set

    var catchType: Int = catchType
        private set

    internal fun read(input: DataInput) {
        startPC   = input.readUnsignedShort()
        endPC     = input.readUnsignedShort()
        handlerPC = input.readUnsignedShort()
        catchType = input.readUnsignedShort()
    }

    companion object {
        internal fun read(input: DataInput): ExceptionElement {
            val entry = ExceptionElement()
            entry.read(input)
            return entry
        }
    }
}