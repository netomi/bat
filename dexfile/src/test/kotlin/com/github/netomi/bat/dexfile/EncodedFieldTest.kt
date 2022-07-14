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

import com.github.netomi.bat.dexfile.EncodedField.Companion.of
import com.github.netomi.bat.dexfile.EncodedField.Companion.readContent
import com.github.netomi.bat.dexfile.io.DexDataInput
import com.github.netomi.bat.dexfile.io.DexDataOutput
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*
import java.util.function.Consumer
import java.util.function.Function

class EncodedFieldTest : DexContentTest<EncodedField>() {
    override val testInstances: Array<EncodedField>
        get() = arrayOf(
                    of(1, Visibility.PUBLIC, FieldModifier.FINAL),
                    of(2, Visibility.PRIVATE),
                    of(65535, Visibility.PACKAGE_PRIVATE, FieldModifier.VOLATILE, FieldModifier.SYNTHETIC)
                )

    override val factoryMethod: Function<DexDataInput, EncodedField>
        get() = Function { input -> readContent(input, 0) }

    override fun getWriteMethod(data: EncodedField): Consumer<DexDataOutput> {
        return Consumer { output -> data.write(output, 0) }
    }

    @Test
    fun inputChecking() {
        assertThrows(IllegalArgumentException::class.java) { of(-1, Visibility.PUBLIC, FieldModifier.FINAL) }
    }

    @Test
    fun getter() {
        val data = testInstances
        assertEquals(1, data[0].fieldIndex)
        assertEquals(Visibility.PUBLIC, data[0].visibility)
        assertEquals(EnumSet.of(FieldModifier.FINAL), data[0].modifiers)
    }

    @Test
    fun equals() {
        val e1 = of(1, Visibility.PUBLIC, FieldModifier.FINAL)
        val e2 = of(2, Visibility.PRIVATE, FieldModifier.STATIC)
        val e3 = of(1, Visibility.PUBLIC, FieldModifier.FINAL)
        assertEquals(e1, e1)
        assertNotEquals(e1, e2)
        assertEquals(e1, e3)
    }
}