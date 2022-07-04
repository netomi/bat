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

package com.github.netomi.bat.dexdump

import com.github.netomi.bat.dexfile.DexFile
import com.github.netomi.bat.dexfile.debug.*
import com.github.netomi.bat.dexfile.visitor.DebugSequenceVisitor
import com.github.netomi.bat.util.Primitives

internal class SourceLinePrinter constructor(
    private var lineNumber: Int,
    private val printer:    Mutf8Printer) : DebugSequenceVisitor {

    private var codeOffset: Int = 0

    override fun visitAnyDebugInstruction(dexFile: DexFile, debugInfo: DebugInfo, instruction: DebugInstruction) {}

    override fun visitAdvanceLine(dexFile: DexFile, debugInfo: DebugInfo, instruction: DebugAdvanceLine) {
        lineNumber += instruction.lineDiff
    }

    override fun visitAdvanceLineAndPC(dexFile: DexFile, debugInfo: DebugInfo, instruction: DebugAdvanceLineAndPC) {
        lineNumber += instruction.lineDiff
        codeOffset += instruction.addrDiff
        printPosition()
    }

    override fun visitAdvancePC(dexFile: DexFile, debugInfo: DebugInfo, instruction: DebugAdvancePC) {
        codeOffset += instruction.addrDiff
    }

    private fun printPosition() {
        printer.println("        %s line=%d".format(Primitives.toHexString(codeOffset.toShort()), lineNumber))
    }
}
