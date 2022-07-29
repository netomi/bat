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
import com.google.common.base.Preconditions
import java.util.*

/**
 * A class representing a try item inside a dex file.
 *
 * @see [try item @ dex format](https://source.android.com/devices/tech/dalvik/dex-format.type-item)
 */
class Try private constructor(_startAddr:    Int                 = 0,
                              _insnCount:    Int                 = 0,
                              _startLabel:   String?             = null,
                              _endLabel:     String?             = null,
                              _catchHandler: EncodedCatchHandler = EncodedCatchHandler.empty()): DexContent() {

    var startAddr: Int = _startAddr
        internal set

    var insnCount: Int = _insnCount
        internal set

    var startLabel: String? = _startLabel
        internal set

    var endLabel: String?   = _endLabel
        internal set

    var catchHandler: EncodedCatchHandler = _catchHandler
        internal set

    // internal as this field is set from the Code item.
    var handlerOffset = 0
        internal set

    val endAddr: Int
        get() = startAddr + insnCount - 1

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

    internal fun referencedIDsAccept(dexFile: DexFile, visitor: ReferencedIDVisitor) {
        catchHandler.referencedIDsAccept(dexFile, visitor)
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
        fun of(startAddr: Int, endAddr: Int, catchHandler: EncodedCatchHandler): Try {
            Preconditions.checkArgument(startAddr >= 0, "startAddr must not be negative")
            Preconditions.checkArgument(startAddr <= 65535, "startAddr must be <= 65535")
            Preconditions.checkArgument(endAddr >= 0, "endAddr must not be negative")
            Preconditions.checkArgument(endAddr <= 65534, "endAddr must be <= 65534")
            Preconditions.checkArgument(endAddr >= startAddr, "endAddr must be > startAddr")

            val insnCount = endAddr - startAddr + 1
            return Try(startAddr, insnCount, null, null, catchHandler)
        }

        fun of(startLabel: String, endLabel: String, catchHandler: EncodedCatchHandler): Try {
            return Try(0, 0, startLabel, endLabel, catchHandler)
        }

        fun readContent(input: DexDataInput): Try {
            val tryItem = Try()
            tryItem.read(input)
            return tryItem
        }
    }
}