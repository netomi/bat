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
import com.github.netomi.bat.dexfile.io.DexFileWriter
import org.junit.jupiter.api.Assumptions
import java.io.File
import java.io.IOException
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import kotlin.io.path.absolutePathString
import kotlin.io.path.outputStream
import kotlin.test.Test

class IntegrationTest {

    @Test
    fun runUnitTest() {
        // only execute the test if the system property has been set
        val androidRuntimesVariable = System.getProperty("ANDROID_RUNTIMES")
        Assumptions.assumeTrue(androidRuntimesVariable != null)

        val resource = javaClass.getResource("/junit-tests/TestSuite.smali")
        val resourcePath = File(resource.file).parentFile.toPath()

        val dexFile  = DexFile.of(DexFormat.FORMAT_035)
        Assembler(dexFile).assemble(resourcePath)

        val outputPath = Path.of(resourcePath.absolutePathString(), "tests.dex")
        outputPath.outputStream().use {
            DexFileWriter(it).visitDexFile(dexFile)
        }

        val result =
            "./rundalvikvm 7.1.2 -c -cp junit.zip:tests.dex org.junit.runner.JUnitCore AllTests"
                .runCommand(resourcePath.toFile(), mapOf(Pair("ANDROID_RUNTIMES", androidRuntimesVariable)))

        println(result!!)
    }

    private fun String.runCommand(workingDir: File, envVariables: Map<String, String> = emptyMap()): String? {
        return try {
            val parts = this.split("\\s".toRegex())
            val builder =
                ProcessBuilder(*parts.toTypedArray())
                    .directory(workingDir)
                    .redirectOutput(ProcessBuilder.Redirect.PIPE)
                    .redirectErrorStream(true)

            val env = builder.environment()
            env.putAll(envVariables)

            val proc = builder.start()

            proc.waitFor(5, TimeUnit.SECONDS)
            proc.inputStream.bufferedReader().readText()
        } catch(e: IOException) {
            e.printStackTrace()
            null
        }
    }
}