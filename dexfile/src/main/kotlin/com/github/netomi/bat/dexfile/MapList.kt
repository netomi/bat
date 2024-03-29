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
import com.github.netomi.bat.util.mutableListOfCapacity

/**
 * A class representing a map list item inside a dex file.
 *
 * @see <a href="https://source.android.com/devices/tech/dalvik/dex-format#map-list">map list item @ dex format</a>
 */
@DataItemAnn(
    type          = TYPE_MAP_LIST,
    dataAlignment = 4,
    dataSection   = true)
class MapList
    private constructor(private var mapItems: MutableList<MapItem> = mutableListOfCapacity(0)) : DataItem(), Sequence<MapItem> {

    val size: Int
        get() = mapItems.size

    operator fun get(index: Int): MapItem {
        return mapItems[index]
    }

    override fun iterator(): Iterator<MapItem> {
        return mapItems.iterator()
    }

    fun getMapItemByType(type: Int): MapItem? {
        for (mapItem in mapItems) {
            if (mapItem.type == type) {
                return mapItem
            }
        }
        return null
    }

    internal fun updateMapItem(type: Int, size: Int, offset: Int) {
        var mapItem = getMapItemByType(type)
        if (mapItem == null) {
            mapItem = MapItem.of(type)
            mapItems.add(mapItem)
        }

        mapItem.size   = size
        mapItem.offset = if (size > 0) offset else 0
    }

    override val isEmpty: Boolean
        get() = mapItems.isEmpty()

    override fun read(input: DexDataInput) {
        val size = input.readInt()
        mapItems = mutableListOfCapacity(size)
        for (i in 0 until size) {
            val mapItem = MapItem.read(input)
            mapItems.add(mapItem)
        }
    }

    override fun write(output: DexDataOutput) {
        // only write out map items which are not empty and sort by offset
        val filteredMap = mapItems.filter { it.size > 0 }.sortedBy { it.offset }

        output.writeInt(filteredMap.size)
        for (mapItem in filteredMap) {
            mapItem.write(output)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MapList

        return mapItems == other.mapItems
    }

    override fun hashCode(): Int {
        return mapItems.hashCode()
    }

    override fun toString(): String {
        return "MapList[items=${mapItems.size}]"
    }

    companion object {
        internal fun empty(): MapList {
            return MapList()
        }

        internal fun read(input: DexDataInput): MapList {
            val mapList = MapList()
            mapList.read(input)
            return mapList
        }
    }
}