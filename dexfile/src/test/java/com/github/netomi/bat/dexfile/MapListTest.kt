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
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.function.Function

class MapListTest : DexContentTest<MapList>() {
    override fun getTestInstances(): Array<MapList> {
        val l1 = MapList.empty()
        l1.updateMapItem(1, 2, 0)
        val l2 = MapList.empty()
        l2.updateMapItem(2, 3, 0)
        return arrayOf(l1, l2)
    }

    override fun getFactoryMethod(): Function<DexDataInput, MapList> {
        return Function<DexDataInput, MapList> { input -> MapList.readMapList(input) }
    }

    @Test
    fun getter() {
        val data = testInstances
        Assertions.assertEquals(1, data[0].getMapItem(0).getType())
        Assertions.assertEquals(2, data[0].getMapItem(0).getSize())
    }

    @Test
    fun equals() {
        val l1 = MapList.empty()
        l1.updateMapItem(1, 2, 0)
        val l2 = MapList.empty()
        l2.updateMapItem(2, 2, 0)
        val l3 = MapList.empty()
        l3.updateMapItem(1, 2, 5)
        Assertions.assertEquals(l1, l1)
        Assertions.assertNotEquals(l1, l2)
        Assertions.assertEquals(l1, l3)
    }
}