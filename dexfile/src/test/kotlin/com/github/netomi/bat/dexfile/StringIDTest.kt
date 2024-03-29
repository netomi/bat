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

import com.github.netomi.bat.dexfile.StringID.Companion.of
import com.github.netomi.bat.dexfile.io.DexDataInput
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import java.util.function.Function

class StringIDTest : DexContentTest<StringID>() {
    override val testInstances: Array<StringID>
        get() = arrayOf(
                    of("Terence Hill"),
                    of("Jean-Claude Van Damme"),
                    of("Bud Spencer")
                )

    override val factoryMethod: Function<DexDataInput, StringID>
        get() = Function { input -> StringID.read(input) }

    @Test
    fun getter() {
        val data = testInstances
        assertEquals("Terence Hill", data[0].stringValue)
        assertEquals("Bud Spencer", data[2].stringValue)
    }

    @Test
    fun equals() {
        val t1 = of("Terence Hill")
        val t2 = of("Bud Spencer")
        val t3 = of("Terence Hill")
        assertEquals(t1, t1)
        assertNotEquals(t1, t2)
        assertEquals(t1, t3)
    }
}