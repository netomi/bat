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

import com.github.netomi.bat.dexfile.ProtoID.Companion.of
import com.github.netomi.bat.dexfile.io.DexDataInput
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.function.Function

class ProtoIDTest : DexContentTest<ProtoID>() {
    override val testInstances: Array<ProtoID>
        get() = arrayOf(
                    of(1, 2, 3, 4, 5),
                    of(1, 2),
                    of(65535, 65535, 65535)
                )

    override val factoryMethod: Function<DexDataInput, ProtoID>
        get() = Function { input -> ProtoID.read(input) }

    @Test
    fun inputChecking() {
        assertThrows(IllegalArgumentException::class.java) { of(-1, 2, 3) }
        assertThrows(IllegalArgumentException::class.java) { of(1, -2, 3) }
        assertThrows(IllegalArgumentException::class.java) { of(1, 2, -3) }
    }

    @Test
    fun getter() {
        val data = testInstances
        assertEquals(1, data[0].shortyIndex)
        assertEquals(2, data[0].returnTypeIndex)
        val parameters = data[0].parameters
        assertEquals(3, parameters.size)
        assertEquals(3, parameters.getTypeIndex(0))
        assertEquals(4, parameters.getTypeIndex(1))
        assertEquals(5, parameters.getTypeIndex(2))
    }

    @Test
    fun equals() {
        val p1 = of(1, 2, 3)
        val p2 = of(1, 3, 4)
        val p3 = of(1, 2, 3)
        assertEquals(p1, p1)
        assertNotEquals(p1, p2)
        assertEquals(p1, p3)
    }
}