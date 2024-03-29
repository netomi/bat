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
package com.github.netomi.bat.dexdump

import com.github.netomi.bat.dexfile.DexFile
import com.github.netomi.bat.dexfile.io.DexFileReader
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.fail
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths

class FullDumpTest {

    @ParameterizedTest
    @MethodSource("testFiles")
    fun fullDexDump(testFile: String) {
        println("checking $testFile...")

        val expectedFile = testFile.replace(".dex", ".txt")
        try {
            javaClass.getResourceAsStream("/dex/$testFile")!!.use { `is` ->
                val reader = DexFileReader(`is`)

                val dexFile = DexFile.empty()
                reader.visitDexFile(dexFile)

                val baos = ByteArrayOutputStream()
                val printer = DexDumpPrinter(baos, true, true, true)

                dexFile.accept(printer)

                val actualBytes   = baos.toByteArray()
                val expectedBytes = javaClass.getResourceAsStream("/dex/$expectedFile")!!.readAllBytes()

                // testing purposes only.
                if (!expectedBytes.contentEquals(actualBytes)) {
                    Files.write(Paths.get("${testFile}_expected.txt"), expectedBytes)
                    Files.write(Paths.get("${testFile}_actual.txt"), actualBytes)
                }

                assertArrayEquals(expectedBytes, actualBytes, "testFile $testFile differs")
            }
        } catch (ex: IOException) {
            fail(ex)
        }
    }

    companion object {
        @JvmStatic
        fun testFiles() = listOf(
            Arguments.of("all.dex"),
            Arguments.of("bytecodes.dex"),
            Arguments.of("checkers.dex"),
            Arguments.of("const-method-handle.dex"),
            Arguments.of("invoke-custom.dex"),
            Arguments.of("invoke-polymorphic.dex"),
            Arguments.of("staticfields.dex"),
            Arguments.of("values.dex")
        )
    }
}