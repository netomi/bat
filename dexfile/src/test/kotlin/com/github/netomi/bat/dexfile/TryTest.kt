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
import java.util.function.Consumer
import java.util.function.Function

class TryTest : DexContentTest<Try>() {
    // can not use concrete EncodedCatchHandler instances for testing
    // special case that will be covered by testing the Code item.
    override val testInstances: Array<Try>
        get() =
            // can not use concrete EncodedCatchHandler instances for testing
            // special case that will be covered by testing the Code item.
            arrayOf(
                Try.of(0, 10,    EncodedCatchHandler.of(10, TypeAddrPair.of(1, 2))),
                Try.of(0, 65534, EncodedCatchHandler.of(10, TypeAddrPair.of(1, 2)))
            )

    override val factoryMethod: Function<DexDataInput, Try>
        get() = Function { input -> Try.read(input) }

    override fun getReadLinkedMethod(data: Try, oldData: Try): Consumer<DexDataInput> {
        // we just set the catch handler manually, as it is not serialized in the Try element itself.
        return Consumer { data.catchHandler = oldData.catchHandler }
    }

    @Test
    fun inputChecking() {
        assertThrows(IllegalArgumentException::class.java) { Try.of(-1, 100, EncodedCatchHandler.of(10, TypeAddrPair.of(1, 2))) }
        assertThrows(IllegalArgumentException::class.java) { Try.of(1, -1, EncodedCatchHandler.of(10, TypeAddrPair.of(1, 2))) }
        assertThrows(IllegalArgumentException::class.java) { Try.of(100000, 100, EncodedCatchHandler.of(10, TypeAddrPair.of(1, 2))) }
        assertThrows(IllegalArgumentException::class.java) { Try.of(1, 100000, EncodedCatchHandler.of(10, TypeAddrPair.of(1, 2))) }
        assertThrows(IllegalArgumentException::class.java) { Try.of(100, 10, EncodedCatchHandler.of(10, TypeAddrPair.of(1, 2))) }
    }

    @Test
    fun getter() {
        val data = testInstances
        assertEquals(0, data[0].startAddr)
        assertEquals(11, data[0].insnCount)
        assertEquals(10, data[0].endAddr)
    }

    @Test
    fun equals() {
        val t1 = Try.of(1, 10, EncodedCatchHandler.of(10, TypeAddrPair.of(1, 2)))
        val t2 = Try.of(2, 10, EncodedCatchHandler.of(10, TypeAddrPair.of(1, 2)))
        val t3 = Try.of(2, 11, EncodedCatchHandler.of(10, TypeAddrPair.of(1, 2)))
        val t4 = Try.of(1, 10, EncodedCatchHandler.of(10, TypeAddrPair.of(1, 2)))
        assertEquals(t1, t1)
        assertNotEquals(t1, t2)
        assertNotEquals(t2, t3)
        assertEquals(t1, t4)
    }
}