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
import com.github.netomi.bat.dexfile.debug.editor.DebugInfoAdder
import com.github.netomi.bat.dexfile.instruction.editor.InstructionAdder
import com.github.netomi.bat.dexfile.visitor.CodeVisitor

internal class CodeAdder constructor(private val targetMethodEditor: MethodEditor): CodeVisitor {

    override fun visitCode(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code) {
        val codeEditor = targetMethodEditor.addCode()

        code.instructionsAccept(dexFile, classDef, method, InstructionAdder(codeEditor))
        code.triesAccept(dexFile, classDef, method, TryAdder(codeEditor))
        code.debugInfoAccept(dexFile, classDef, method, DebugInfoAdder(codeEditor))

        codeEditor.finishEditing(code.registersSize)
    }
}