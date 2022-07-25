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

import com.github.netomi.bat.dexfile.MapItem.Companion.of
import com.github.netomi.bat.dexfile.io.DexDataInput
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.function.Function

class MapItemTest : DexContentTest<MapItem>() {
    override val testInstances: Array<MapItem>
        get() = arrayOf(
                    of(1, 2),
                    of(2, 0)
                )

    override val factoryMethod: Function<DexDataInput, MapItem>
        get() = Function { input -> MapItem.readContent(input) }

    @Test
    fun setter() {
        assertThrows<IllegalArgumentException> { of(0, -1) }
        assertThrows<IllegalArgumentException> {
            val mapItem = of(0, 0)
            mapItem.offset = -1
        }
    }

    @Test
    fun getter() {
        val data = testInstances
        assertEquals(1, data[0].type)
        assertEquals(2, data[0].size)
    }

    @Test
    fun equals() {
        val m1 = of(1, 2)
        val m2 = of(1, 3)
        val m3 = of(1, 2)
        assertEquals(m1, m1)
        assertNotEquals(m1, m2)
        assertEquals(m1, m3)
    }
}