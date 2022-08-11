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

import com.github.netomi.bat.dexfile.instruction.editor.OffsetMap
import com.github.netomi.bat.dexfile.io.DexDataInput
import com.github.netomi.bat.dexfile.io.DexDataOutput
import com.github.netomi.bat.dexfile.visitor.ReferencedIDVisitor
import com.github.netomi.bat.util.Copyable
import com.github.netomi.bat.util.deepCopy
import com.github.netomi.bat.util.mutableListOfCapacity
import java.util.*
import kotlin.math.abs

/**
 * A class representing an encoded catch handler inside a dex file.
 *
 * @see [encoded catch handler @ dex format](https://source.android.com/devices/tech/dalvik/dex-format.encoded-catch-handler)
 */
class EncodedCatchHandler
    private constructor(            catchAllAddr:  Int                       = -1,
                        private val catchAllLabel: String?                   = null,
                        private var _handlers:     MutableList<TypeAddrPair> = mutableListOfCapacity(0)) : DexContent(), Copyable<EncodedCatchHandler> {

    var catchAllAddr: Int = catchAllAddr
        private set

    val handlers: List<TypeAddrPair>
        get() = _handlers

    val handlerCount: Int
        get() = handlers.size

    fun getHandler(index: Int): TypeAddrPair {
        return handlers[index]
    }

    internal fun copyWithoutLabels(): EncodedCatchHandler {
        return of(catchAllAddr, handlers.map { it.copyWithoutLabels() })
    }

    internal fun updateOffsets(offsetMap: OffsetMap) {
        catchAllAddr =
            catchAllLabel?.let { offsetMap.getOffset(catchAllLabel) } ?: offsetMap.getNewOffset(catchAllAddr)

        for (handler in handlers) {
            handler.updateOffsets(offsetMap)
        }
    }

    override fun read(input: DexDataInput) {
        val readSize = input.readSleb128()
        val size = abs(readSize)
        _handlers = mutableListOfCapacity(size)
        for (i in 0 until size) {
            val typeAddrPair = TypeAddrPair.readContent(input)
            _handlers.add(typeAddrPair)
        }
        if (readSize <= 0) {
            catchAllAddr = input.readUleb128()
        }
    }

    override fun write(output: DexDataOutput) {
        var writtenSize = _handlers.size
        if (catchAllAddr != -1) {
            writtenSize = -writtenSize
        }
        output.writeSleb128(writtenSize)
        for (typeAddrPair in _handlers) {
            typeAddrPair.write(output)
        }
        if (writtenSize <= 0) {
            output.writeUleb128(catchAllAddr)
        }
    }

    internal fun referencedIDsAccept(dexFile: DexFile, visitor: ReferencedIDVisitor) {
        _handlers.forEach { it.referencedIDsAccept(dexFile, visitor) }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EncodedCatchHandler

        return catchAllAddr  == other.catchAllAddr &&
               catchAllLabel == other.catchAllLabel &&
               _handlers     == other._handlers
    }

    override fun hashCode(): Int {
        return Objects.hash(catchAllAddr, catchAllLabel, _handlers)
    }

    override fun toString(): String {
        return if (catchAllLabel != null) {
            "EncodedCatchHandler[handlers=%d items,catchAllLabel=%s]".format(_handlers.size, catchAllLabel)
        } else {
            "EncodedCatchHandler[handlers=%d items,catchAllAddr=%04x]".format(_handlers.size, catchAllAddr)
        }
    }

    override fun copy(): EncodedCatchHandler {
        val newHandlers = _handlers.deepCopy()
        return EncodedCatchHandler(catchAllAddr, catchAllLabel, newHandlers.toMutableList())
    }

    companion object {
        fun empty(): EncodedCatchHandler {
            return EncodedCatchHandler()
        }

        fun of(catchAllAddr: Int): EncodedCatchHandler {
            return EncodedCatchHandler(catchAllAddr)
        }

        fun of(catchAllLabel: String): EncodedCatchHandler {
            return EncodedCatchHandler(0, catchAllLabel)
        }

        fun of(handlers: List<TypeAddrPair>): EncodedCatchHandler {
            return EncodedCatchHandler(NO_INDEX, null, handlers.toMutableList())
        }

        fun of(catchAllAddr: Int, vararg handlers: TypeAddrPair): EncodedCatchHandler {
            return EncodedCatchHandler(catchAllAddr, null, mutableListOf(*handlers))
        }

        fun of(catchAllLabel: String, vararg handlers: TypeAddrPair): EncodedCatchHandler {
            return EncodedCatchHandler(0, catchAllLabel, mutableListOf(*handlers))
        }

        fun of(catchAllAddr: Int, handlers: List<TypeAddrPair>): EncodedCatchHandler {
            return EncodedCatchHandler(catchAllAddr, null, handlers.toMutableList())
        }

        fun of(catchAllLabel: String, handlers: List<TypeAddrPair>): EncodedCatchHandler {
            return EncodedCatchHandler(0, catchAllLabel, handlers.toMutableList())
        }

        fun readContent(input: DexDataInput): EncodedCatchHandler {
            val encodedCatchHandler = EncodedCatchHandler()
            encodedCatchHandler.read(input)
            return encodedCatchHandler
        }
    }
}