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
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

public class MapItemTest
extends      DexContentTest<MapItem>
{
    @Override
    public MapItem[] getTestInstances() {
        return new MapItem[] {
            MapItem.of(1, 2),
            MapItem.of(2, 0)
        };
    }

    @Override
    public Function<DexDataInput, MapItem> getFactoryMethod() {
        return MapItem::readContent;
    }

    @Test
    public void getter() {
        MapItem[] data = getTestInstances();

        assertEquals(1, data[0].getType());
        assertEquals(2, data[0].getSize());
    }

    @Test
    public void equals() {
        MapItem m1 = MapItem.of(1, 2);
        MapItem m2 = MapItem.of(1, 3);
        MapItem m3 = MapItem.of(1, 2);

        assertEquals(m1, m1);
        assertNotEquals(m1, m2);
        assertEquals(m1, m3);
    }
}
