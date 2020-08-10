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

import java.util.Objects;

public class MapItem
extends      DexContent
{
    int type;     // ushort
    //int unused; // ushort
    int size;     // uint
    int offset;   // uint

    public static MapItem readContent(DexDataInput input) {
        MapItem mapItem = new MapItem();
        mapItem.read(input);
        return mapItem;
    }

    public static MapItem of(int type) {
        return new MapItem(type);
    }

    private MapItem() {}

    private MapItem(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public int getSize() {
        return size;
    }

    public int getOffset() {
        return offset;
    }

    @Override
    protected void read(DexDataInput input) {
        type   = input.readUnsignedShort();
        input.readUnsignedShort();
        size   = input.readInt();
        offset = input.readInt();
    }

    @Override
    protected void write(DexDataOutput output) {
        output.writeUnsignedShort(type);
        output.writeUnsignedShort(0x0);
        output.writeInt(size);
        output.writeInt(offset);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MapItem other = (MapItem) o;
        return type   == other.type &&
               size   == other.size;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, size);
    }

    @Override
    public String toString() {
        return String.format("MapItem[type=0x%04x,size=%5d,offset=%7d]", type, size, offset);
    }
}
