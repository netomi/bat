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

package com.github.netomi.bat.smali

import com.github.netomi.bat.dexfile.DexFile
import com.github.netomi.bat.dexfile.DexFormat
import com.github.netomi.bat.util.Arrays
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.file.Files
import java.nio.file.Paths

class AssemblerTest {

    @ParameterizedTest
    @MethodSource("smaliFiles")
    fun batchDisassemble(testFile: String) {
        javaClass.getResourceAsStream("/smali/$testFile")!!.use { `is` ->
            println("assembling $testFile...")

            val baos = ByteArrayOutputStream()
            `is`.copyTo(baos)

            val dexFile  = DexFile.of(DexFormat.FORMAT_035)
            val classDef = Assembler(dexFile).assemble(ByteArrayInputStream(baos.toByteArray()))

            val className = classDef.getClassName(dexFile)

            val outputStreamFactory = TestOutputStreamFactory()
            val disassembler = Disassembler(outputStreamFactory)
            dexFile.classDefAccept(className, disassembler)

            val expectedBytes = baos.toByteArray()
            val actualBytes   = outputStreamFactory.getOutputStream(className)!!.toByteArray()

            // testing purposes only.
            if (!Arrays.equals(expectedBytes, actualBytes, expectedBytes.size)) {
                Files.write(Paths.get("${className}_expected.smali"), expectedBytes)
                Files.write(Paths.get("${className}_actual.smali"), actualBytes)
            }

            assertArrayEquals(expectedBytes, actualBytes)
        }
    }

    companion object {
        @JvmStatic
        fun smaliFiles() = listOf(
            Arguments.of("Fields/StaticFields.smali")
        )
    }

}