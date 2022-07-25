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
package com.github.netomi.bat.dexfile.debug.visitor

import com.github.netomi.bat.dexfile.DexFile
import com.github.netomi.bat.dexfile.debug.*

fun interface DebugSequenceVisitor {
    fun visitAnyDebugInstruction(dexFile: DexFile, debugInfo: DebugInfo, instruction: DebugInstruction)

    fun visitAdvanceLine(dexFile: DexFile, debugInfo: DebugInfo, instruction: DebugAdvanceLine) {
        visitAnyDebugInstruction(dexFile, debugInfo, instruction)
    }

    fun visitAdvanceLineAndPC(dexFile: DexFile, debugInfo: DebugInfo, instruction: DebugAdvanceLineAndPC) {
        visitAnyDebugInstruction(dexFile, debugInfo, instruction)
    }

    fun visitAdvancePC(dexFile: DexFile, debugInfo: DebugInfo, instruction: DebugAdvancePC) {
        visitAnyDebugInstruction(dexFile, debugInfo, instruction)
    }

    fun visitEndLocal(dexFile: DexFile, debugInfo: DebugInfo, instruction: DebugEndLocal) {
        visitAnyDebugInstruction(dexFile, debugInfo, instruction)
    }

    fun visitEndSequence(dexFile: DexFile, debugInfo: DebugInfo, instruction: DebugEndSequence) {
        visitAnyDebugInstruction(dexFile, debugInfo, instruction)
    }

    fun visitRestartLocal(dexFile: DexFile, debugInfo: DebugInfo, instruction: DebugRestartLocal) {
        visitAnyDebugInstruction(dexFile, debugInfo, instruction)
    }

    fun visitSetEpilogueBegin(dexFile: DexFile, debugInfo: DebugInfo, instruction: DebugSetEpilogueBegin) {
        visitAnyDebugInstruction(dexFile, debugInfo, instruction)
    }

    fun visitSetFile(dexFile: DexFile, debugInfo: DebugInfo, instruction: DebugSetFile) {
        visitAnyDebugInstruction(dexFile, debugInfo, instruction)
    }

    fun visitSetPrologueEnd(dexFile: DexFile, debugInfo: DebugInfo, instruction: DebugSetPrologueEnd) {
        visitAnyDebugInstruction(dexFile, debugInfo, instruction)
    }

    fun visitStartLocal(dexFile: DexFile, debugInfo: DebugInfo, instruction: DebugStartLocal) {
        visitAnyDebugInstruction(dexFile, debugInfo, instruction)
    }

    fun visitStartLocalExtended(dexFile: DexFile, debugInfo: DebugInfo, instruction: DebugStartLocalExtended) {
        visitAnyDebugInstruction(dexFile, debugInfo, instruction)
    }
}