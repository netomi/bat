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
import com.github.netomi.bat.dexfile.instruction.editor.LabelInstruction
import com.github.netomi.bat.dexfile.instruction.editor.OffsetMap
import com.github.netomi.bat.util.deepCopy
import com.google.common.base.Preconditions

class CodeEditor private constructor(        val dexEditor: DexEditor,
                                             val classDef:  ClassDef,
                                             val method:    EncodedMethod,
                                             val code:      Code) {

    private val dexFile: DexFile
        get() = dexEditor.dexFile

    private val modifications = mutableMapOf<Int, CodeModifications>()
    private val tryCatchList  = mutableListOf<Try>()

    fun prependLabel(offset: Int, label: String) {
        val modifications = getModifications(offset)
        modifications.prependList.add(LabelInstruction.of(label))
    }

    fun appendLabel(offset: Int, label: String) {
        val modifications = getModifications(offset)
        modifications.appendList.add(LabelInstruction.of(label))
    }

    fun prependInstruction(offset: Int, instruction: DexInstruction) {
        prependInstruction(offset, listOf(instruction))
    }

    fun prependInstruction(offset: Int, instructions: List<DexInstruction>) {
        instructions.forEach {
            Preconditions.checkArgument(dexFile.supportsOpcode(it.opCode),
                "instruction '$it' not supported by DexFile of format '${dexFile.dexFormat}'")
        }

        val modifications = getModifications(offset)
        modifications.prependList.addAll(instructions)
    }

    fun appendInstruction(offset: Int, instruction: DexInstruction) {
        appendInstruction(offset, listOf(instruction))
    }

    fun appendInstruction(offset: Int, instructions: List<DexInstruction>) {
        instructions.forEach {
            Preconditions.checkArgument(dexFile.supportsOpcode(it.opCode),
                "instruction '$it' not supported by DexFile of format '${dexFile.dexFormat}'")
        }

        val modifications = getModifications(offset)
        modifications.appendList.addAll(instructions)
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

        val writeInstructionsAndPayloads = { instructions: List<DexInstruction>, payloads: List<Payload> ->
            for (instruction in instructions) {
                instruction.write(instructionWriter, instructionWriter.nextWriteOffset, offsetMap)
            }

            for (payload in payloads) {
                if (instructionWriter.nextWriteOffset.mod(2) == 1) {
                    val nop = BasicInstruction.of(DexOpCode.NOP)
                    nop.write(instructionWriter, instructionWriter.nextWriteOffset, offsetMap)
                }

                payload.write(instructionWriter, instructionWriter.nextWriteOffset, offsetMap)
            }
        }

        val (instructions, payloads) = collectInstructionsAndPayloads(offsetMap)

        // in the first iteration, collect label offsets and write instructions
        // with potentially wrong offsets, which will be corrected in a second pass.
        writeInstructionsAndPayloads(instructions, payloads)

        // fail when encountering missing labels / offsets as they should be present now.
        offsetMap.failOnMissingKey = true

        // update the offsets of each try/catch element based on the collected labels.
        // TODO: handling existing try/catch elements as well
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
        if (offsetMap.hasUpdates()) {
            instructionWriter.reset()

            // write all instructions / payloads again with fixed offsets.
            writeInstructionsAndPayloads(instructions, payloads)
        }

        code.insns = instructionWriter.getInstructionArray()

        // TODO: also handle existing try/catch elements when editing existing code
        //       atm we only support composing new code.
        if (tryCatchList.isNotEmpty()) {
            code.tryList = normalizeTries(tryCatchList)
        }

        code.registersSize = registersSize
        updateCodeSizeData()

        // clear modifications
        modifications.clear()
        tryCatchList.clear()
    }

    internal fun acceptInstructions(visitor: InstructionVisitor) {
        code.instructionsAccept(dexFile, classDef, method, visitor)
    }

    private fun collectInstructionsAndPayloads(offsetMap: OffsetMap): Pair<List<DexInstruction>, List<Payload>> {
        val instructions = mutableListOf<DexInstruction>()
        val payloads     = mutableListOf<Payload>()

        if (code.insnsSize == 0) {
            val modifications = getModificationsOrNull(0)
            modifications?.processPrependModifications(instructions)
            modifications?.processAppendModifications(instructions)
        } else {
            var codeOffset = 0

            code.instructionsAccept(dexFile, classDef, method, object: InstructionVisitor {
                override fun visitAnyInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: DexInstruction) {
                    val modifications = getModificationsOrNull(offset)

                    codeOffset += modifications?.processPrependModifications(instructions) ?: 0

                    val (finishedWriting, length) = modifications?.processInstruction(instructions) ?: Pair(false, 0)
                    codeOffset += length
                    if (!finishedWriting) {
                        offsetMap.setOldToNewOffsetMapping(offset, codeOffset)
                        instructions.add(instruction)
                        codeOffset += instruction.length
                    }

                    codeOffset += modifications?.processAppendModifications(instructions) ?: 0
                }

                // ignore payloads
                override fun visitAnyPayload(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, payload: Payload) {}
            })
        }

        // remove NOP instructions at the end, they are padding instructions that will be recreated when needed.
        while (instructions.lastOrNull()?.opCode == DexOpCode.NOP) {
            instructions.removeLast()
        }

        instructions.filterIsInstance<PayloadInstruction<*>>().forEach { instruction -> payloads.add(instruction.payload) }
        return Pair(instructions, payloads)
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

private class CodeModifications {
    val prependList         = mutableListOf<DexInstruction>()
    val appendList          = mutableListOf<DexInstruction>()
    val replaceModification = mutableListOf<DexInstruction>()
    val deleteModification: Boolean = false

    fun processPrependModifications(instructions: MutableList<DexInstruction>): Int {
        var instructionLength = 0
        for (instruction in prependList) {
            instructions.add(instruction)
            instructionLength += instruction.length
        }
        return instructionLength
    }

    fun processAppendModifications(instructions: MutableList<DexInstruction>): Int {
        var instructionLength = 0
        for (instruction in appendList) {
            instructions.add(instruction)
            instructionLength += instruction.length
        }
        return instructionLength
    }

    fun processInstruction(instructions: MutableList<DexInstruction>): Pair<Boolean, Int> {
        return if (deleteModification) {
            Pair(true, 0)
        } else if (replaceModification.isNotEmpty()) {
            var instructionLength = 0
            for (instruction in replaceModification) {
                instructions.add(instruction)
                instructionLength += instruction.length
            }
            Pair(true, instructionLength)
        } else {
            Pair(false, 0)
        }
    }
}

private class CodeSizeCalculator: InstructionVisitor {

    var outgoingArgumentSize: Int = 0
        private set

    override fun visitAnyInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: DexInstruction) {}

    override fun visitAnyMethodInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: MethodInstruction) {
        val argumentSize = instruction.registers.size
        outgoingArgumentSize = maxOf(outgoingArgumentSize, argumentSize)
    }

    override fun visitCallSiteInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: CallSiteInstruction) {
        val argumentSize = instruction.registers.size
        outgoingArgumentSize = maxOf(outgoingArgumentSize, argumentSize)
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
            flattenedElements.add(Try.of(lastTry.startAddr, lastTry.endAddr, lastTry.catchHandler.add(tryElement.catchHandler.copy())))
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
                    val endingTry = Try.of(currentTry.startAddr, seq.addr - 1, currentTry.catchHandler.copy())
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
                            val endingTry = Try.of(currentTry.startAddr, seq.addr - 1, currentTry.catchHandler.copy())
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
    return EncodedCatchHandler.of(newCatchAllAddr, newHandlers.toList().deepCopy())
}

private fun EncodedCatchHandler.add(other: EncodedCatchHandler): EncodedCatchHandler {
    var newCatchAllAddr = catchAllAddr
    if (newCatchAllAddr == NO_INDEX) {
        newCatchAllAddr = other.catchAllAddr
    }

    val newHandlers = handlers + other.handlers
    return EncodedCatchHandler.of(newCatchAllAddr, newHandlers.deepCopy())
}
