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

import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class MapListTest
extends      DexContentTest<MapList>
{
    @Override
    public MapList[] getTestInstances() {
        MapList l1 = MapList.empty();
        l1.updateMapItem(1, 2, 0);

        MapList l2 = MapList.empty();
        l2.updateMapItem(2, 3, 0);

        return new MapList[] { l1, l2 };
    }

    @Override
    public Function<DexDataInput, MapList> getFactoryMethod() {
        return MapList::readContent;
    }

    @Test
    public void getter() {
        MapList[] data = getTestInstances();

        assertEquals(1, data[0].getMapItem(0).getType());
        assertEquals(2, data[0].getMapItem(0).getSize());
    }

    @Test
    public void equals() {
        MapList l1 = MapList.empty();
        l1.updateMapItem(1, 2, 0);

        MapList l2 = MapList.empty();
        l2.updateMapItem(2, 2, 0);

        MapList l3 = MapList.empty();
        l3.updateMapItem(1, 2, 5);

        assertEquals(l1, l1);
        assertNotEquals(l1, l2);
        assertEquals(l1, l3);
    }
}
