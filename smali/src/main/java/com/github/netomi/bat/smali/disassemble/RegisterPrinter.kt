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
package com.github.netomi.bat.smali.disassemble

import com.github.netomi.bat.dexfile.Code
import com.github.netomi.bat.io.IndentingPrinter

internal class RegisterPrinter(code: Code) {
    private val localRegisters: Int = code.registersSize - code.insSize

    fun formatRegister(registerNum: Int): String {
        val registerPrefix = if (registerNum < localRegisters) "v" else "p"
        val registerIndex  = if (registerNum < localRegisters) registerNum else registerNum - localRegisters
        return registerPrefix + registerIndex
    }

    fun printRegister(printer: IndentingPrinter, registerNum: Int) {
        printer.print(formatRegister(registerNum))
    }
}