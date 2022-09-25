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

import com.github.netomi.bat.io.DataEntry
import com.github.netomi.bat.io.FileOutputStreamFactory
import com.github.netomi.bat.io.PathInputSource
import com.github.netomi.bat.io.filterDataEntriesBy
import com.github.netomi.bat.jasm.Assembler
import com.github.netomi.bat.smali.SmaliAssembleException
import com.github.netomi.bat.util.fileNameMatcher
import picocli.CommandLine
import java.io.PrintWriter
import java.nio.file.Path
import kotlin.io.path.name

/**
 * Command-line tool to assemble class files from jasm input files.
 */
@CommandLine.Command(
    name                 = "bat-jasm",
    description          = ["assembles jasm files to class format."],
    parameterListHeading = "%nParameters:%n",
    optionListHeading    = "%nOptions:%n")
class JasmCommand : Runnable {

    @CommandLine.Parameters(index = "0", arity = "1..*", paramLabel = "inputfile", description = ["input file / directory to process (*.jasm)"])
    private lateinit var inputFiles: Array<Path>

    @CommandLine.Option(names = ["-o"], arity = "1", defaultValue = "out", description = ["output (default=out)"])
    private lateinit var outputPath: Path

    @CommandLine.Option(names = ["-l", "--lenient"], description = ["enables lenient mode"])
    private var lenientMode: Boolean = false

    @CommandLine.Option(names = ["-v", "--verbose"], description = ["verbose output"])
    private var verbose: Boolean = false

    override fun run() {
        val startTime = System.nanoTime()

        val warningPrinter = if (verbose) PrintWriter(System.out, true) else PrintWriter(System.err, true)

        val outputStreamFactory = FileOutputStreamFactory(outputPath, "class")
        val assembler = Assembler(outputStreamFactory, lenientMode, warningPrinter)

        var assembledClasses = 0

        val processJasmFile = { entry: DataEntry ->
            try {
                entry.getInputStream().use {
                    printVerbose("  assembling file '${entry.name}'")
                    assembledClasses += assembler.assemble(it, entry.name).size
                }
            } catch (exception: SmaliAssembleException) {
                warningPrinter.println("error: ${exception.message}")
                warningPrinter.println("abort assembling.")
            }
        }

        inputFiles.forEach { inputPath ->
            printVerbose("assembling path '${inputPath.name}' into path '${outputPath.name}' ...")

            val inputSource = PathInputSource.of(inputPath, true)
            inputSource.pumpDataEntries(
                filterDataEntriesBy(fileNameMatcher("**.jasm"),
                processJasmFile))
        }

        printVerbose("assembled $assembledClasses class(es).")

        val endTime = System.nanoTime()
        printVerbose("done, took ${(endTime - startTime) / 1e6} ms.")
    }

    private fun printVerbose(text: String) {
        if (verbose) {
            println(text)
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val cmdLine = CommandLine(JasmCommand())
            cmdLine.execute(*args)
        }
    }
}