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

package com.github.netomi.bat.dexfile.instruction.editor

import com.github.netomi.bat.dexfile.ClassDef
import com.github.netomi.bat.dexfile.Code
import com.github.netomi.bat.dexfile.DexFile
import com.github.netomi.bat.dexfile.EncodedMethod
import com.github.netomi.bat.dexfile.editor.CodeEditor
import com.github.netomi.bat.dexfile.instruction.*
import com.github.netomi.bat.dexfile.instruction.visitor.InstructionVisitor

class InstructionAdder constructor(private val targetCodeEditor: CodeEditor): InstructionVisitor {

    private val targetDexEditor = targetCodeEditor.dexEditor

    override fun visitAnyInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: DexInstruction) {
        targetCodeEditor.appendInstruction(0, instruction)
    }

    override fun visitStringInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: StringInstruction) {
        val stringIDIndex = targetDexEditor.addOrGetStringIDIndex(instruction.getString(dexFile))
        instruction.stringIndex = stringIDIndex
        visitAnyInstruction(dexFile, classDef, method, code, offset, instruction)
    }

    override fun visitAnyTypeInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: TypeInstruction) {
        instruction.typeIndex = targetDexEditor.addOrGetTypeIDIndex(instruction.getType(dexFile))
        visitAnyInstruction(dexFile, classDef, method, code, offset, instruction)
    }

    override fun visitFieldInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: FieldInstruction) {
        instruction.fieldIndex = targetDexEditor.addOrGetFieldIDIndex(dexFile, instruction.getField(dexFile))
        visitAnyInstruction(dexFile, classDef, method, code, offset, instruction)
    }

    override fun visitMethodInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: MethodInstruction) {
        instruction.methodIndex = targetDexEditor.addOrGetMethodIDIndex(dexFile, instruction.getMethodID(dexFile))
        visitAnyInstruction(dexFile, classDef, method, code, offset, instruction)
    }

    override fun visitMethodProtoInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: MethodProtoInstruction) {
        instruction.methodIndex = targetDexEditor.addOrGetMethodIDIndex(dexFile, instruction.getMethodID(dexFile))
        instruction.protoIndex  = targetDexEditor.addOrGetProtoIDIndex(dexFile, instruction.getProtoID(dexFile))
        visitAnyInstruction(dexFile, classDef, method, code, offset, instruction)
    }

    override fun visitMethodTypeRefInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: MethodTypeRefInstruction) {
        instruction.protoIndex  = targetDexEditor.addOrGetProtoIDIndex(dexFile, instruction.getProtoID(dexFile))
        visitAnyInstruction(dexFile, classDef, method, code, offset, instruction)
    }
}