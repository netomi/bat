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
import com.github.netomi.bat.dexfile.util.PrimitiveIterable
import com.github.netomi.bat.dexfile.visitor.TypeVisitor
import com.github.netomi.bat.util.IntArray
import java.util.*

/**
 * A class representing a list of type ids inside a dex file.
 *
 * @see [type list @ dex format](https://source.android.com/devices/tech/dalvik/dex-format.type-list)
 */
@DataItemAnn(type = DexConstants.TYPE_TYPE_LIST, dataAlignment = 4, dataSection = false)
class TypeList private constructor(private val typeList: IntArray = IntArray(0)) : DataItem() {

    val isEmpty: Boolean
        get() = typeCount == 0

    /**
     * Returns the number of types contained in this TypeList.
     */
    val typeCount: Int
        get() = typeList.size()

    fun getType(dexFile: DexFile, index: Int): String {
        return dexFile.getTypeID(typeList[index]).getType(dexFile)
    }

    fun getTypeIndex(index: Int): Int {
        return typeList[index]
    }

    fun getTypes(dexFile: DexFile): Iterable<String> {
        return PrimitiveIterable.of(dexFile, { df, idx -> df.getTypeID(idx).getType(df) }, typeList)
    }

    fun addType(typeIDIndex: Int) {
        typeList.add(typeIDIndex)
    }

    fun addType(dexFile: DexFile, type: String) {
        addType(dexFile.addOrGetTypeIDIndex(type))
    }

    override fun read(input: DexDataInput) {
        input.skipAlignmentPadding(dataAlignment)
        val size = input.readUnsignedInt().toInt()
        typeList.clear()
        typeList.resize(size)
        for (i in 0 until size) {
            val typeIndex = input.readUnsignedShort()
            typeList[i] = typeIndex
        }
    }

    override fun write(output: DexDataOutput) {
        output.writeAlignmentPadding(dataAlignment)
        val size = typeList.size()
        output.writeInt(size)
        for (i in 0 until size) {
            output.writeUnsignedShort(typeList[i])
        }
    }

    fun typesAccept(dexFile: DexFile, visitor: TypeVisitor) {
        val size = typeList.size()
        for (i in 0 until size) {
            visitor.visitType(dexFile, this, i, dexFile.getTypeID(typeList[i]).getType(dexFile))
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TypeList

        return typeList == other.typeList
    }

    override fun hashCode(): Int {
        return Objects.hash(typeList)
    }

    override fun toString(): String {
        return "TypeList[types=${typeList}]"
    }

    companion object {
        /**
         * Returns a new empty TypeList instance.
         */
        @JvmStatic
        fun empty(): TypeList {
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
                typeList.addType(typeIndex)
            }
            return typeList
        }

        @JvmStatic
        fun readContent(input: DexDataInput): TypeList {
            val typeList = TypeList()
            typeList.read(input)
            return typeList
        }
    }
}