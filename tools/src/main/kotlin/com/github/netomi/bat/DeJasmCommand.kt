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
package com.github.netomi.bat

import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.io.ClassFileReader
import com.github.netomi.bat.io.ConsoleOutputStreamFactory
import com.github.netomi.bat.io.FileOutputStreamFactory
import com.github.netomi.bat.jasm.Disassembler
import picocli.CommandLine
import java.io.File
import java.io.FileInputStream
import java.lang.Runnable
import java.nio.file.Files
import kotlin.io.path.exists

/**
 * Command-line tool to disassemble class files in jasm format.
 */
@CommandLine.Command(
    name                 = "bat-dejasm",
    description          = ["disassembles class files."],
    parameterListHeading = "%nParameters:%n",
    optionListHeading    = "%nOptions:%n")
class DeJasmCommand : Runnable {

    @CommandLine.Parameters(index = "0", arity = "1", paramLabel = "inputfile", description = ["input file to process (*.class)"])
    private lateinit var inputFile: File

    @CommandLine.Option(names = ["-o"], arity = "1", defaultValue = "out", description = ["output directory"])
    private lateinit var outputFile: File

    @CommandLine.Option(names = ["-v"], description = ["verbose output"])
    private var verbose: Boolean = false

    override fun run() {
        FileInputStream(inputFile).use { `is` ->
            val outputPath = outputFile.toPath()
            if (!outputPath.exists()) {
                Files.createDirectories(outputPath)
            }

            val classFile = ClassFile.empty()
            val reader    = ClassFileReader(`is`)

            printVerbose("Disassembling '${inputFile.name}' into directory $outputPath ...")
            reader.visitClassFile(classFile)

            val startTime = System.nanoTime()

            //classFile.accept(Disassembler(FileOutputStreamFactory(outputPath, "jasm")))

            classFile.accept(Disassembler(ConsoleOutputStreamFactory(System.out)))

            val endTime = System.nanoTime()
            printVerbose("done, took ${(endTime - startTime) / 1e6} ms.")
        }
    }

    private fun printVerbose(text: String) {
        if (verbose) {
            println(text)
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val cmdLine = CommandLine(DeJasmCommand())
            cmdLine.execute(*args)
        }
    }
}