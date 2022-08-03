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
import com.github.netomi.bat.dexfile.instruction.*
import com.github.netomi.bat.dexfile.instruction.editor.InstructionBuilder
import com.github.netomi.bat.dexfile.instruction.visitor.InstructionPrinter
import com.github.netomi.bat.io.IndentingPrinter
import java.io.OutputStreamWriter
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals

class CodeEditorTest {

    private fun editor(): CodeEditor {
        val dexFile   = DexFile.of(DexFormat.FORMAT_035)
        val dexEditor = DexEditor.of(dexFile)

        val classDefEditor = dexEditor.addClassDef("LTestClass;", Visibility.PUBLIC)
        val methodEditor   = classDefEditor.addMethod("<init>", Visibility.PUBLIC, EnumSet.of(MethodModifier.CONSTRUCTOR))
        return methodEditor.addCode()
    }

    @Test
    fun simpleCompose() {
        val codeEditor = editor()
        val builder    = InstructionBuilder.of(codeEditor)

        builder.apply {
            invokeDirect("Ljava/lang/Object;", "<init>", emptyList(), "V", 0)
            const(6, 0)
            newArray("[I", 0, 0)
            fillArrayData(FillArrayPayload.of(intArrayOf(1, 2, 3, 4, 5, 6)), 0)
            returnVoid()
        }

        codeEditor.prependInstruction(0, builder.getInstructionSequence())
        codeEditor.finishEditing(1)

        val code = codeEditor.code
        assertEquals(28, code.insnsSize)

        // printInstructions(codeEditor)
    }

    @Test
    fun tryCatch() {
        val codeEditor = editor()
        val builder    = InstructionBuilder.of(codeEditor)

        builder.apply {
            invokeDirect("Ljava/lang/Object;", "<init>", emptyList(), "V", 0)
            nop()
            label("try_start")
            const(6, 0)
            newArray("[I", 0, 0)
            fillArrayData(FillArrayPayload.of(intArrayOf(1, 2, 3, 4, 5, 6)), 0)
            returnVoid()
            label("try_end")
            label("handler")
            returnVoid()
        }

        codeEditor.addTryCatchElement(Try.of("try_start", "try_end", EncodedCatchHandler.of("handler")))

        codeEditor.prependInstruction(0, builder.getInstructionSequence())
        codeEditor.finishEditing(1)

        // printInstructions(codeEditor)

        codeEditor.appendInstruction(0x9, builder.nop())
        codeEditor.prependInstruction(0xd, builder.nop())
        codeEditor.finishEditing(1)

        val code = codeEditor.code
        assertEquals(1, code.tryList.size)
        assertEquals(0x4, code.tryList.first().startAddr)
        assertEquals(0xd, code.tryList.first().endAddr)
        assertEquals(0xe, code.tryList.first().catchHandler.catchAllAddr)

        // printInstructions(codeEditor)
    }

    private fun printInstructions(codeEditor: CodeEditor) {
        val indentingPrinter = IndentingPrinter(OutputStreamWriter(System.out))
        val instructionPrinter = InstructionPrinter(indentingPrinter)

        codeEditor.acceptInstructions(instructionPrinter)
        println(codeEditor.code.tryList)
    }
}