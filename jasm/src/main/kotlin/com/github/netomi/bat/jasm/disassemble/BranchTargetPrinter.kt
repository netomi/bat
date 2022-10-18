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
package com.github.netomi.bat.jasm.disassemble

import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.Method
import com.github.netomi.bat.classfile.attribute.CodeAttribute
import com.github.netomi.bat.classfile.instruction.BranchInstruction
import com.github.netomi.bat.classfile.instruction.JvmInstruction
import com.github.netomi.bat.classfile.instruction.LookupSwitchInstruction
import com.github.netomi.bat.classfile.instruction.TableSwitchInstruction
import com.github.netomi.bat.classfile.instruction.visitor.InstructionVisitor
import com.github.netomi.bat.io.IndentingPrinter
import java.util.*
import kotlin.collections.HashMap

internal class BranchTargetPrinter(private val printer: IndentingPrinter) : InstructionVisitor {

    private val branchInfos: MutableMap<Int, MutableSet<String>> = HashMap()

    fun printLabels(offset: Int) {
        val labels = branchInfos[offset]

        // combine branch and try/catch labels and print them in a sorted manner.
        val sortedLabels = TreeSet<String>()
        if (labels != null) {
            sortedLabels.addAll(labels)
        }

        sortedLabels.forEach { printer.println(it) }
    }

    fun formatBranchInstructionTarget(offset: Int, instruction: BranchInstruction): String {
        val target = offset + instruction.branchOffset
        val mnemonic = instruction.opCode.mnemonic
        val prefix = if (mnemonic.startsWith("goto")) "goto" else "cond"
        return ":${prefix}_${Integer.toHexString(target)}"
    }

    fun formatTableSwitchTarget(offset: Int, branchTarget: Int): String {
        val target = offset + branchTarget
        return ":tswitch_" + Integer.toHexString(target)
    }

    fun formatLookupSwitchTarget(offset: Int, branchTarget: Int): String {
        val target = offset + branchTarget
        return ":lswitch_" + Integer.toHexString(target)
    }

    override fun visitAnyInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: JvmInstruction) {}

    override fun visitBranchInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: BranchInstruction) {
        val target = offset + instruction.branchOffset
        addBranchInfo(target, formatBranchInstructionTarget(offset, instruction))
    }

    override fun visitLookupSwitchInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: LookupSwitchInstruction) {
        for (pair in instruction) {
            val target = offset + pair.offset
            addBranchInfo(target, formatLookupSwitchTarget(offset, pair.offset))
        }

        val defaultTarget = offset + instruction.defaultOffset
        addBranchInfo(defaultTarget, formatLookupSwitchTarget(offset, instruction.defaultOffset))
    }

    override fun visitTableSwitchInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: TableSwitchInstruction) {
        for (pair in instruction) {
            val target = offset + pair.offset
            addBranchInfo(target, formatTableSwitchTarget(offset, pair.offset))
        }

        val defaultTarget = offset + instruction.defaultOffset
        addBranchInfo(defaultTarget, formatTableSwitchTarget(offset, instruction.defaultOffset))
    }

    private fun addBranchInfo(offset: Int, info: String) {
        val infos = branchInfos.computeIfAbsent(offset) { TreeSet() }
        infos.add(info)
    }
}