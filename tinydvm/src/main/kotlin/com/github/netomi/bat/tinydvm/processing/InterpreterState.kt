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

package com.github.netomi.bat.tinydvm.processing

import com.github.netomi.bat.tinydvm.data.DvmValue

class InterpreterState private constructor(registersSize: Int) {

    val registers: Array<DvmValue?> = arrayOfNulls(registersSize)
    var invocationResult: DvmValue? = null

    companion object {
        fun of(registersSize: Int): InterpreterState {
            return InterpreterState(registersSize)
        }
    }
}