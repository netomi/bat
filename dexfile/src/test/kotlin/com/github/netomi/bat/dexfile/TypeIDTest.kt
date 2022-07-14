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
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.function.Function

class TypeIDTest : DexContentTest<TypeID>() {
    override val testInstances: Array<TypeID>
        get() = arrayOf(
                    TypeID.of(10),
                    TypeID.of(20),
                    TypeID.of(65535)
                )

    override val factoryMethod: Function<DexDataInput, TypeID>
        get() = Function { input -> TypeID.readContent(input) }

    @Test
    fun inputChecking() {
        assertThrows(IllegalArgumentException::class.java) { TypeID.of(-1) }
    }

    @Test
    fun getter() {
        val data = testInstances
        assertEquals(10, data[0].descriptorIndex)
        assertEquals(20, data[1].descriptorIndex)
    }

    @Test
    fun equals() {
        val t1 = TypeID.of(1)
        val t2 = TypeID.of(2)
        val t3 = TypeID.of(1)
        assertEquals(t1, t1)
        assertNotEquals(t1, t2)
        assertEquals(t1, t3)
    }
}