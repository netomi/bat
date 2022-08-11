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

import com.github.netomi.bat.dexfile.CallSite.Companion.of
import com.github.netomi.bat.dexfile.io.DexDataInput
import com.github.netomi.bat.dexfile.value.EncodedMethodHandleValue
import com.github.netomi.bat.dexfile.value.EncodedMethodTypeValue
import com.github.netomi.bat.dexfile.value.EncodedStringValue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import java.util.function.Function

class CallSiteTest : DexContentTest<CallSite>() {
    override val testInstances: Array<CallSite>
        get() = arrayOf(
                    of(1, 2, 3),
                    of(65535, 65535, 65535)
                )

    override val factoryMethod: Function<DexDataInput, CallSite>
        get() = Function { input -> CallSite.read(input) }

    @Test
    fun getter() {
        val data = testInstances
        assertEquals(EncodedMethodHandleValue.of(1), data[0].methodHandle)
        assertEquals(EncodedStringValue.of(2), data[0].methodName)
        assertEquals(EncodedMethodTypeValue.of(3), data[0].methodType)
    }

    @Test
    fun equals() {
        val c1 = of(1, 2, 3)
        val c2 = of(2, 3, 4)
        val c3 = of(1, 2, 3)
        assertEquals(c1, c1)
        assertNotEquals(c1, c2)
        assertEquals(c1, c3)
    }
}