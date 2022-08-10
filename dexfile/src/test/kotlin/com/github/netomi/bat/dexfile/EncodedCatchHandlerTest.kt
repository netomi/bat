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

import com.github.netomi.bat.dexfile.EncodedCatchHandler.Companion.of
import com.github.netomi.bat.dexfile.io.DexDataInput
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import java.util.function.Function

class EncodedCatchHandlerTest : DexContentTest<EncodedCatchHandler>() {
    override val testInstances: Array<EncodedCatchHandler>
        get() = arrayOf(
                    of(1),
                    of(0, TypeAddrPair.of(1, 2), TypeAddrPair.of(3, 4))
                )

    override val factoryMethod: Function<DexDataInput, EncodedCatchHandler>
        get() = Function { input -> EncodedCatchHandler.readContent(input) }

    @Test
    fun equals() {
        val c1 = of(1, TypeAddrPair.of(1, 2))
        val c2 = of(1, TypeAddrPair.of(3, 4))
        val c3 = of(1, TypeAddrPair.of(1, 2))
        assertEquals(c1, c1)
        assertNotEquals(c1, c2)
        assertEquals(c1, c3)
    }
}