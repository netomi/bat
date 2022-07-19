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

package com.github.netomi.bat.dexdump

import com.github.netomi.bat.dexfile.Code
import com.github.netomi.bat.dexfile.DexFile
import com.github.netomi.bat.dexfile.EncodedMethod
import com.github.netomi.bat.dexfile.debug.*
import com.github.netomi.bat.dexfile.visitor.DebugSequenceVisitor
import com.github.netomi.bat.util.Primitives

internal class LocalVariablePrinter constructor(
                dexFile: DexFile,
                method:  EncodedMethod,
                code:    Code,
    private val printer: Mutf8Printer) : DebugSequenceVisitor {

    private val codeSize:      Int
    private val variableInfos: Array<LocalVariableInfo?>
    private var codeOffset:    Int = 0

    init {
        codeSize      = code.insnsSize
        variableInfos = arrayOfNulls(code.registersSize)

        // initialize the local variable info with the method parameters.
        var register = code.registersSize - code.insSize
        if (!method.isStatic) {
            val classType = method.getClassType(dexFile)
            variableInfos[register++] = LocalVariableInfo("this", classType, null)
        }

        val debugInfo  = code.debugInfo
        val protoID    = method.getProtoID(dexFile)
        val parameters = protoID.parameters

        debugInfo?.apply {
            var i = 0
            while (i < parameterCount && register < code.registersSize) {
                val parameterName = getParameterName(dexFile, i)
                val parameterType = if (i < parameters.typeCount) parameters.getType(dexFile, i) else null
                variableInfos[register] = LocalVariableInfo(parameterName, parameterType, null)

                // TODO: extract into util class.
                if (parameterType == "J" || parameterType == "D") {
                    register++
                }

                i++
                register++
            }
        }
    }

    fun finish() {
        for (i in variableInfos.indices) {
            val info = variableInfos[i]
            if (info != null && info.endAddr == -1) {
                info.endAddr = codeSize
                printLocal(i, info)
            }
        }
    }

    override fun visitAnyDebugInstruction(dexFile: DexFile, debugInfo: DebugInfo, instruction: DebugInstruction) {}

    override fun visitAdvanceLineAndPC(dexFile: DexFile, debugInfo: DebugInfo, instruction: DebugAdvanceLineAndPC) {
        codeOffset += instruction.addrDiff
    }

    override fun visitAdvancePC(dexFile: DexFile, debugInfo: DebugInfo, instruction: DebugAdvancePC) {
        codeOffset += instruction.addrDiff
    }

    override fun visitStartLocal(dexFile: DexFile, debugInfo: DebugInfo, instruction: DebugStartLocal) {
        val name = instruction.name(dexFile)
        val type = instruction.type(dexFile)

        handleStartLocalInstructions(instruction.registerNum, name, type, null)
    }

    override fun visitStartLocalExtended(dexFile: DexFile, debugInfo: DebugInfo, instruction: DebugStartLocalExtended) {
        val name = instruction.name(dexFile)
        val type = instruction.type(dexFile)
        val sig  = instruction.signature(dexFile)

        handleStartLocalInstructions(instruction.registerNum, name, type, sig)
    }

    private fun handleStartLocalInstructions(registerNum: Int, name: String?, type: String?, sig: String?) {
        var variableInfo = variableInfos[registerNum]

        // only for compatibility with dexdump:
        // print the method parameters potentially twice
        if (variableInfo != null && variableInfo.endAddr == -1) {
            variableInfo.endAddr = codeOffset
            printLocal(registerNum, variableInfo)
        }

        variableInfo = LocalVariableInfo(name, type, sig)
        variableInfo.startAddr = codeOffset
        variableInfos[registerNum] = variableInfo
    }

    override fun visitEndLocal(dexFile: DexFile, debugInfo: DebugInfo, instruction: DebugEndLocal) {
        val variableInfo = variableInfos[instruction.registerNum]
        variableInfo?.apply {
            endAddr = codeOffset
            printLocal(instruction.registerNum, this)
        }

    }

    override fun visitRestartLocal(dexFile: DexFile, debugInfo: DebugInfo, instruction: DebugRestartLocal) {
        val variableInfo = variableInfos[instruction.registerNum]
        variableInfo?.apply {
            startAddr = codeOffset
            endAddr   = -1
        }
    }

    private fun printLocal(registerNum: Int, variableInfo: LocalVariableInfo) {
        printer.println(variableInfo.toString(registerNum))
    }
}

private class LocalVariableInfo constructor(private val name: String?, private val type: String?, private val signature: String?) {
    var startAddr = 0
    var endAddr   = -1

    fun toString(registerNum: Int): String {
        val sb = StringBuilder()
        sb.append("    ")
        sb.append(Primitives.toHexString(startAddr.toShort()))
        sb.append(" - ")
        sb.append(Primitives.toHexString(endAddr.toShort()))
        sb.append(" reg=")
        sb.append(registerNum)
        sb.append(' ')
        sb.append(name ?: "(null)")
        sb.append(' ')
        sb.append(type)

        signature?.apply {
            sb.append(' ')
            sb.append(this)
        }

        return sb.toString()
    }
}