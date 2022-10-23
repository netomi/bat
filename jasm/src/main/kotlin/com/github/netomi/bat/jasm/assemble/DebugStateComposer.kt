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

package com.github.netomi.bat.jasm.assemble

import com.github.netomi.bat.classfile.attribute.*
import com.github.netomi.bat.classfile.constant.editor.ConstantPoolEditor
import com.github.netomi.bat.classfile.editor.CodeEditor

internal class DebugStateComposer constructor(private val codeEditor: CodeEditor) {

    private val constantPoolEditor: ConstantPoolEditor
        get() = codeEditor.constantPoolEditor

    private val localVariableInfos: MutableMap<Int, MutableList<LocalVariableInfo>> = mutableMapOf()
    private val lineNumberInfos:    MutableList<Pair<Int, Int>> = mutableListOf()

    fun addLineNumber(offset: Int, lineNumber: Int) {
        lineNumberInfos.add(Pair(offset, lineNumber))
    }

    fun startLocalVariable(startPC: Int, variable: Int, name: String, descriptor: String?, signature: String?) {
        addLocalVariable(startPC, variable, name, descriptor, signature)
    }

    fun endLocalVariable(endPC: Int, variable: Int) {
        val infosForVariable = localVariableInfos[variable]
        val localVariableInfo = infosForVariable?.lastOrNull()

        if (localVariableInfo != null) {
            localVariableInfo.endPC = endPC
        } else {
            error("no corresponding start local for variable $variable")
        }
    }

    fun finish(codeOffset: Int) {
        // implicitly close any non-ended local variable infos.
        for ((_, infos) in localVariableInfos) {
            infos.filter { it.endPC == -1 }.forEach { it.endPC = codeOffset }
        }

        // create the LineNumberTable attribute.
        if (lineNumberInfos.isNotEmpty()) {
            val lineNumberAttribute = codeEditor.addOrGetAttribute<LineNumberTableAttribute>(AttributeType.LINE_NUMBER_TABLE)

            // sort by offset
            lineNumberInfos.sortBy { it.first }

            for ((offset, lineNumber) in lineNumberInfos) {
                lineNumberAttribute.add(LineNumberEntry.of(offset, lineNumber))
            }
        }

        if (localVariableInfos.isNotEmpty()) {
            val localVariableEntries:     MutableList<LocalVariableEntry>     = mutableListOf()
            val localVariableTypeEntries: MutableList<LocalVariableTypeEntry> = mutableListOf()

            for ((_, infos) in localVariableInfos) {
                for (info in infos) {
                    if (info.descriptor != null) {
                        info.apply {
                            localVariableEntries.add(
                                LocalVariableEntry.of(startPC,
                                                      endPC - startPC,
                                                      constantPoolEditor.addOrGetUtf8ConstantIndex(name),
                                                      constantPoolEditor.addOrGetUtf8ConstantIndex(descriptor!!),
                                                      variable))
                        }
                    }

                    if (info.signature != null) {
                        info.apply {
                            localVariableTypeEntries.add(
                                LocalVariableTypeEntry.of(startPC,
                                                          endPC - startPC,
                                                          constantPoolEditor.addOrGetUtf8ConstantIndex(name),
                                                          constantPoolEditor.addOrGetUtf8ConstantIndex(signature!!),
                                                          variable))
                        }
                    }
                }
            }

            if (localVariableEntries.isNotEmpty()) {
                val localVariableTableAttribute = codeEditor.addOrGetAttribute<LocalVariableTableAttribute>(AttributeType.LOCAL_VARIABLE_TABLE)

                localVariableEntries.sortWith(compareBy({ it.variableIndex }, { it.startPC }))

                for (entry in localVariableEntries) {
                    localVariableTableAttribute.add(entry)
                }
            }

            if (localVariableTypeEntries.isNotEmpty()) {
                val localVariableTypeTableAttribute = codeEditor.addOrGetAttribute<LocalVariableTypeTableAttribute>(AttributeType.LOCAL_VARIABLE_TYPE_TABLE)

                localVariableTypeEntries.sortWith(compareBy({ it.variableIndex }, { it.startPC }))

                for (entry in localVariableTypeEntries) {
                    localVariableTypeTableAttribute.add(entry)
                }
            }
        }
    }

    private fun addLocalVariable(startPC: Int, variable: Int, name: String, descriptor: String?, signature: String?) {
        val infos = localVariableInfos.computeIfAbsent(variable) { ArrayList() }

        var localVariableInfo = infos.firstOrNull { it.startPC == startPC && it.name == name }
        if (localVariableInfo == null) {
            localVariableInfo = LocalVariableInfo(startPC, -1, variable, name)
            infos.add(localVariableInfo)
        }

        localVariableInfo.updateDescriptor(descriptor)
        localVariableInfo.updateSignature(signature)
    }

}

internal data class LocalVariableInfo(val startPC:    Int,
                                      var endPC:      Int,
                                      val variable:   Int,
                                      val name:       String,
                                      var descriptor: String? = null,
                                      var signature:  String? = null) {
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