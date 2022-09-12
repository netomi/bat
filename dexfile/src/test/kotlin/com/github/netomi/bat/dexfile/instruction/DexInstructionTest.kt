/*
 *  Copyright (c) 2020-2022 Thomas Neidhart.
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

package com.github.netomi.bat.dexfile.instruction

import com.github.netomi.bat.dexfile.instruction.editor.InstructionWriter
import org.junit.jupiter.api.Test
import java.io.IOException
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

abstract class DexInstructionTest<T: DexInstruction> {
    protected abstract val testInstances: Array<T>

    protected abstract val failInstances: Array<() -> T>

    abstract fun equals(instructionA: T, instructionB: T): Boolean

    @Test
    fun readWrite() {
        val testData = testInstances
        for (data in testData) {
            serializeAndDeserialize(data)
        }
    }

    @Test
    fun illegal() {
        val testData = failInstances
        for (data in testData) {
            assertFails("instruction '$data' did not fail as expected") { data.invoke() }
        }

    }

    @Suppress("UNCHECKED_CAST")
    private fun serializeAndDeserialize(value: T) {
        try {
            val writer = InstructionWriter()
            value.write(writer, writer.nextWriteOffset)
            val instructions = writer.getInstructionArray()
            val result = DexInstruction.create(instructions, 0) as T
            assertTrue(equals(value, result), "instructions not equal '$value' != '$result'")
        } catch (ioe: IOException) {
            fail("deserialized value not equal", ioe)
        }
    }
}