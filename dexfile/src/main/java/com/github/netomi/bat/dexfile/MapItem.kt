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
import java.util.*

/**
 * A class representing a map item inside a dex file.
 *
 * Note: this class is not intended for general use, its automatically created when writing dex files.
 *
 * @see [map item @ dex format](https://source.android.com/devices/tech/dalvik/dex-format.map-item)
 */
class MapItem : DexContent {

    var type: Int = 0   // ushort
        private set

    var size: Int = 0   // uint
        internal set

    var offset: Int = 0 // uint
        internal set

    private constructor()
    private constructor(type: Int, size: Int) {
        this.type = type
        this.size = size
    }

    override fun read(input: DexDataInput) {
        type = input.readUnsignedShort()
        input.readUnsignedShort()
        size = input.readInt()
        offset = input.readInt()
    }

    override fun write(output: DexDataOutput) {
        output.writeUnsignedShort(type)
        output.writeUnsignedShort(0x0)
        output.writeInt(size)
        output.writeInt(offset)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val o = other as MapItem
        return type == o.type &&
               size == o.size
    }

    override fun hashCode(): Int {
        return Objects.hash(type, size)
    }

    override fun toString(): String {
        return "MapItem[type=0x%04x,size=%5d,offset=%7d]".format(type, size, offset)
    }

    companion object {
        @JvmStatic
        fun of(type: Int, size: Int = 0): MapItem {
            return MapItem(type, size)
        }

        @JvmStatic
        fun readContent(input: DexDataInput): MapItem {
            val mapItem = MapItem()
            mapItem.read(input)
            return mapItem
        }
    }
}