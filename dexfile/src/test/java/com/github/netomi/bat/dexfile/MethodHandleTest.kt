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

import com.github.netomi.bat.dexfile.MethodHandle.Companion.of
import com.github.netomi.bat.dexfile.MethodID.Companion.of
import com.github.netomi.bat.dexfile.io.DexDataInput
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.function.Function

class MethodHandleTest : DexContentTest<MethodHandle>() {
    override fun getTestInstances(): Array<MethodHandle> {
        return arrayOf(
            of(MethodHandleType.INSTANCE_GET, 1),
            of(MethodHandleType.INSTANCE_PUT, 65535)
        )
    }

    override fun getFactoryMethod(): Function<DexDataInput, MethodHandle> {
        return Function { input -> MethodHandle.readContent(input) }
    }

    @Test
    fun inputChecking() {
        assertThrows(IllegalArgumentException::class.java) { of(MethodHandleType.INSTANCE_PUT, -1) }
    }

    @Test
    fun getter() {
        val data = testInstances
        assertEquals(MethodHandleType.INSTANCE_GET, data[0].methodHandleType)
        assertEquals(3, data[0].methodHandleTypeValue)
        assertEquals(1, data[0].fieldOrMethodId)
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