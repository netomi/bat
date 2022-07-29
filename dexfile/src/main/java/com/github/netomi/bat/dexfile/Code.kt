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
package com.github.netomi.bat.dexfile

import com.github.netomi.bat.dexfile.debug.DebugInfo
import com.github.netomi.bat.dexfile.instruction.DexInstruction
import com.github.netomi.bat.dexfile.instruction.visitor.InstructionVisitor
import com.github.netomi.bat.dexfile.io.DexDataInput
import com.github.netomi.bat.dexfile.io.DexDataOutput
import com.github.netomi.bat.dexfile.visitor.DataItemVisitor
import com.github.netomi.bat.dexfile.visitor.ReferencedIDVisitor
import com.github.netomi.bat.dexfile.visitor.TryVisitor
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.LinkedHashSet

/**
 * A class representing a code item inside a dex file.
 *
 * @see [code item @ dex format](https://source.android.com/devices/tech/dalvik/dex-format#code-item)
 */
@DataItemAnn(
    type          = TYPE_CODE_ITEM,
    dataAlignment = 4,
    dataSection   = true)
class Code private constructor(
    _registersSize:    Int                            = 0,
    _insSize:          Int                            = 0,
    _outsSize:         Int                            = 0,
    _insns:            ShortArray                     = EMPTY_INSTRUCTIONS,
    _tryList:          ArrayList<Try>                 = ArrayList(0),
    _debugInfo:        DebugInfo                      = DebugInfo.empty()) : DataItem() {

    var registersSize = _registersSize
        set(value) {
            assert(registersSize >= 0)
            field = value
        }

    var insSize = _insSize
        internal set(value) {
            assert(value >= 0)
            field = value
        }

    var outsSize = _outsSize
        set(value) {
            assert(value >= 0)
            field = value
        }

    val insnsSize: Int
        get() = insns.size

    var insns: ShortArray = _insns

    var tryList: ArrayList<Try> = _tryList

    var debugInfoOffset = 0
        private set

    var debugInfo: DebugInfo = _debugInfo

    override val isEmpty: Boolean
        get() = insnsSize == 0

    internal fun sort() {
        tryList.sortBy { it.startAddr }
    }

    override fun read(input: DexDataInput) {
        registersSize   = input.readUnsignedShort()
        insSize         = input.readUnsignedShort()
        outsSize        = input.readUnsignedShort()
        val triesSize   = input.readUnsignedShort()
        debugInfoOffset = input.readInt()
        val instructionsSize = input.readInt()
        insns = ShortArray(instructionsSize)
        for (i in 0 until instructionsSize) {
            insns[i] = input.readShort()
        }
        if (triesSize > 0 && instructionsSize % 2 == 1) {
            // read padding
            input.readUnsignedShort()
        }
        if (triesSize > 0) {
            tryList.clear()
            tryList.ensureCapacity(triesSize)
            for (i in 0 until triesSize) {
                tryList.add(Try.readContent(input))
            }

            val startOffset = input.offset
            val catchHandlerSize = input.readUleb128()
            val offsetMap = HashMap<Int, EncodedCatchHandler>()
            for (i in 0 until catchHandlerSize) {
                val currentOffset = input.offset
                val encodedCatchHandler = EncodedCatchHandler.readContent(input)
                offsetMap[currentOffset - startOffset] = encodedCatchHandler
            }

            // initialize the associated catch handlers for each try
            for (currentTry in tryList) {
                currentTry.catchHandler = offsetMap[currentTry.handlerOffset]!!
            }
        }
    }

    override fun readLinkedDataItems(input: DexDataInput) {
        if (debugInfoOffset != 0) {
            input.offset = debugInfoOffset
            debugInfo = DebugInfo.readContent(input)
        }
    }

    override fun updateOffsets(dataItemMap: Map) {
        debugInfoOffset = dataItemMap.getOffset(debugInfo)
    }

    override fun write(output: DexDataOutput) {
        output.writeUnsignedShort(registersSize)
        output.writeUnsignedShort(insSize)
        output.writeUnsignedShort(outsSize)
        output.writeUnsignedShort(tryList.size)
        output.writeInt(debugInfoOffset)
        output.writeInt(insnsSize)
        for (i in 0 until insnsSize) {
            output.writeShort(insns[i])
        }
        if (tryList.isNotEmpty() && insnsSize % 2 == 1) {
            output.writeUnsignedShort(0x0)
        }
        if (tryList.isNotEmpty()) {
            val tryStartOffset = output.offset
            for (tryItem in tryList) {
                tryItem.write(output)
            }

            // first collect all catch handlers
            val catchHandlers = LinkedHashSet<EncodedCatchHandler>()
            for (tryElement in tryList) {
                catchHandlers.add(tryElement.catchHandler)
            }

            // write the catch handlers and remember their offset
            val offsetMap = HashMap<EncodedCatchHandler, Int>()
            val startOffset = output.offset
            output.writeUleb128(catchHandlers.size)
            for (encodedCatchHandler in catchHandlers) {
                val currentOffset = output.offset
                offsetMap[encodedCatchHandler] = currentOffset - startOffset
                encodedCatchHandler.write(output)
            }
            val endOffset = output.offset

            // re-write the Try elements with the updated handler offsets.
            output.offset = tryStartOffset
            for (tryItem in tryList) {
                tryItem.handlerOffset = offsetMap[tryItem.catchHandler]!!
                tryItem.write(output)
            }
            output.offset = endOffset
        }
    }

    fun instructionsAccept(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, visitor: InstructionVisitor) {
        var offset = 0
        while (offset < insnsSize) {
            val instruction = DexInstruction.create(insns, offset)
            instruction.accept(dexFile, classDef, method, this, offset, visitor)
            offset += instruction.length
        }
    }

    fun triesAccept(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, visitor: TryVisitor) {
        tryList.forEachIndexed { index, tryElement -> visitor.visitTry(dexFile, classDef, method, this, index, tryElement) }
    }

    override fun dataItemsAccept(dexFile: DexFile, visitor: DataItemVisitor) {
        visitor.visitDebugInfo(dexFile, this, debugInfo)
    }

    internal fun referencedIDsAccept(dexFile: DexFile, visitor: ReferencedIDVisitor) {
        debugInfo.referencedIDsAccept(dexFile, visitor)
        for (tryElement in tryList) {
            tryElement.catchHandler.referencedIDsAccept(dexFile, visitor)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        val o = other as Code

        return registersSize == o.registersSize &&
               insSize       == o.insSize       &&
               outsSize      == o.outsSize      &&
               tryList       == o.tryList       &&
               debugInfo     == o.debugInfo     &&
               insns.contentEquals(o.insns)
    }

    override fun hashCode(): Int {
        return Objects.hash(registersSize, insSize, outsSize, insns.contentHashCode(), tryList, debugInfo)
    }

    override fun toString(): String {
        return "Code[registers=%d,ins=%d,outs=%d,insn=%d code units]".format(registersSize, insSize, outsSize, insSize)
    }

    companion object {
        private val EMPTY_INSTRUCTIONS = ShortArray(0)

        fun empty(): Code {
            return Code()
        }

        fun of(registersSize: Int, insSize: Int, outsSize: Int): Code {
            return Code(registersSize, insSize, outsSize)
        }

        fun readItem(input: DexDataInput): Code {
            val code = Code()
            code.read(input)
            return code
        }
    }
}