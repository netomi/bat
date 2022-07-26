/*
 *  Copyright (c) 2020 Thomas Neidhart.
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
package com.github.netomi.bat.smali.disassemble

import com.github.netomi.bat.dexfile.ClassDef
import com.github.netomi.bat.dexfile.Code
import com.github.netomi.bat.dexfile.DexFile
import com.github.netomi.bat.dexfile.EncodedMethod
import com.github.netomi.bat.dexfile.instruction.*
import com.github.netomi.bat.dexfile.instruction.visitor.InstructionVisitor
import com.github.netomi.bat.io.IndentingPrinter
import java.util.*
import kotlin.collections.HashMap

internal class BranchTargetPrinter : InstructionVisitor {

    private val branchInfos:          MutableMap<Int, MutableSet<String>> = HashMap()
    private val reversePayloadLookup: MutableMap<Int, Int>                = HashMap()

    fun printLabels(offset: Int, printer: IndentingPrinter) {
        val labels = branchInfos[offset]
        labels?.forEach { printer.println(it) }
    }

    fun formatBranchInstructionTarget(offset: Int, instruction: BranchInstruction): String {
        val target = offset + instruction.branchOffset
        val mnemonic = instruction.opCode.mnemonic
        val prefix = if (mnemonic.startsWith("goto")) "goto" else "cond"
        return ":${prefix}_${Integer.toHexString(target)}"
    }

    fun formatPayloadInstructionTarget(offset: Int, instruction: PayloadInstruction): String {
        val prefix = when (instruction.opCode) {
            DexOpCode.FILL_ARRAY_DATA -> "array"
            DexOpCode.PACKED_SWITCH   -> "pswitch_data"
            DexOpCode.SPARSE_SWITCH   -> "sswitch_data"

            else -> throw RuntimeException("unexpected payload instruction $instruction")
        }

        val target = offset + instruction.payloadOffset
        return ":${prefix}_${Integer.toHexString(target)}"
    }

    fun formatPackedSwitchTarget(payloadOffset: Int, branchTarget: Int): String {
        val switchOffset = reversePayloadLookup[payloadOffset]!!
        val target = switchOffset + branchTarget
        return ":pswitch_" + Integer.toHexString(target)
    }

    fun formatSparseSwitchTarget(payloadOffset: Int, branchTarget: Int): String {
        val switchOffset = reversePayloadLookup[payloadOffset]!!
        val target = switchOffset + branchTarget
        return ":sswitch_" + Integer.toHexString(target)
    }

    override fun visitAnyInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: DexInstruction) {}

    override fun visitBranchInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: BranchInstruction) {
        val target = offset + instruction.branchOffset
        addBranchInfo(target, formatBranchInstructionTarget(offset, instruction))
    }

    override fun visitAnyPayloadInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: PayloadInstruction) {
        val target = offset + instruction.payloadOffset
        reversePayloadLookup[target] = offset
        addBranchInfo(target, formatPayloadInstructionTarget(offset, instruction))
    }

    override fun visitPackedSwitchPayload(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, payload: PackedSwitchPayload) {
        for (branchTarget in payload.branchTargets) {
            val switchOffset = reversePayloadLookup[offset]!!
            val target = switchOffset + branchTarget
            addBranchInfo(target, formatPackedSwitchTarget(offset, branchTarget))
        }
    }

    override fun visitSparseSwitchPayload(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, payload: SparseSwitchPayload) {
        for (branchTarget in payload.branchTargets) {
            val switchOffset = reversePayloadLookup[offset]!!
            val target = switchOffset + branchTarget
            addBranchInfo(target, formatSparseSwitchTarget(offset, branchTarget))
        }
    }

    private fun addBranchInfo(offset: Int, info: String) {
        val infos = branchInfos.computeIfAbsent(offset) { TreeSet() }
        infos.add(info)
    }
}