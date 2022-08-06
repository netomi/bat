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

package com.github.netomi.bat.dexfile.editor

import com.github.netomi.bat.dexfile.*
import com.github.netomi.bat.dexfile.debug.editor.DebugInstructionAdder
import com.github.netomi.bat.dexfile.instruction.editor.InstructionAdder
import com.github.netomi.bat.dexfile.visitor.CodeVisitor

class CodeAdder constructor(private val targetMethodEditor: MethodEditor): CodeVisitor {

    private val dexEditor = targetMethodEditor.dexEditor

    override fun visitCode(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code) {
        val codeEditor = targetMethodEditor.addCode()

        code.instructionsAccept(dexFile, classDef, method, InstructionAdder(codeEditor))
        code.triesAccept(dexFile, classDef, method, TryAdder(codeEditor))

        // TODO: move this code to a separate DebugInfoAdder
        val oldDebugInfo = code.debugInfo
        val newDebugInfo = codeEditor.code.debugInfo
        newDebugInfo.lineStart = oldDebugInfo.lineStart

        for (index in 0 until oldDebugInfo.parameterCount) {
            val parameterName = oldDebugInfo.getParameterName(dexFile, index)
            if (parameterName != null) {
                newDebugInfo.setParameterName(index, dexEditor.addOrGetStringIDIndex(parameterName))
            } else {
                newDebugInfo.setParameterName(index, NO_INDEX)
            }
        }

        val debugInstructionAdder = DebugInstructionAdder(dexEditor)
        oldDebugInfo.debugSequenceAccept(dexFile, debugInstructionAdder)

        newDebugInfo.debugSequence.addAll(debugInstructionAdder.debugSequence)

        codeEditor.finishEditing(code.registersSize)
    }
}