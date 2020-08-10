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

@DataItemAnn(
    type          = DexConstants.TYPE_MAP_LIST,
    dataAlignment = 4,
    dataSection   = true
)
public class MapList
implements   DataItem
{
    //public int size; // uint, use mapItems.size().
    private ArrayList<MapItem> mapItems = new ArrayList<>(1);

    public static MapList readItem(DexDataInput input) {
        MapList mapList = new MapList();
        mapList.read(input);
        return mapList;
    }

    public static MapList empty() {
        return new MapList();
    }

    private MapList() {}

    public MapItem getMapItem(int type) {
        for (MapItem mapItem : mapItems) {
            if (mapItem.type == type) {
                return mapItem;
            }
        }
        return null;
    }

    public void updateMapItem(int type, int size, int offset) {
        MapItem mapItem = getMapItem(type);
        if (mapItem == null) {
            mapItem = MapItem.of(type);
            mapItems.add(mapItem);
        }

        mapItem.size   = size;
        mapItem.offset = offset;
    }

    @Override
    public void read(DexDataInput input) {
        input.skipAlignmentPadding(getDataAlignment());

        int size = input.readInt();
        mapItems.ensureCapacity(size);
        for (int i = 0; i < size; i++) {
            MapItem mapItem = MapItem.readItem(input);
            mapItems.add(mapItem);
        }
    }

    @Override
    public void write(DexDataOutput output) {
        output.writeAlignmentPadding(getDataAlignment());

        output.writeInt(mapItems.size());
        for (MapItem mapItem : mapItems) {
            mapItem.write(output);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("MapList: \n");
        for (MapItem mapItem : mapItems) {
            sb.append("  " + mapItem + "\n");
        }
        return sb.toString();
    }
}
