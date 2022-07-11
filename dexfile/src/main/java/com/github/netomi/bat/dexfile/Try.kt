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
import com.google.common.base.Preconditions
import java.util.*

/**
 * A class representing a try item inside a dex file.
 *
 * @see [try item @ dex format](https://source.android.com/devices/tech/dalvik/dex-format.type-item)
 */
class Try private constructor(): DexContent() {

    var startAddr: Int = 0
        private set

    var insnCount: Int = 0
        private set

    lateinit var catchHandler: EncodedCatchHandler
        internal set

    // package-private as these fields are set from the Code item.
    var handlerOffset = 0
        // TODO: make set internal

    val endAddr: Int
        get() = startAddr + insnCount - 1

    private constructor(startAddr: Int, insnCount: Int, catchHandler: EncodedCatchHandler): this() {
        this.startAddr    = startAddr
        this.insnCount    = insnCount
        this.catchHandler = catchHandler
    }

    override fun read(input: DexDataInput) {
        startAddr     = input.readInt()
        insnCount     = input.readUnsignedShort()
        handlerOffset = input.readUnsignedShort()
    }

    override fun write(output: DexDataOutput) {
        output.writeInt(startAddr)
        output.writeUnsignedShort(insnCount)
        output.writeUnsignedShort(handlerOffset)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val o = other as Try
        return startAddr    == o.startAddr &&
               insnCount    == o.insnCount &&
               catchHandler == o.catchHandler
    }

    override fun hashCode(): Int {
        return Objects.hash(startAddr, insnCount, catchHandler)
    }

    override fun toString(): String {
        return "Try[startAddr=%04x,insnCount=%d,handler=%s]".format(startAddr, insnCount, catchHandler)
    }

    companion object {
        @JvmStatic
        fun of(startAddr: Int, endAddr: Int, catchHandler: EncodedCatchHandler): Try {
            Preconditions.checkArgument(startAddr >= 0, "startAddr must not be negative")
            Preconditions.checkArgument(startAddr <= 65535, "startAddr must be <= 65535")
            Preconditions.checkArgument(endAddr >= 0, "endAddr must not be negative")
            Preconditions.checkArgument(endAddr <= 65534, "endAddr must be <= 65534")
            Preconditions.checkArgument(endAddr > startAddr, "endAddr must be > startAddr")

            val insnCount = endAddr - startAddr + 1
            return Try(startAddr, insnCount, catchHandler)
        }

        @JvmStatic
        fun readContent(input: DexDataInput): Try {
            val tryItem = Try()
            tryItem.read(input)
            return tryItem
        }
    }
}