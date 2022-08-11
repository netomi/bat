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

import com.github.netomi.bat.dexfile.MethodID.Companion.of
import com.github.netomi.bat.dexfile.io.DexDataInput
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.function.Function

class MethodIDTest : DexContentTest<MethodID>() {
    override val testInstances: Array<MethodID>
        get() = arrayOf(
                    of(1, 2, 3),
                    of(65535, 65535, 65535)
                )

    override val factoryMethod: Function<DexDataInput, MethodID>
        get() = Function { input -> MethodID.read(input) }

    @Test
    fun inputChecking() {
        assertThrows(IllegalArgumentException::class.java) { of(-1, 2, 3) }
        assertThrows(IllegalArgumentException::class.java) { of(1, -2, 3) }
        assertThrows(IllegalArgumentException::class.java) { of(1, 2, -3) }
    }

    @Test
    fun getter() {
        val data = testInstances
        assertEquals(1, data[0].classIndex)
        assertEquals(2, data[0].nameIndex)
        assertEquals(3, data[0].protoIndex)
    }

    @Test
    fun equals() {
        val m1 = of(1, 2, 3)
        val m2 = of(1, 3, 4)
        val m3 = of(1, 2, 3)
        assertEquals(m1, m1)
        assertNotEquals(m1, m2)
        assertEquals(m1, m3)
    }
}