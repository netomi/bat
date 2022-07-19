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
package com.github.netomi.bat.dexfile.debug

import com.github.netomi.bat.dexfile.DataItem
import com.github.netomi.bat.dexfile.DataItemAnn
import com.github.netomi.bat.dexfile.DexFile
import com.github.netomi.bat.dexfile.TYPE_DEBUG_INFO_ITEM
import com.github.netomi.bat.dexfile.debug.DebugInstruction.Companion.readInstruction
import com.github.netomi.bat.dexfile.io.DexDataInput
import com.github.netomi.bat.dexfile.io.DexDataOutput
import com.github.netomi.bat.dexfile.visitor.ArrayElementAccessor
import com.github.netomi.bat.dexfile.visitor.DebugSequenceVisitor
import com.github.netomi.bat.dexfile.visitor.ReferencedIDVisitor
import java.util.*
import kotlin.collections.ArrayList

/**
 * A class representing a debug info item inside a dex file.
 *
 * @see [debug info item @ dex format](https://source.android.com/devices/tech/dalvik/dex-format.debug-info-item)
 */
@DataItemAnn(
    type          = TYPE_DEBUG_INFO_ITEM,
    dataAlignment = 1,
    dataSection   = true)
class DebugInfo private constructor() : DataItem() {

    var lineStart = 0
        private set

    private var parameterNames                             = intArrayOf()
    private val debugSequence: ArrayList<DebugInstruction> = ArrayList(0)

    val parameterCount: Int
        get() = parameterNames.size

    fun getParameterName(dexFile: DexFile, parameterIndex: Int): String? {
        return dexFile.getStringNullable(parameterNames[parameterIndex])
    }

    override val isEmpty: Boolean
        get() = false

    override fun read(input: DexDataInput) {
        lineStart = input.readUleb128()

        val parametersSize = input.readUleb128()
        if (parameterNames.size != parametersSize) {
            parameterNames = IntArray(parametersSize)
        }

        for (i in parameterNames.indices) {
            parameterNames[i] = input.readUleb128p1()
        }

        var debugInstruction: DebugInstruction

        do {
            debugInstruction = readInstruction(input)
            debugSequence.add(debugInstruction)
        } while (debugInstruction.opcode != DebugInstruction.DBG_END_SEQUENCE)

        debugSequence.trimToSize()
    }

    override fun write(output: DexDataOutput) {
        output.writeUleb128(lineStart)
        output.writeUleb128(parameterNames.size)

        for (element in parameterNames) {
            output.writeUleb128p1(element)
        }

        for (debugInstruction in debugSequence) {
            debugInstruction.writeInternal(output)
        }
    }

    fun debugSequenceAccept(dexFile: DexFile, visitor: DebugSequenceVisitor) {
        for (debugInstruction in debugSequence) {
            debugInstruction.accept(dexFile, this, visitor)
        }
    }

    internal fun referencedIDsAccept(dexFile: DexFile, visitor: ReferencedIDVisitor) {
        for (i in parameterNames.indices) {
            visitor.visitStringID(dexFile, ArrayElementAccessor(parameterNames, i))
        }

        debugSequence.forEach { it.referencedIDsAccept(dexFile, visitor) }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val o = other as DebugInfo

        return lineStart      == o.lineStart      &&
               debugSequence  == o.debugSequence  &&
               parameterNames.contentEquals(o.parameterNames)
    }

    override fun hashCode(): Int {
        return Objects.hash(lineStart, parameterNames.contentHashCode(), debugSequence)
    }

    override fun toString(): String {
        return "DebugInfo[lineStart=${lineStart},parameterNames=${parameterNames.size} items,debugSequence=${debugSequence.size} items]"
    }

    companion object {
        @JvmStatic
        fun readContent(input: DexDataInput): DebugInfo {
            val debugInfo = DebugInfo()
            debugInfo.read(input)
            return debugInfo
        }
    }
}