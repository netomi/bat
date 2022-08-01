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
import com.github.netomi.bat.dexfile.instruction.visitor.InstructionVisitor
import com.github.netomi.bat.dexfile.instruction.editor.InstructionWriter
import com.github.netomi.bat.dexfile.instruction.editor.OffsetMap
import com.google.common.base.Preconditions

class CodeEditor private constructor(        val dexEditor: DexEditor,
                                     private val classDef:  ClassDef,
                                     private val method:    EncodedMethod,
                                             val code:      Code) {

    private val dexFile: DexFile
        get() = dexEditor.dexFile

    private val modifications = mutableMapOf<Int, CodeModifications>()
    private val tryCatchList  = mutableListOf<Try>()

    fun prependLabel(offset: Int, label: String) {
        val modifications = getModifications(offset)
        modifications.prependList.add(CodeModification.of(label))
    }

    fun appendLabel(offset: Int, label: String) {
        val modifications = getModifications(offset)
        modifications.appendList.add(CodeModification.of(label))
    }

    fun prependInstruction(offset: Int, instruction: DexInstruction) {
        Preconditions.checkArgument(dexFile.supportsOpcode(instruction.opCode), "instruction '$instruction' not supported by DexFile of format '${dexFile.dexFormat}'")

        val modifications = getModifications(offset)
        modifications.prependList.add(CodeModification.of(instruction))
    }

    fun appendInstruction(offset: Int, instruction: DexInstruction) {
        val modifications = getModifications(offset)
        modifications.appendList.add(CodeModification.of(instruction))
    }

    private fun getModifications(offset: Int): CodeModifications {
        return modifications.computeIfAbsent(offset) { CodeModifications() }
    }

    private fun getModificationsOrNull(offset: Int): CodeModifications? {
        return modifications[offset]
    }

    fun finishEditing(registersSize: Int) {
        if (modifications.isEmpty() && tryCatchList.isEmpty()) {
            return
        }

        val instructionWriter = InstructionWriter()
        val offsetMap         = OffsetMap(false)

        val writeInstructions = {
            if (code.insnsSize == 0) {
                val modifications = getModificationsOrNull(0)
                modifications?.processPrependModifications(instructionWriter, offsetMap)
                modifications?.processAppendModifications(instructionWriter, offsetMap)
            } else {
                code.instructionsAccept(dexFile, classDef, method) { _, _, _, _, offset, instruction ->
                    val modifications = getModificationsOrNull(offset)

                    modifications?.processPrependModifications(instructionWriter, offsetMap)

                    val finishedWriting = modifications?.processInstruction(instructionWriter, offsetMap) ?: false
                    if (!finishedWriting) {
                        val newOffset = writeInstruction(instruction, instructionWriter, instructionWriter.nextWriteOffset, offsetMap)
                        offsetMap.setOldToNewOffsetMapping(offset, newOffset)
                    }

                    modifications?.processAppendModifications(instructionWriter, offsetMap)
                }
            }
        }

        // in the first iteration, collect label offsets and write label instructions
        // with wrong offsets.
        writeInstructions()

        // fail when encountering missing labels in the second pass.
        offsetMap.failOnMissingKey = true

        // update the offsets of each try/catch element based on the collected labels.
        for (tryElement in tryCatchList) {
            tryElement.startAddr = offsetMap.getOffset(tryElement.startLabel!!)
            val endAddr          = offsetMap.getOffset(tryElement.endLabel!!)
            tryElement.insnCount = endAddr - tryElement.startAddr

            if (tryElement.catchHandler.catchAllLabel != null) {
                tryElement.catchHandler.catchAllAddr = offsetMap.getOffset(tryElement.catchHandler.catchAllLabel!!)
            }

            for (addrPair in tryElement.catchHandler.handlers) {
                if (addrPair.label != null) {
                    addrPair.address = offsetMap.getOffset(addrPair.label!!)
                }
            }
        }

        // if we encountered any labels, we need to fix instructions that reference them.
        if (offsetMap.hasLabelsOrOffsetUpdates()) {
            instructionWriter.reset()

            // write all instructions again with now fixed offsets.
            writeInstructions()
        }

        code.insns = instructionWriter.getInstructionArray()

        code.tryList = normalizeTries(tryCatchList)

        code.registersSize = registersSize
        updateCodeSizeData()

        // clear modifications
        modifications.clear()
        tryCatchList.clear()
    }

    fun setParameterName(parameterIndex: Int, name: String?) {
        val nameIndex = if (name != null) dexEditor.addOrGetStringIDIndex(name) else NO_INDEX
        code.debugInfo.setParameterName(parameterIndex, nameIndex)
    }

    fun addTryCatchElement(tryCatchElement: Try) {
        tryCatchList.add(tryCatchElement)
    }

    private fun updateCodeSizeData() {
        val codeSizeCalculator = CodeSizeCalculator()
        code.instructionsAccept(dexFile, classDef, method, codeSizeCalculator)

        code.outsSize = codeSizeCalculator.outgoingArgumentSize
    }

    companion object {
        fun of(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code): CodeEditor {
            return CodeEditor(DexEditor.of(dexFile), classDef, method, code)
        }

        fun of(dexEditor: DexEditor, classDef: ClassDef, method: EncodedMethod, code: Code): CodeEditor {
            return CodeEditor(dexEditor, classDef, method, code)
        }
    }
}

private fun writeInstruction(instruction: DexInstruction,
                             writer:      InstructionWriter,
                             offset:      Int,
                             offsetMap:   OffsetMap): Int {
    if (instruction is Payload && offset.mod(2) == 1) {
        val nop = BasicInstruction.of(DexOpCode.NOP)
        nop.write(writer, offset, offsetMap)
    }

    val newOffset = writer.nextWriteOffset
    instruction.write(writer, writer.nextWriteOffset, offsetMap)
    return newOffset
}

private class CodeModifications {
    val prependList         = mutableListOf<CodeModification>()
    val appendList          = mutableListOf<CodeModification>()
    val replaceModification = mutableListOf<CodeModification>()
    val deleteModification: Boolean = false

    fun processPrependModifications(instructionWriter: InstructionWriter, labelOffsetMap: OffsetMap) {
        for (modification in prependList) {
            modification.processModification(instructionWriter, labelOffsetMap)
        }
    }

    fun processAppendModifications(instructionWriter: InstructionWriter, labelOffsetMap: OffsetMap) {
        for (modification in appendList) {
            modification.processModification(instructionWriter, labelOffsetMap)
        }
    }

    fun processInstruction(instructionWriter: InstructionWriter, offsetMap: OffsetMap): Boolean {
        return if (deleteModification) {
            true
        } else if (replaceModification.isNotEmpty()) {
            for (modification in replaceModification) {
                modification.processModification(instructionWriter, offsetMap)
            }
            true
        } else {
            false
        }
    }
}

private class CodeModification private constructor(val label: String? = null, val instruction: DexInstruction? = null) {
    fun processModification(instructionWriter: InstructionWriter, offsetMap: OffsetMap) {
        if (label != null) {
            offsetMap.setLabel(label, instructionWriter.nextWriteOffset)
        }

        if (instruction != null) {
            writeInstruction(instruction, instructionWriter, instructionWriter.nextWriteOffset, offsetMap)
        }
    }

    companion object {
        fun of(instruction: DexInstruction): CodeModification {
            return CodeModification(instruction = instruction)
        }

        fun of(label: String): CodeModification {
            return CodeModification(label = label)
        }
    }
}

private class CodeSizeCalculator: InstructionVisitor {

    var outgoingArgumentSize: Int = 0
        private set

    override fun visitAnyInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: DexInstruction) {}

    override fun visitMethodInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: MethodInstruction) {
        val argumentSize = instruction.registers.size
        outgoingArgumentSize = maxOf(outgoingArgumentSize, argumentSize)

        visitAnyInstruction(dexFile, classDef, method, code, offset, instruction)
    }

    override fun visitMethodProtoInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: MethodProtoInstruction) {
        val argumentSize = instruction.registers.size
        outgoingArgumentSize = maxOf(outgoingArgumentSize, argumentSize)

        visitAnyInstruction(dexFile, classDef, method, code, offset, instruction)
    }
}

private data class Seq(val addr: Int, val type: SeqType, val tryElement: Try)

private enum class SeqType {
    START,
    END
}

private fun normalizeTries(tryElements: MutableList<Try>): ArrayList<Try> {
    if (tryElements.isEmpty()) {
        return arrayListOf()
    }

    tryElements.sortBy { it.startAddr }

    val flattenedElements = mutableListOf<Try>()
    var lastTry: Try? = null
    for (tryElement in tryElements) {
        if (lastTry != null &&
            lastTry.startAddr == tryElement.startAddr &&
            lastTry.endAddr   == tryElement.endAddr) {
            flattenedElements.removeLast()
            flattenedElements.add(Try.of(lastTry.startAddr, lastTry.endAddr, lastTry.catchHandler.add(tryElement.catchHandler)))
        } else {
            flattenedElements.add(tryElement)
        }
        lastTry = tryElement
    }

    val sequence = mutableListOf<Seq>()
    flattenedElements.forEach {
        sequence.add(Seq(it.startAddr, SeqType.START, it))
        sequence.add(Seq(it.endAddr, SeqType.END, it))
    }

    sequence.sortWith(compareBy<Seq>{ it.addr }.thenBy { it.type })

    val nonOverlappingTries = arrayListOf<Try>()
    var currentTry: Try? = null

    for (seq in sequence) {
        when (seq.type) {
            SeqType.START -> {
                currentTry = if (currentTry == null) {
                    seq.tryElement
                } else {
                    val endingTry = Try.of(currentTry.startAddr, seq.addr - 1, currentTry.catchHandler)
                    nonOverlappingTries.add(endingTry)

                    val handler = seq.tryElement.catchHandler.add(currentTry.catchHandler)
                    val startingTry = Try.of(seq.addr, currentTry.endAddr, handler)
                    startingTry
                }
            }
            SeqType.END -> {
                if (currentTry != null) {
                    currentTry = if (currentTry.endAddr == seq.addr) {
                        nonOverlappingTries.add(currentTry)
                        null
                    } else {
                        if (currentTry.startAddr < seq.addr - 1) {
                            val endingTry = Try.of(currentTry.startAddr, seq.addr - 1, currentTry.catchHandler)
                            nonOverlappingTries.add(endingTry)
                        }

                        val handler = currentTry.catchHandler.subtract(seq.tryElement.catchHandler)
                        val startingTry = Try.of(seq.addr, currentTry.endAddr, handler)
                        startingTry
                    }
                } else {
                    if (nonOverlappingTries.last().endAddr != seq.addr) {
                        throw RuntimeException("not expected")
                    }
                }
            }
        }
    }

    return nonOverlappingTries
}

private fun EncodedCatchHandler.subtract(other: EncodedCatchHandler): EncodedCatchHandler {
    var newCatchAllAddr = catchAllAddr
    if (other.catchAllAddr != NO_INDEX) {
        newCatchAllAddr = NO_INDEX
    }

    val newHandlers = LinkedHashSet(handlers) - other.handlers.toSet()
    return EncodedCatchHandler.of(newCatchAllAddr, newHandlers.toList())
}

private fun EncodedCatchHandler.add(other: EncodedCatchHandler): EncodedCatchHandler {
    var newCatchAllAddr = catchAllAddr
    if (newCatchAllAddr == NO_INDEX) {
        newCatchAllAddr = other.catchAllAddr
    }

    val newHandlers = handlers + other.handlers
    return EncodedCatchHandler.of(newCatchAllAddr, newHandlers)
}
