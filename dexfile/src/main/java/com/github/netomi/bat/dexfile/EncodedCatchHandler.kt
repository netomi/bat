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

import com.github.netomi.bat.dexfile.io.DexDataInput
import com.github.netomi.bat.dexfile.io.DexDataOutput
import com.github.netomi.bat.dexfile.visitor.ReferencedIDVisitor
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs

/**
 * A class representing an encoded catch handler inside a dex file.
 *
 * @see [encoded catch handler @ dex format](https://source.android.com/devices/tech/dalvik/dex-format.encoded-catch-handler)
 */
class EncodedCatchHandler private constructor(
    _catchAllAddr:         Int                     = -1,
    _catchAllLabel:        String?                 = null,
    private val _handlers: ArrayList<TypeAddrPair> = ArrayList(0)) : DexContent() {

    var catchAllAddr: Int = _catchAllAddr
        internal set

    var catchAllLabel: String? = _catchAllLabel
        internal set

    val handlers: List<TypeAddrPair>
        get() = _handlers

    val handlerCount: Int
        get() = _handlers.size

    fun getHandler(index: Int): TypeAddrPair {
        return _handlers[index]
    }

    override fun read(input: DexDataInput) {
        val readSize = input.readSleb128()
        val size = abs(readSize)
        _handlers.clear()
        _handlers.ensureCapacity(size)
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
        handlers.forEach { it.referencedIDsAccept(dexFile, visitor) }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EncodedCatchHandler

        return catchAllAddr == other.catchAllAddr &&
               _handlers    == other._handlers
    }

    override fun hashCode(): Int {
        return Objects.hash(catchAllAddr, _handlers)
    }

    override fun toString(): String {
        return "EncodedCatchHandler[handlers=%d,catchAllAddr=%04x]".format(_handlers.size, catchAllAddr)
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
            return EncodedCatchHandler(NO_INDEX, null, ArrayList(handlers))
        }

        fun of(catchAllAddr: Int, vararg handlers: TypeAddrPair): EncodedCatchHandler {
            return EncodedCatchHandler(catchAllAddr, null, arrayListOf(*handlers))
        }

        fun of(catchAllLabel: String, vararg handlers: TypeAddrPair): EncodedCatchHandler {
            return EncodedCatchHandler(0, catchAllLabel, arrayListOf(*handlers))
        }

        fun of(catchAllAddr: Int, handlers: List<TypeAddrPair>): EncodedCatchHandler {
            return EncodedCatchHandler(catchAllAddr, null, ArrayList(handlers))
        }

        fun of(catchAllLabel: String, handlers: List<TypeAddrPair>): EncodedCatchHandler {
            return EncodedCatchHandler(0, catchAllLabel, ArrayList(handlers))
        }

        fun readContent(input: DexDataInput): EncodedCatchHandler {
            val encodedCatchHandler = EncodedCatchHandler()
            encodedCatchHandler.read(input)
            return encodedCatchHandler
        }
    }
}