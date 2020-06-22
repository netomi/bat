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
package org.netomi.bat.classfile.constant

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream

abstract class ConstantBaseTest {

    abstract fun createEmptyConstant(): Constant
    abstract fun createConstants(): List<Constant>

    @Test
    fun serialization() {
        for (constant in createConstants()) {
            val content      = serializeConstant(constant)
            val deserialized = deserializeConstant(content)

            assertEquals(constant, deserialized)
        }
    }

    private fun serializeConstant(constant: Constant): ByteArray {
        val baos = ByteArrayOutputStream()

        DataOutputStream(baos).use {
            constant.write(it)
        }

        return baos.toByteArray()
    }

    private fun deserializeConstant(content: ByteArray): Constant {
        DataInputStream(ByteArrayInputStream(content)).use {
            return Constant.read(it)
        }
    }
}