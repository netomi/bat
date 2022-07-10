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
import com.google.common.base.Preconditions
import dev.ahmedmourad.nocopy.annotations.NoCopy

/**
 * A class representing an encoded type address pair inside a dex file.
 *
 * @see [type addr pair @ dex format](https://source.android.com/devices/tech/dalvik/dex-format.encoded-type-addr-pair)
 */
@NoCopy
data class TypeAddrPair private constructor(
    private var typeIndex_: Int = NO_INDEX,
    private var address_:   Int = 0) : DexContent() {

    val typeIndex: Int
        get() = typeIndex_

    val address: Int
        get() = address_

    fun getType(dexFile: DexFile): String {
        return dexFile.getTypeID(typeIndex_).getType(dexFile)
    }

    override fun read(input: DexDataInput) {
        typeIndex_ = input.readUleb128()
        address_ = input.readUleb128()
    }

    override fun write(output: DexDataOutput) {
        output.writeUleb128(typeIndex_)
        output.writeUleb128(address_)
    }
    
    override fun toString(): String {
        return "TypeAddrPair[type=${typeIndex_},addr=${address_}]"
    }

    companion object {
        @JvmStatic
        fun of(typeIndex: Int, addr: Int): TypeAddrPair {
            Preconditions.checkArgument(typeIndex >= 0, "typeIndex must not be negative")
            Preconditions.checkArgument(addr >= 0, "addr must not be negative")
            return TypeAddrPair(typeIndex, addr)
        }

        @JvmStatic
        fun readContent(input: DexDataInput): TypeAddrPair {
            val typeAddrPair = TypeAddrPair()
            typeAddrPair.read(input)
            return typeAddrPair
        }
    }
}