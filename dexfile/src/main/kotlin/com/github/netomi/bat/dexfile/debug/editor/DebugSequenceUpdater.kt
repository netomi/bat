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

import com.github.netomi.bat.dexfile.ClassDef
import com.github.netomi.bat.dexfile.Code
import com.github.netomi.bat.dexfile.DexFile
import com.github.netomi.bat.dexfile.EncodedMethod
import com.github.netomi.bat.dexfile.debug.*
import com.github.netomi.bat.dexfile.debug.visitor.DebugInfoVisitor
import com.github.netomi.bat.dexfile.debug.visitor.DebugSequenceVisitor
import com.github.netomi.bat.dexfile.editor.DexEditor
import com.github.netomi.bat.dexfile.instruction.editor.OffsetMap

internal class DebugSequenceUpdater(private val dexEditor: DexEditor,
                                    private val offsetMap: OffsetMap): DebugInfoVisitor, DebugSequenceVisitor {

    private lateinit var debugSequenceComposer: DebugSequenceComposer

    private var lineNumber = 0
    private var codeOffset: Int = 0

    override fun visitDebugInfo(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, debugInfo: DebugInfo) {
        debugSequenceComposer = DebugSequenceComposer.of(dexEditor, debugInfo)

        lineNumber = debugInfo.lineStart
        debugInfo.debugSequenceAccept(dexFile, this)
        debugSequenceComposer.finish()
    }

    override fun visitAnyDebugInstruction(dexFile: DexFile, debugInfo: DebugInfo, instruction: DebugInstruction) {}

    override fun visitSetPrologueEnd(dexFile: DexFile, debugInfo: DebugInfo, instruction: DebugSetPrologueEnd) {
        debugSequenceComposer.prologueEnd(offsetMap.getNewOffset(codeOffset))
    }

    override fun visitSetEpilogueBegin(dexFile: DexFile, debugInfo: DebugInfo, instruction: DebugSetEpilogueBegin) {
        debugSequenceComposer.epilogueStart(offsetMap.getNewOffset(codeOffset))
    }

    override fun visitAdvanceLine(dexFile: DexFile, debugInfo: DebugInfo, instruction: DebugAdvanceLine) {
        lineNumber += instruction.lineDiff
    }

    override fun visitAdvanceLineAndPC(dexFile: DexFile, debugInfo: DebugInfo, instruction: DebugAdvanceLineAndPC) {
        lineNumber += instruction.lineDiff
        codeOffset += instruction.addrDiff

        debugSequenceComposer.advanceLine(offsetMap.getNewOffset(codeOffset), lineNumber)
    }

    override fun visitAdvancePC(dexFile: DexFile, debugInfo: DebugInfo, instruction: DebugAdvancePC) {
        codeOffset += instruction.addrDiff
    }

    override fun visitEndLocal(dexFile: DexFile, debugInfo: DebugInfo, instruction: DebugEndLocal) {
        debugSequenceComposer.endLocal(offsetMap.getNewOffset(codeOffset), instruction.registerNum)
    }

    override fun visitRestartLocal(dexFile: DexFile, debugInfo: DebugInfo, instruction: DebugRestartLocal) {
        debugSequenceComposer.restartLocal(offsetMap.getNewOffset(codeOffset), instruction.registerNum)
    }

    override fun visitSetFile(dexFile: DexFile, debugInfo: DebugInfo, instruction: DebugSetFile) {
        debugSequenceComposer.setFile(offsetMap.getNewOffset(codeOffset), instruction.nameIndex)
    }

    override fun visitStartLocal(dexFile: DexFile, debugInfo: DebugInfo, instruction: DebugStartLocal) {
        debugSequenceComposer.startLocal(offsetMap.getNewOffset(codeOffset),
                                         instruction.registerNum,
                                         instruction.nameIndex,
                                         instruction.typeIndex)
    }

    override fun visitStartLocalExtended(dexFile: DexFile, debugInfo: DebugInfo, instruction: DebugStartLocalExtended) {
        debugSequenceComposer.startLocal(offsetMap.getNewOffset(codeOffset),
                                         instruction.registerNum,
                                         instruction.nameIndex,
                                         instruction.typeIndex,
                                         instruction.sigIndex)
    }

    override fun visitEndSequence(dexFile: DexFile, debugInfo: DebugInfo, instruction: DebugEndSequence) {}
}