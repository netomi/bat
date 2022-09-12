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

import com.github.netomi.bat.dexfile.*
import com.github.netomi.bat.dexfile.debug.DebugInfo
import com.github.netomi.bat.dexfile.debug.visitor.DebugInfoVisitor
import com.github.netomi.bat.dexfile.editor.CodeEditor

internal class DebugInfoAdder constructor(private val codeEditor: CodeEditor): DebugInfoVisitor {

    private val dexEditor = codeEditor.dexEditor

    override fun visitDebugInfo(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, debugInfo: DebugInfo) {
        val newDebugInfo = codeEditor.code.debugInfo
        newDebugInfo.lineStart = debugInfo.lineStart

        for (index in 0 until debugInfo.parameterCount) {
            val parameterName = debugInfo.getParameterName(dexFile, index)
            val nameIndex = if (parameterName != null) dexEditor.addOrGetStringIDIndex(parameterName) else NO_INDEX
            newDebugInfo.setParameterName(index, nameIndex)
        }

        debugInfo.debugSequenceAccept(dexFile, DebugSequenceAdder(dexEditor, newDebugInfo))
    }
}