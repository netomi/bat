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

package com.github.netomi.bat.classfile.editor

import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.Method
import com.github.netomi.bat.classfile.attribute.Attribute
import com.github.netomi.bat.classfile.attribute.AttributeMap
import com.github.netomi.bat.classfile.attribute.CodeAttribute
import com.github.netomi.bat.classfile.attribute.ExceptionEntry
import com.github.netomi.bat.classfile.constant.editor.ConstantPoolEditor
import com.github.netomi.bat.classfile.instruction.JvmInstruction
import com.github.netomi.bat.classfile.instruction.editor.InstructionWriter
import com.github.netomi.bat.classfile.instruction.editor.LabelInstruction

class CodeEditor private constructor(private val classEditor:   ClassEditor,
                                     private val method:        Method,
                                     private val codeAttribute: CodeAttribute): AttributeEditor() {

    val classFile: ClassFile
        get() = classEditor.classFile

    override val constantPoolEditor: ConstantPoolEditor
        get() = classEditor.constantPoolEditor

    override val attributeMap: AttributeMap
        get() = this.codeAttribute.attributeMap

    override fun addAttribute(attribute: Attribute) {
        codeAttribute.addAttribute(attribute)
    }

    private val modifications      = mutableMapOf<Int, CodeModifications>()
    private val addedExceptionList = mutableListOf<ExceptionEntry>()

    fun prependLabel(offset: Int, label: String) {
        val modifications = getModifications(offset)
        modifications.prependList.add(LabelInstruction.of(label))
    }

    fun appendLabel(offset: Int, label: String) {
        val modifications = getModifications(offset)
        modifications.appendList.add(LabelInstruction.of(label))
    }

    fun prependInstruction(offset: Int, instruction: JvmInstruction) {
        prependInstructions(offset, listOf(instruction))
    }

    fun prependInstructions(offset: Int, instructions: List<JvmInstruction>) {
        val modifications = getModifications(offset)
        modifications.prependList.addAll(instructions)
    }

    fun appendInstruction(offset: Int, instruction: JvmInstruction) {
        appendInstructions(offset, listOf(instruction))
    }

    fun appendInstructions(offset: Int, instructions: List<JvmInstruction>) {
        val modifications = getModifications(offset)
        modifications.appendList.addAll(instructions)
    }

    private fun getModifications(offset: Int): CodeModifications {
        return modifications.computeIfAbsent(offset) { CodeModifications() }
    }

    private fun getModificationsOrNull(offset: Int): CodeModifications? {
        return modifications[offset]
    }

    fun addExceptionEntry(exceptionEntry: ExceptionEntry) {
        addedExceptionList.add(exceptionEntry)
    }

    fun finishEditing() {
        if (modifications.isEmpty() && addedExceptionList.isEmpty()) {
            return
        }

        val instructions = collectInstructions()
        val assembledInstructions = InstructionWriter.writeInstructions(instructions)

        codeAttribute._code = assembledInstructions
    }

    private fun collectInstructions(): List<JvmInstruction> {
        val instructions = mutableListOf<JvmInstruction>()

        if (codeAttribute.codeLength == 0) {
            val modifications = getModificationsOrNull(0)
            val offset = modifications?.processPrependModifications(0, instructions) ?: 0
            modifications?.processAppendModifications(offset, instructions)
        } else {
//            var codeOffset = 0
//
//            code.instructionsAccept(dexFile, classDef, method, object: InstructionVisitor {
//                override fun visitAnyInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: DexInstruction) {
//                    val modifications = getModificationsOrNull(offset)
//
//                    offsetMap.setOldToNewOffsetMapping(offset, codeOffset)
//
//                    codeOffset += modifications?.processPrependModifications(instructions) ?: 0
//
//                    val (finishedWriting, length) = modifications?.processInstruction(instructions) ?: Pair(false, 0)
//                    codeOffset += length
//                    if (!finishedWriting) {
//                        instructions.add(instruction)
//                        codeOffset += instruction.length
//                    }
//
//                    codeOffset += modifications?.processAppendModifications(instructions) ?: 0
//                }
//
//                override fun visitAnyPayload(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, payload: Payload) {
//                    offsetMap.setOldToNewOffsetMapping(offset, codeOffset)
//                    codeOffset += payload.length
//                }
//            })
//
//            // add a mapping of the old size to the new size in case try / catch elements go
//            // till the end of the method.
//            offsetMap.setOldToNewOffsetMapping(code.insnsSize, codeOffset)
        }

        return instructions
    }

    companion object {
        fun of(classEditor: ClassEditor, method: Method, codeAttribute: CodeAttribute): CodeEditor {
            return CodeEditor(classEditor, method, codeAttribute)
        }
    }
}

private class CodeModifications {
    val prependList         = mutableListOf<JvmInstruction>()
    val appendList          = mutableListOf<JvmInstruction>()
    val replaceModification = mutableListOf<JvmInstruction>()
    val deleteModification: Boolean = false

    fun processPrependModifications(offset: Int, instructions: MutableList<JvmInstruction>): Int {
        var currOffset      = offset
        var prependedLength = 0

        for (instruction in prependList) {
            instructions.add(instruction)
            val length = instruction.getLength(currOffset)
            prependedLength += length
            currOffset      += length
        }
        return prependedLength
    }

    fun processAppendModifications(offset: Int, instructions: MutableList<JvmInstruction>): Int {
        var currOffset     = offset
        var appendedLength = 0

        for (instruction in appendList) {
            instructions.add(instruction)
            val length = instruction.getLength(currOffset)
            appendedLength += length
            currOffset     += length
        }
        return appendedLength
    }

    fun processInstruction(offset: Int, instructions: MutableList<JvmInstruction>): Pair<Boolean, Int> {
        return if (deleteModification) {
            Pair(true, 0)
        } else if (replaceModification.isNotEmpty()) {
            var currOffset        = offset
            var instructionLength = 0
            for (instruction in replaceModification) {
                instructions.add(instruction)
                val length = instruction.getLength(currOffset)
                instructionLength += length
                currOffset        += length
            }
            Pair(true, instructionLength)
        } else {
            Pair(false, 0)
        }
    }
}
