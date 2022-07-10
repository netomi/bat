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
import dev.ahmedmourad.nocopy.annotations.NoCopy
import kotlin.collections.ArrayList

/**
 * A class representing a map list item inside a dex file.
 *
 * @see [map list item @ dex format](https://source.android.com/devices/tech/dalvik/dex-format.map-list)
 */
@DataItemAnn(
    type          = DexConstants.TYPE_MAP_LIST,
    dataAlignment = 4,
    dataSection   = true)
@NoCopy
data class MapList private constructor(private val mapItems_: ArrayList<MapItem> = ArrayList(0)) : DataItem() {

    val mapItems: List<MapItem>
        get() = mapItems_

    val mapItemCount: Int
        get() = mapItems_.size

    fun getMapItem(index: Int): MapItem {
        return mapItems_[index]
    }

    fun getMapItemByType(type: Int): MapItem? {
        for (mapItem in mapItems_) {
            if (mapItem.type == type) {
                return mapItem
            }
        }
        return null
    }

    fun updateMapItem(type: Int, size: Int, offset: Int) {
        var mapItem = getMapItemByType(type)
        if (mapItem == null) {
            mapItem = MapItem.of(type)
            mapItems_.add(mapItem)
        }

        mapItem.size   = size
        mapItem.offset = if (size > 0) offset else 0
    }

    override fun read(input: DexDataInput) {
        input.skipAlignmentPadding(dataAlignment)
        val size = input.readInt()
        mapItems_.clear()
        mapItems_.ensureCapacity(size)
        for (i in 0 until size) {
            val mapItem = MapItem.readContent(input)
            mapItems_.add(mapItem)
        }
    }

    override fun write(output: DexDataOutput) {
        output.writeAlignmentPadding(dataAlignment)

        // only write out map items which are not empty and sort by offset
        val filteredMap = mapItems_.filter { it.size > 0 }.sortedBy { it.offset }

        output.writeInt(filteredMap.size)
        for (mapItem in filteredMap) {
            mapItem.write(output)
        }
    }

    override fun toString(): String {
        return "MapList[items=${mapItems_.size}]"
    }

    companion object {
        internal fun empty(): MapList {
            return MapList()
        }

        @JvmStatic
        internal fun readMapList(input: DexDataInput): MapList {
            val mapList = MapList()
            mapList.read(input)
            return mapList
        }
    }
}