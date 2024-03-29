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
import com.github.netomi.bat.dexfile.util.DexType
import com.github.netomi.bat.dexfile.visitor.PropertyAccessor
import com.github.netomi.bat.dexfile.visitor.ReferencedIDVisitor
import com.github.netomi.bat.util.Copyable
import java.util.*

/**
 * A class representing an encoded type address pair inside a dex file.
 *
 * @see <a href="https://source.android.com/devices/tech/dalvik/dex-format#encoded-type-addr-pair">type addr pair @ dex format</a>
 */
class TypeAddrPair private constructor(            typeIndex: Int     = NO_INDEX,
                                                   address:   Int     = 0,
                                       private val label:     String? = null) : DexContent(), Copyable<TypeAddrPair> {

    var typeIndex: Int = typeIndex
        private set

    var address: Int = address
        private set

    fun getType(dexFile: DexFile): DexType {
        return dexFile.getTypeID(typeIndex).getType(dexFile)
    }

    internal fun copyWithoutLabels(): TypeAddrPair {
        return of(typeIndex, address)
    }

    internal fun updateOffsets(offsetMap: OffsetMap) {
        address = label?.let { offsetMap.getOffset(it) } ?: offsetMap.getNewOffset(address)
    }

    override fun read(input: DexDataInput) {
        typeIndex = input.readUleb128()
        address   = input.readUleb128()
    }

    override fun write(output: DexDataOutput) {
        output.writeUleb128(typeIndex)
        output.writeUleb128(address)
    }

    internal fun referencedIDsAccept(dexFile: DexFile, visitor: ReferencedIDVisitor) {
        visitor.visitTypeID(dexFile, PropertyAccessor(::typeIndex))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        val o = other as TypeAddrPair

        return typeIndex == o.typeIndex &&
               address   == o.address   &&
               label     == o.label
    }

    override fun hashCode(): Int {
        return Objects.hash(typeIndex, address, label)
    }

    override fun toString(): String {
        return if (label != null) {
            "TypeAddrPair[type=${typeIndex},label=${label}]"
        } else {
            "TypeAddrPair[type=${typeIndex},addr=${address}]"
        }
    }

    override fun copy(): TypeAddrPair {
        return TypeAddrPair(typeIndex, address, label)
    }

    companion object {
        fun of(typeIndex: Int, addr: Int): TypeAddrPair {
            require(typeIndex >= 0) { "typeIndex must not be negative" }
            require(addr >= 0) { "addr must not be negative" }
            return TypeAddrPair(typeIndex, addr)
        }

        fun of(typeIndex: Int, label: String): TypeAddrPair {
            require(typeIndex >= 0) { "typeIndex must not be negative" }
            return TypeAddrPair(typeIndex, 0, label)
        }

        internal fun read(input: DexDataInput): TypeAddrPair {
            val typeAddrPair = TypeAddrPair()
            typeAddrPair.read(input)
            return typeAddrPair
        }
    }
}