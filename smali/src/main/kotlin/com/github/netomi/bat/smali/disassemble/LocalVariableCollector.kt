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

import com.github.netomi.bat.dexfile.DexFile
import com.github.netomi.bat.dexfile.NO_INDEX
import com.github.netomi.bat.dexfile.debug.*
import com.github.netomi.bat.dexfile.debug.visitor.DebugSequenceVisitor

internal class LocalVariableCollector(private val debugState:         MutableMap<Int, MutableList<String>>,
                                      private val localVariableInfos: Array<LocalVariableInfo?>,
                                      private val registerPrinter:    RegisterPrinter) : DebugSequenceVisitor {

    private var codeOffset: Int = 0

    override fun visitAnyDebugInstruction(dexFile: DexFile, debugInfo: DebugInfo, instruction: DebugInstruction) {}

    override fun visitAdvanceLineAndPC(dexFile: DexFile, debugInfo: DebugInfo, instruction: DebugAdvanceLineAndPC) {
        codeOffset += instruction.addrDiff
    }

    override fun visitAdvancePC(dexFile: DexFile, debugInfo: DebugInfo, instruction: DebugAdvancePC) {
        codeOffset += instruction.addrDiff
    }

    override fun visitEndLocal(dexFile: DexFile, debugInfo: DebugInfo, instruction: DebugEndLocal) {
        val info = buildString {
            val registerNum = instruction.registerNum
            append(".end local ")
            append(registerPrinter.formatRegister(registerNum))
            handleGenericLocal(registerNum, this)
        }
        addDebugInfo(codeOffset, info)
    }

    override fun visitRestartLocal(dexFile: DexFile, debugInfo: DebugInfo, instruction: DebugRestartLocal) {
        val info = buildString {
            val registerNum = instruction.registerNum
            append(".restart local ")
            append(registerPrinter.formatRegister(registerNum))
            handleGenericLocal(registerNum, this)
        }
        addDebugInfo(codeOffset, info)
    }

    override fun visitStartLocal(dexFile: DexFile, debugInfo: DebugInfo, instruction: DebugStartLocal) {
        handleStartLocal(dexFile, instruction.registerNum, instruction.nameIndex, instruction.typeIndex, NO_INDEX)
    }

    override fun visitStartLocalExtended(dexFile: DexFile, debugInfo: DebugInfo, instruction: DebugStartLocalExtended) {
        handleStartLocal(dexFile, instruction.registerNum, instruction.nameIndex, instruction.typeIndex, instruction.sigIndex)
    }

    private fun handleStartLocal(dexFile: DexFile, registerNum: Int, nameIndex: Int, typeIndex: Int, sigIndex: Int) {
        localVariableInfos[registerNum] = LocalVariableInfo(
            dexFile.getStringNullable(nameIndex),
            dexFile.getTypeOrNull(typeIndex)?.type,
            dexFile.getStringNullable(sigIndex)
        )

        val info = buildString {
            append(".local ")
            append(registerPrinter.formatRegister(registerNum))
            append(", ")

            append("\"")
            if (nameIndex != NO_INDEX) {
                append(dexFile.getString(nameIndex))
            }
            append("\"")

            if (typeIndex != NO_INDEX) {
                append(":")
                append(dexFile.getType(typeIndex))
            }

            if (sigIndex != NO_INDEX) {
                append(", \"")
                append(dexFile.getString(sigIndex))
                append("\"")
            }
        }
        addDebugInfo(codeOffset, info)
    }

    private fun handleGenericLocal(registerNum: Int, sb: StringBuilder) {
        val localVariableInfo = localVariableInfos[registerNum]
        localVariableInfo?.apply {
            sb.append("    # ")
            if (name != null) {
                sb.append("\"")
                sb.append(name)
                sb.append("\"")
            } else {
                sb.append("null")
            }
            sb.append(":")
            sb.append(type)
            if (signature != null) {
                sb.append(", \"")
                sb.append(signature)
                sb.append("\"")
            }
        }
    }

    private fun addDebugInfo(offset: Int, info: String) {
        val infos = debugState.computeIfAbsent(offset) { ArrayList() }
        infos.add(info)
    }
}

internal class LocalVariableInfo internal constructor(val name: String?, val type: String?, val signature: String?)
