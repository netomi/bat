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

import com.github.netomi.bat.dexfile.DexFile
import com.github.netomi.bat.dexfile.debug.*
import com.github.netomi.bat.dexfile.visitor.DebugSequenceVisitor

internal class SourceLineCollector(private val debugState: MutableMap<Int, MutableList<String>>, private var lineNumber: Int) : DebugSequenceVisitor {

    private var codeOffset: Int = 0

    override fun visitAnyDebugInstruction(dexFile: DexFile, debugInfo: DebugInfo, instruction: DebugInstruction) {}

    override fun visitSetPrologueEnd(dexFile: DexFile, debugInfo: DebugInfo, instruction: DebugSetPrologueEnd) {
        addDebugInfo(codeOffset.toInt(), ".prologue")
    }

    override fun visitAdvanceLine(dexFile: DexFile, debugInfo: DebugInfo, instruction: DebugAdvanceLine) {
        lineNumber += instruction.lineDiff
    }

    override fun visitAdvanceLineAndPC(dexFile: DexFile, debugInfo: DebugInfo, instruction: DebugAdvanceLineAndPC) {
        lineNumber += instruction.lineDiff
        codeOffset += instruction.addrDiff

        addDebugInfo(codeOffset, ".line $lineNumber")
    }

    override fun visitAdvancePC(dexFile: DexFile, debugInfo: DebugInfo, instruction: DebugAdvancePC) {
        codeOffset += instruction.addrDiff
    }

    private fun addDebugInfo(offset: Int, info: String) {
        val infos = debugState.computeIfAbsent(offset) { ArrayList() }
        infos.add(info)
    }
}