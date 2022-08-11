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

import com.github.netomi.bat.dexfile.TypeList.Companion.empty
import com.github.netomi.bat.dexfile.TypeList.Companion.of
import com.github.netomi.bat.dexfile.io.DexDataInput
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import java.util.function.Function

class TypeListTest : DexContentTest<TypeList>() {
    override val testInstances: Array<TypeList>
        get() = arrayOf(
            empty(),
            of(1, 2, 3),
            of(6, 5, 4, 3, 2, 1),
            of(65535)
        )

    override val factoryMethod: Function<DexDataInput, TypeList>
        get() = Function { input -> TypeList.read(input) }

    @Test
    fun getter() {
        val data = testInstances
        assertEquals(0, data[0].typeCount)
        assertEquals(3, data[1].typeCount)
        assertEquals(6, data[2].typeCount)
    }

    @Test
    fun equals() {
        val l1 = empty()
        val l2 = empty()
        assertEquals(l1, l2)
        l1.addType(1)
        assertNotEquals(l1, l2)
        l2.addType(1)
        assertEquals(l1, l2)
        l1.addType(2)
        assertNotEquals(l1, l2)
    }
}