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

import com.github.netomi.bat.dexfile.DexFile
import com.github.netomi.bat.dexfile.NO_INDEX
import com.github.netomi.bat.dexfile.debug.*
import com.github.netomi.bat.dexfile.debug.visitor.DebugSequenceVisitor
import com.github.netomi.bat.dexfile.editor.DexEditor

class DebugInstructionAdder constructor(private val dexEditor: DexEditor): DebugSequenceVisitor {

    val debugSequence = mutableListOf<DebugInstruction>()

    private fun addDebugInstruction(instruction: DebugInstruction) {
        debugSequence.add(instruction)
    }

    override fun visitAnyDebugInstruction(dexFile: DexFile, debugInfo: DebugInfo, instruction: DebugInstruction) {}

    override fun visitAdvanceLine(dexFile: DexFile, debugInfo: DebugInfo, instruction: DebugAdvanceLine) {
        addDebugInstruction(DebugAdvanceLine.of(instruction.lineDiff))
    }

    override fun visitAdvanceLineAndPC(dexFile: DexFile, debugInfo: DebugInfo, instruction: DebugAdvanceLineAndPC) {
        addDebugInstruction(DebugAdvanceLineAndPC.of(instruction.opcode))
    }

    override fun visitAdvancePC(dexFile: DexFile, debugInfo: DebugInfo, instruction: DebugAdvancePC) {
        addDebugInstruction(DebugAdvancePC.of(instruction.addrDiff))
    }

    override fun visitEndLocal(dexFile: DexFile, debugInfo: DebugInfo, instruction: DebugEndLocal) {
        addDebugInstruction(DebugEndLocal.of(instruction.registerNum))
    }

    override fun visitEndSequence(dexFile: DexFile, debugInfo: DebugInfo, instruction: DebugEndSequence) {
        addDebugInstruction(DebugEndSequence)
    }

    override fun visitRestartLocal(dexFile: DexFile, debugInfo: DebugInfo, instruction: DebugRestartLocal) {
        addDebugInstruction(DebugRestartLocal.of(instruction.registerNum))
    }

    override fun visitSetEpilogueBegin(dexFile: DexFile, debugInfo: DebugInfo, instruction: DebugSetEpilogueBegin) {
        addDebugInstruction(DebugSetEpilogueBegin)
    }

    override fun visitSetPrologueEnd(dexFile: DexFile, debugInfo: DebugInfo, instruction: DebugSetPrologueEnd) {
        addDebugInstruction(DebugSetPrologueEnd)
    }

    override fun visitSetFile(dexFile: DexFile, debugInfo: DebugInfo, instruction: DebugSetFile) {
        val nameIndex = if (instruction.nameIndex != NO_INDEX) {
            dexEditor.addOrGetStringIDIndex(instruction.getName(dexFile)!!)
        } else {
            NO_INDEX
        }

        addDebugInstruction(DebugSetFile.of(nameIndex))
    }

    override fun visitStartLocal(dexFile: DexFile, debugInfo: DebugInfo, instruction: DebugStartLocal) {
        val nameIndex = if (instruction.nameIndex != NO_INDEX) {
            dexEditor.addOrGetStringIDIndex(instruction.getName(dexFile)!!)
        } else {
            NO_INDEX
        }

        val typeIndex = if (instruction.typeIndex != NO_INDEX) {
            dexEditor.addOrGetTypeIDIndex(instruction.getType(dexFile)!!)
        } else {
            NO_INDEX
        }

        addDebugInstruction(DebugStartLocal.of(instruction.registerNum, nameIndex, typeIndex))
    }

    override fun visitStartLocalExtended(dexFile: DexFile, debugInfo: DebugInfo, instruction: DebugStartLocalExtended) {
        val nameIndex = if (instruction.nameIndex != NO_INDEX) {
            dexEditor.addOrGetStringIDIndex(instruction.getName(dexFile)!!)
        } else {
            NO_INDEX
        }

        val typeIndex = if (instruction.typeIndex != NO_INDEX) {
            dexEditor.addOrGetTypeIDIndex(instruction.getType(dexFile)!!)
        } else {
            NO_INDEX
        }

        val sigIndex = if (instruction.sigIndex != NO_INDEX) {
            dexEditor.addOrGetStringIDIndex(instruction.getSignature(dexFile)!!)
        } else {
            NO_INDEX
        }

        addDebugInstruction(DebugStartLocalExtended.of(instruction.registerNum, nameIndex, typeIndex, sigIndex))
    }
}