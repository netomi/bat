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

package com.github.netomi.bat.dexfile.debug.editor

import com.github.netomi.bat.dexfile.NO_INDEX
import com.github.netomi.bat.dexfile.debug.*
import com.github.netomi.bat.dexfile.editor.DexEditor

class DebugSequenceComposer private constructor(val dexEditor: DexEditor,
                                                val debugInfo: DebugInfo) {

    private var lineRegister = 0
    private var addrRegister = 0

    private var lineStart = 0

    private val debugSequence = mutableListOf<DebugInstruction>()

    fun reset() {
        lineRegister = 0
        addrRegister = 0
        lineStart    = 0

        debugSequence.clear()
    }

    fun prologueEnd(codeOffset: Int) {
        debugSequence.add(DebugSetPrologueEnd)
    }

    fun epilogueStart(codeOffset: Int) {
        debugSequence.add(DebugSetEpilogueBegin)
    }

    fun advanceLine(codeOffset: Int, lineNumber: Int) {
        if (lineRegister == 0) {
            lineStart = lineNumber

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

            if (lineDiff != 0) {
                val insn = DebugAdvanceLineAndPC.createIfPossible(lineDiff, addrDiff)
                if (insn != null) {
                    debugSequence.add(insn)
                } else {
                    val lineInsn = DebugAdvanceLineAndPC.createIfPossible(lineDiff, 0)
                    if (lineInsn != null) {
                        debugSequence.add(DebugAdvancePC.of(addrDiff))
                        debugSequence.add(lineInsn)
                    } else {
                        debugSequence.add(DebugAdvanceLine.of(lineDiff))
                        val addrInsn = DebugAdvanceLineAndPC.createIfPossible(0, addrDiff)
                        if (addrInsn != null) {
                            debugSequence.add(addrInsn)
                        } else {
                            debugSequence.add(DebugAdvancePC.of(addrDiff))
                            debugSequence.add(DebugAdvanceLineAndPC.nop())
                        }
                    }
                }
            }
        }

        lineRegister = lineNumber
        addrRegister = codeOffset
    }

    fun startLocal(codeOffset: Int, registerNum: Int, name: String?, type: String?, signature: String?) {
        val nameIndex = if (name != null) dexEditor.addOrGetStringIDIndex(name) else NO_INDEX
        val typeIndex = if (type != null) dexEditor.addOrGetTypeIDIndex(type) else NO_INDEX
        val sigIndex  = if (signature != null) dexEditor.addOrGetStringIDIndex(signature) else NO_INDEX

        startLocal(codeOffset, registerNum, nameIndex, typeIndex, sigIndex)
    }

    fun startLocal(codeOffset: Int, registerNum: Int, nameIndex: Int, typeIndex: Int, sigIndex: Int = NO_INDEX) {
        val addrDiff = codeOffset - addrRegister

        if (addrDiff > 0) {
            debugSequence.add(DebugAdvancePC.of(addrDiff))
        }

        if (sigIndex != NO_INDEX) {
            debugSequence.add(DebugStartLocalExtended.of(registerNum, nameIndex, typeIndex, sigIndex))
        } else {
            debugSequence.add(DebugStartLocal.of(registerNum, nameIndex, typeIndex))
        }

        addrRegister = codeOffset
    }

    fun restartLocal(codeOffset: Int, registerNum: Int) {
        val addrDiff = codeOffset - addrRegister

        if (addrDiff > 0) {
            debugSequence.add(DebugAdvancePC.of(addrDiff))
        }

        debugSequence.add(DebugRestartLocal.of(registerNum))
    }

    fun endLocal(codeOffset: Int, registerNum: Int) {
        val addrDiff = codeOffset - addrRegister

        if (addrDiff > 0) {
            debugSequence.add(DebugAdvancePC.of(addrDiff))
        }

        debugSequence.add(DebugEndLocal.of(registerNum))
    }

    fun setFile(codeOffset: Int, name: String) {
        val nameIndex = dexEditor.addOrGetStringIDIndex(name)
        setFile(codeOffset, nameIndex)
    }

    fun setFile(codeOffset: Int, nameIndex: Int) {
        debugSequence.add(DebugSetFile.of(nameIndex))
    }

    fun finish() {
        if (!debugInfo.isEmpty || debugSequence.isNotEmpty()) {
            debugSequence.add(DebugEndSequence)

            debugInfo.lineStart = lineStart
            debugInfo.debugSequence.clear()
            debugInfo.debugSequence.addAll(debugSequence)
        }
    }

    companion object {
        fun of(dexEditor: DexEditor, debugInfo: DebugInfo): DebugSequenceComposer {
            return DebugSequenceComposer(dexEditor, debugInfo)
        }
    }
}