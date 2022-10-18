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

package com.github.netomi.bat.jasm.disassemble

import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.Method
import com.github.netomi.bat.classfile.attribute.*
import com.github.netomi.bat.classfile.attribute.visitor.CodeAttributeVisitor
import com.github.netomi.bat.classfile.attribute.visitor.MethodAttributeVisitor
import com.github.netomi.bat.io.IndentingPrinter

internal class CodePrinter constructor(private val printer:         IndentingPrinter,
                                       private val constantPrinter: ConstantPrinter): MethodAttributeVisitor, CodeAttributeVisitor {

    private var debugState: MutableMap<Int, MutableList<String>> = mutableMapOf()
    private var localVariableInfos: MutableMap<Int, MutableList<LocalVariableInfo>> = mutableMapOf()

    override fun visitAnyAttribute(classFile: ClassFile, attribute: Attribute) {}

    override fun visitCode(classFile: ClassFile, method: Method, attribute: CodeAttribute) {
        debugState.clear()
        localVariableInfos.clear()

        printer.println(".stack ${attribute.maxStack}")
        printer.println(".locals ${attribute.maxLocals}")

        // fill debug info
        attribute.attributesAccept(classFile, method, this)
        for ((_, infos) in localVariableInfos) {
            for (localVariableInfo in infos) {
                addStartLocalInfo(localVariableInfo)
                addEndLocalInfo(localVariableInfo)
            }
        }

        // collect branch target / label infos.
        val branchTargetPrinter = BranchTargetPrinter(printer)
        attribute.instructionsAccept(classFile, method, branchTargetPrinter)

        // print instructions and debug info
        attribute.instructionsAccept(classFile, method, InstructionPrinter(printer, constantPrinter, branchTargetPrinter, debugState))
    }

    // CodeAttributeVisitor.

    override fun visitLocalVariableTable(classFile: ClassFile, method: Method, code: CodeAttribute, attribute: LocalVariableTableAttribute) {
        for (entry in attribute) {
            addLocalVariable(entry.startPC, entry.length, entry.variableIndex, entry.getName(classFile), entry.getDescriptor(classFile), null)
        }
    }

    override fun visitLocalVariableTypeTable(classFile: ClassFile, method: Method, code: CodeAttribute, attribute: LocalVariableTypeTableAttribute) {
        for (entry in attribute) {
            addLocalVariable(entry.startPC, entry.length, entry.variableIndex, entry.getName(classFile), null, entry.getSignature(classFile))
        }
    }

    private fun addLocalVariable(startPC: Int, length: Int, registerNum: Int, name: String, descriptor: String?, signature: String?) {
        val infos = localVariableInfos.computeIfAbsent(startPC) { ArrayList() }

        var localVariableInfo = infos.firstOrNull { it.startPC == startPC && it.name == name }
        if (localVariableInfo == null) {
            localVariableInfo = LocalVariableInfo(startPC, length, registerNum, name)
            infos.add(localVariableInfo)
        }

        localVariableInfo.updateDescriptor(descriptor)
        localVariableInfo.updateSignature(signature)
    }

    override fun visitLineNumberTable(classFile: ClassFile, method: Method, code: CodeAttribute, attribute: LineNumberTableAttribute) {
        for (entry in attribute) {
            addLineDebugInfo(entry.startPC, entry.lineNumber)
        }
    }

    private fun addLineDebugInfo(offset: Int, lineNumber: Int) {
        addDebugInfo(offset, ".line $lineNumber")
    }

    private fun addStartLocalInfo(localVariableInfo: LocalVariableInfo) {
        val info = buildString {
            append(".local ")
            append(localVariableInfo.variableIndex)
            append(", ")

            append("\"")
            append(localVariableInfo.name)
            append("\"")

            if (localVariableInfo.descriptor != null) {
                append(":")
                append(localVariableInfo.descriptor)
            }

            if (localVariableInfo.signature != null) {
                append(", \"")
                append(localVariableInfo.signature)
                append("\"")
            }
        }

        addDebugInfo(localVariableInfo.startPC, info)
    }

    private fun addEndLocalInfo(localVariableInfo: LocalVariableInfo) {
        val info = buildString {
            append(".end local ")
            append(localVariableInfo.variableIndex)
            append(" # ")

            append("\"")
            append(localVariableInfo.name)
            append("\"")

            if (localVariableInfo.descriptor != null) {
                append(":")
                append(localVariableInfo.descriptor)
            }

            if (localVariableInfo.signature != null) {
                append(", \"")
                append(localVariableInfo.signature)
                append("\"")
            }
        }

        addDebugInfo(localVariableInfo.startPC + localVariableInfo.length, info)
    }

    private fun addDebugInfo(offset: Int, info: String) {
        val infos = debugState.computeIfAbsent(offset) { ArrayList() }
        infos.add(info)
    }
}

internal data class LocalVariableInfo
    constructor(val startPC:       Int,
                val length:        Int,
                val variableIndex: Int,
                val name:          String,
                var descriptor:    String? = null,
                var signature:     String? = null) {

    fun updateDescriptor(descriptor: String?) {
        if (descriptor != null) {
            this.descriptor = descriptor
        }
    }

    fun updateSignature(signature: String?) {
        if (signature != null) {
            this.signature = signature
        }
    }
}
