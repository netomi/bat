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
import com.github.netomi.bat.dexfile.io.DexDataOutput
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*
import java.util.function.Consumer
import java.util.function.Function

class EncodedMethodTest : DexContentTest<EncodedMethod>() {
    override val testInstances: Array<EncodedMethod>
        get() = arrayOf(
                    EncodedMethod.of(1, Visibility.PUBLIC, MethodModifier.FINAL),
                    EncodedMethod.of(2, Visibility.PRIVATE),
                    EncodedMethod.of(65535, Visibility.PACKAGE_PRIVATE, MethodModifier.SYNCHRONIZED, MethodModifier.SYNTHETIC)
                )

    override val factoryMethod: Function<DexDataInput, EncodedMethod>
        get() = Function { input -> EncodedMethod.read(input, 0) }

    override fun getWriteMethod(data: EncodedMethod): Consumer<DexDataOutput> {
        return Consumer { output -> data.write(output, 0) }
    }

    @Test
    fun inputChecking() {
        assertThrows(IllegalArgumentException::class.java) { EncodedMethod.of(-1, Visibility.PUBLIC, MethodModifier.FINAL) }
    }

    @Test
    fun getter() {
        val data = testInstances
        assertEquals(1, data[0].methodIndex)
        assertEquals(Visibility.PUBLIC, data[0].visibility)
        assertEquals(EnumSet.of(MethodModifier.FINAL), data[0].modifiers)
    }

    @Test
    fun equals() {
        val e1 = EncodedMethod.of(1, Visibility.PUBLIC, MethodModifier.FINAL)
        val e2 = EncodedMethod.of(2, Visibility.PRIVATE, MethodModifier.STATIC)
        val e3 = EncodedMethod.of(1, Visibility.PUBLIC, MethodModifier.FINAL)
        assertEquals(e1, e1)
        assertNotEquals(e1, e2)
        assertEquals(e1, e3)
    }
}