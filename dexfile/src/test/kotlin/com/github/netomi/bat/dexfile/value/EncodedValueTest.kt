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
package com.github.netomi.bat.dexfile.value

import com.github.netomi.bat.dexfile.io.ByteBufferBackedDexDataOutput
import com.github.netomi.bat.dexfile.io.DexDataInput
import com.github.netomi.bat.dexfile.value.EncodedValue.Companion.read
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.IOException

abstract class EncodedValueTest<T : EncodedValue> {

    protected abstract val testInstance: Array<T>

    @Test
    fun readWrite() {
        val testData = testInstance
        for (data in testData) {
            serializeAndDeserialize(data)
        }
    }

    private fun serializeAndDeserialize(value: T) {
        try {
            val output = ByteBufferBackedDexDataOutput(1024)
            value.write(output)
            val input = DexDataInput(ByteArrayInputStream(output.toArray()))
            val result = read(input)
            assertEquals(value, result)
        } catch (ioe: IOException) {
            Assertions.fail<Any>(ioe)
        }
    }
}