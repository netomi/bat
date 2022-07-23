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

package com.github.netomi.bat.smali.assemble

import com.github.netomi.bat.dexfile.debug.*

internal class DebugSequenceAssembler internal constructor(private val debugInfo: DebugInfo) {

    private var lineRegister = 0
    private var addrRegister = 0

    private val debugSequence: MutableList<DebugInstruction>
        get() = debugInfo.debugSequence

    fun prologueEnd(codeOffset: Int) {
        debugSequence.add(DebugSetPrologueEnd)
    }

    fun epilogueStart(codeOffset: Int) {
        debugSequence.add(DebugSetEpilogueBegin)
    }

    fun advanceLine(lineNumber: Int, codeOffset: Int) {
        if (lineRegister == 0) {
            debugInfo.lineStart = lineNumber

            if (codeOffset == 0) {
                debugSequence.add(DebugAdvanceLineAndPC.nop())
            } else {
                val insn = DebugAdvanceLineAndPC.createIfPossible(0, codeOffset)
                if (insn != null) {
                    debugSequence.add(insn)
                } else {
                    debugSequence.add(DebugAdvancePC.of(codeOffset))
                    debugSequence.add(DebugAdvanceLineAndPC.nop())
                }
            }
        } else {
            val lineDiff = lineNumber - lineRegister
            val addrDiff = codeOffset - addrRegister

            val insn = DebugAdvanceLineAndPC.createIfPossible(lineDiff, addrDiff)
            if (insn != null) {
                debugSequence.add(insn)
            } else {
                debugSequence.add(DebugAdvancePC.of(addrDiff))
                val lineInsn = DebugAdvanceLineAndPC.createIfPossible(lineDiff, 0)
                if (lineInsn != null) {
                    debugSequence.add(lineInsn)
                } else {
                    debugSequence.add(DebugAdvanceLine.of(lineDiff))
                }
            }
        }

        lineRegister = lineNumber
        addrRegister = codeOffset
    }
}