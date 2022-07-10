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

import com.github.netomi.bat.dexfile.DexConstants.NO_INDEX
import com.github.netomi.bat.dexfile.io.DexDataInput
import com.github.netomi.bat.dexfile.io.DexDataOutput
import dev.ahmedmourad.nocopy.annotations.NoCopy
import kotlin.collections.ArrayList
import kotlin.math.abs

/**
 * A class representing an encoded catch handler inside a dex file.
 *
 * @see [encoded catch handler @ dex format](https://source.android.com/devices/tech/dalvik/dex-format.encoded-catch-handler)
 */
@NoCopy
data class EncodedCatchHandler private constructor(
    private var catchAllAddr_: Int                     = NO_INDEX, // uleb128
    private val handlers_:     ArrayList<TypeAddrPair> = ArrayList(0)) : DexContent() {

    val catchAllAddr: Int
        get() = catchAllAddr_

    val handlers: List<TypeAddrPair>
        get() = handlers_

    val handlerCount: Int
        get() = handlers_.size

    fun getHandler(index: Int): TypeAddrPair {
        return handlers_[index]
    }

    override fun read(input: DexDataInput) {
        val readSize = input.readSleb128()
        val size = abs(readSize)
        handlers_.clear()
        handlers_.ensureCapacity(size)
        for (i in 0 until size) {
            val typeAddrPair = TypeAddrPair.readContent(input)
            handlers_.add(typeAddrPair)
        }
        if (readSize <= 0) {
            catchAllAddr_ = input.readUleb128()
        }
    }

    override fun write(output: DexDataOutput) {
        var writtenSize = handlers_.size
        if (catchAllAddr_ != -1) {
            writtenSize = -writtenSize
        }
        output.writeSleb128(writtenSize)
        for (typeAddrPair in handlers_) {
            typeAddrPair.write(output)
        }
        if (writtenSize <= 0) {
            output.writeUleb128(catchAllAddr_)
        }
    }

    override fun toString(): String {
        return "EncodedCatchHandler[handlers=%d,catchAllAddr=%04x]".format(handlers_.size, catchAllAddr_)
    }

    companion object {
        @JvmStatic
        fun of(catchAllAddr: Int, vararg handlers: TypeAddrPair): EncodedCatchHandler {
            return EncodedCatchHandler(catchAllAddr, arrayListOf(*handlers))
        }

        @JvmStatic
        fun readContent(input: DexDataInput): EncodedCatchHandler {
            val encodedCatchHandler = EncodedCatchHandler()
            encodedCatchHandler.read(input)
            return encodedCatchHandler
        }
    }
}