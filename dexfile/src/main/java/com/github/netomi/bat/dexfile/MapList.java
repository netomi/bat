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
package com.github.netomi.bat.dexfile;

import com.github.netomi.bat.dexfile.io.DexDataInput;
import com.github.netomi.bat.dexfile.io.DexDataOutput;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A class representing a map list item inside a dex file.
 *
 * @see <a href="https://source.android.com/devices/tech/dalvik/dex-format#map-list">map list item @ dex format</a>
 *
 * @author Thomas Neidhart
 */
@DataItemAnn(
    type          = DexConstants.TYPE_MAP_LIST,
    dataAlignment = 4,
    dataSection   = true
)
public class MapList
extends      DataItem
{
    //public int size; // uint, use mapItems.size().
    private ArrayList<MapItem> mapItems = new ArrayList<>(0);

    public static MapList empty() {
        return new MapList();
    }

    public static MapList readContent(DexDataInput input) {
        MapList mapList = new MapList();
        mapList.read(input);
        return mapList;
    }

    private MapList() {}

    public int getMapItemCount() {
        return mapItems.size();
    }

    public MapItem getMapItem(int index) {
        return mapItems.get(index);
    }

    MapItem getMapItemByType(int type) {
        for (MapItem mapItem : mapItems) {
            if (mapItem.type == type) {
                return mapItem;
            }
        }
        return null;
    }

    void updateMapItem(int type, int size, int offset) {
        MapItem mapItem = getMapItemByType(type);
        if (mapItem == null) {
            mapItem = MapItem.of(type);
            mapItems.add(mapItem);
        }

        mapItem.size   = size;
        mapItem.offset = offset;
    }

    @Override
    protected void read(DexDataInput input) {
        input.skipAlignmentPadding(getDataAlignment());

        int size = input.readInt();
        mapItems.ensureCapacity(size);
        for (int i = 0; i < size; i++) {
            MapItem mapItem = MapItem.readContent(input);
            mapItems.add(mapItem);
        }
    }

    @Override
    protected void write(DexDataOutput output) {
        output.writeAlignmentPadding(getDataAlignment());

        output.writeInt(mapItems.size());
        for (MapItem mapItem : mapItems) {
            mapItem.write(output);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MapList other = (MapList) o;
        return Objects.equals(mapItems, other.mapItems);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mapItems);
    }

    @Override
    public String toString() {
        return String.format("MapList[items=%d]", mapItems.size());
    }
}
