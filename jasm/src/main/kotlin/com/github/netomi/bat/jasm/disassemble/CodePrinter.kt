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
import com.github.netomi.bat.classfile.attribute.Attribute
import com.github.netomi.bat.classfile.attribute.CodeAttribute
import com.github.netomi.bat.classfile.attribute.LineNumberTableAttribute
import com.github.netomi.bat.classfile.attribute.visitor.CodeAttributeVisitor
import com.github.netomi.bat.classfile.attribute.visitor.MethodAttributeVisitor
import com.github.netomi.bat.io.IndentingPrinter

internal class CodePrinter constructor(private val printer:         IndentingPrinter,
                                       private val constantPrinter: ConstantPrinter): MethodAttributeVisitor, CodeAttributeVisitor {

    private var debugState: MutableMap<Int, MutableList<String>> = mutableMapOf()

    override fun visitAnyAttribute(classFile: ClassFile, attribute: Attribute) {}

    override fun visitCode(classFile: ClassFile, method: Method, attribute: CodeAttribute) {
        debugState.clear()

        printer.println(".stack ${attribute.maxStack}")
        printer.println(".locals ${attribute.maxLocals}")

        // fill debug info
        attribute.attributesAccept(classFile, method, this)

        // collect branch target / label infos.
        val branchTargetPrinter = BranchTargetPrinter(printer)
        attribute.instructionsAccept(classFile, method, branchTargetPrinter)

        // print instructions and debug info
        attribute.instructionsAccept(classFile, method, InstructionPrinter(printer, constantPrinter, branchTargetPrinter, debugState))
    }

    // CodeAttributeVisitor.

    override fun visitLineNumberTable(classFile: ClassFile, method: Method, code: CodeAttribute, attribute: LineNumberTableAttribute) {
        for (entry in attribute) {
            addLineDebugInfo(entry.startPC, entry.lineNumber)
        }
    }

    private fun addLineDebugInfo(offset: Int, lineNumber: Int) {
        addDebugInfo(offset, ".line $lineNumber")
    }

    private fun addDebugInfo(offset: Int, info: String) {
        val infos = debugState.computeIfAbsent(offset) { ArrayList() }
        infos.add(info)
    }
}