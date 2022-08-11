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
import com.github.netomi.bat.dexfile.visitor.ArrayElementAccessor
import com.github.netomi.bat.dexfile.visitor.ReferencedIDVisitor
import com.github.netomi.bat.dexfile.visitor.TypeVisitor
import com.google.common.primitives.Ints

/**
 * A class representing a list of type ids inside a dex file.
 *
 * @see [type list @ dex format](https://source.android.com/devices/tech/dalvik/dex-format.type-list)
 */
@DataItemAnn(
    type          = TYPE_TYPE_LIST,
    dataAlignment = 4,
    dataSection   = false)
class TypeList private constructor(private var typeList: IntArray = intArrayOf()): DataItem(), Comparable<TypeList>, Sequence<Int> {

    override val isEmpty: Boolean
        get() = typeList.isEmpty()

    /**
     * Returns the number of types contained in this TypeList.
     */
    val typeCount: Int
        get() = typeList.size

    fun getType(dexFile: DexFile, index: Int): String {
        return dexFile.getTypeID(typeList[index]).getType(dexFile)
    }

    fun getTypeIndex(index: Int): Int {
        return typeList[index]
    }

    fun getTypes(dexFile: DexFile): List<String> {
        return typeList.map { dexFile.getTypeID(it).getType(dexFile) }.toList()
    }

    internal fun addType(typeIDIndex: Int) {
        val oldSize = typeList.size
        typeList = typeList.copyOf(oldSize + 1)
        typeList[oldSize] = typeIDIndex
    }

    override fun iterator(): IntIterator {
        return typeList.iterator()
    }

    override fun read(input: DexDataInput) {
        val size = input.readInt()
        if (typeList.size != size) {
            typeList = IntArray(size)
        }
        for (i in typeList.indices) {
            val typeIndex = input.readUnsignedShort()
            typeList[i] = typeIndex
        }
    }

    override fun write(output: DexDataOutput) {
        val size = typeList.size
        output.writeInt(size)
        for (i in typeList.indices) {
            output.writeUnsignedShort(typeList[i])
        }
    }

    fun typesAccept(dexFile: DexFile, visitor: TypeVisitor) {
        for (i in typeList.indices) {
            visitor.visitType(dexFile, this, i, typeList[i], dexFile.getTypeID(typeList[i]).getType(dexFile))
        }
    }

    internal fun referencedIDsAccept(dexFile: DexFile, visitor: ReferencedIDVisitor) {
        for (i in typeList.indices) {
            visitor.visitTypeID(dexFile, ArrayElementAccessor(typeList, i))
        }
    }

    override fun compareTo(other: TypeList): Int {
        return Ints.lexicographicalComparator().compare(typeList, other.typeList)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TypeList

        return typeList.contentEquals(other.typeList)
    }

    override fun hashCode(): Int {
        return typeList.contentHashCode()
    }

    override fun toString(): String {
        return "TypeList[types=${typeList.contentToString()}]"
    }

    companion object {
        /**
         * Returns a new empty TypeList instance.
         */
        internal fun empty(): TypeList {
            return TypeList()
        }

        /**
         * Returns a new TypeList instance containing the given type indices.
         *
         * @param typeIndices the type indices that the list should contain.
         * @return a new TypeList instance containing the type indices.
         */
        fun of(vararg typeIndices: Int): TypeList {
            val typeList = TypeList()
            for (typeIndex in typeIndices) {
                require(typeIndex >= 0) { "type index must not be negative" }
                typeList.addType(typeIndex)
            }
            return typeList
        }

        internal fun read(input: DexDataInput): TypeList {
            val typeList = TypeList()
            typeList.read(input)
            return typeList
        }
    }
}