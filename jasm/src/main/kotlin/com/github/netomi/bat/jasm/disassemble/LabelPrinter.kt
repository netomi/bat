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
import com.github.netomi.bat.classfile.attribute.ExceptionEntry
import com.github.netomi.bat.classfile.attribute.visitor.ExceptionVisitor
import com.github.netomi.bat.classfile.instruction.BranchInstruction
import com.github.netomi.bat.classfile.instruction.JvmInstruction
import com.github.netomi.bat.classfile.instruction.LookupSwitchInstruction
import com.github.netomi.bat.classfile.instruction.TableSwitchInstruction
import com.github.netomi.bat.classfile.instruction.visitor.InstructionVisitor
import com.github.netomi.bat.io.IndentingPrinter
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.LinkedHashSet

internal class LabelPrinter(private val printer: IndentingPrinter) : InstructionVisitor, ExceptionVisitor {

    private val startLabelInfos: MutableMap<Int, MutableSet<String>> = HashMap()
    private val endLabelInfos:   MutableMap<Int, MutableSet<String>> = HashMap()

    fun printStartLabels(offset: Int) {
        val labels = startLabelInfos[offset]
        labels?.forEach { printer.println(it) }
    }

    fun printEndLabels(offset: Int) {
        val labels = endLabelInfos[offset]
        labels?.forEach { printer.println(it) }
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

    // InstructionVisitor.

    override fun visitAnyInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: JvmInstruction) {}

    override fun visitBranchInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: BranchInstruction) {
        val target = offset + instruction.branchOffset
        addStartLabelInfo(target, formatBranchInstructionTarget(offset, instruction))
    }

    override fun visitLookupSwitchInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: LookupSwitchInstruction) {
        for (pair in instruction) {
            val target = offset + pair.offset
            addStartLabelInfo(target, formatLookupSwitchTarget(offset, pair.offset))
        }

        val defaultTarget = offset + instruction.defaultOffset
        addStartLabelInfo(defaultTarget, formatLookupSwitchTarget(offset, instruction.defaultOffset))
    }

    override fun visitTableSwitchInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: TableSwitchInstruction) {
        for (pair in instruction) {
            val target = offset + pair.offset
            addStartLabelInfo(target, formatTableSwitchTarget(offset, pair.offset))
        }

        val defaultTarget = offset + instruction.defaultOffset
        addStartLabelInfo(defaultTarget, formatTableSwitchTarget(offset, instruction.defaultOffset))
    }

    // ExceptionVisitor.

    override fun visitException(classFile: ClassFile, method: Method, code: CodeAttribute, exception: ExceptionEntry) {
        val startPC   = Integer.toHexString(exception.startPC)
        val endPC     = Integer.toHexString(exception.endPC)
        val handlerPC = Integer.toHexString(exception.handlerPC)

        addStartLabelInfo(exception.startPC, ":try_start_$startPC")

        if (exception.catchType == 0) {
            addStartLabelInfo(exception.handlerPC, ":catchall_$handlerPC")
        } else {
            addStartLabelInfo(exception.handlerPC, ":catch_$handlerPC")
        }

        addEndLabelInfo(exception.endPC, ":try_end_$endPC")

        if (exception.catchType == 0) {
            addEndLabelInfo(exception.endPC, ".catchall {:try_start_$startPC .. :try_end_${endPC}} :catchall_${handlerPC}")
        } else {
            addEndLabelInfo(exception.endPC, ".catch ${exception.getCaughtExceptionClassName(classFile)} {:try_start_$startPC .. :try_end_${endPC}} :catchall_${handlerPC}")
        }
    }

    private fun addStartLabelInfo(offset: Int, info: String) {
        val infos = startLabelInfos.computeIfAbsent(offset) { TreeSet() }
        infos.add(info)
    }

    private fun addEndLabelInfo(offset: Int, info: String) {
        val infos = endLabelInfos.computeIfAbsent(offset) { LinkedHashSet() }
        infos.add(info)
    }
}