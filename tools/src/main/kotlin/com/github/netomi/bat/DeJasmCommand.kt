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
import com.github.netomi.bat.io.*
import com.github.netomi.bat.jasm.Disassembler
import com.github.netomi.bat.util.fileNameMatcher
import picocli.CommandLine
import java.lang.Runnable
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.name
import kotlin.io.path.notExists

/**
 * Command-line tool to disassemble class files in jasm format.
 */
@CommandLine.Command(
    name                 = "bat-dejasm",
    description          = ["disassembles class files."],
    parameterListHeading = "%nParameters:%n",
    optionListHeading    = "%nOptions:%n")
class DeJasmCommand : Runnable {

    @CommandLine.Parameters(index = "0", arity = "1", paramLabel = "inputfile", description = ["input file to process (*.[class|jar])"])
    private lateinit var inputPath: Path

    @CommandLine.Option(names = ["-o"], arity = "1", description = ["output directory"])
    private var outputPath: Path? = null

    @CommandLine.Option(names = ["-v"], description = ["verbose output"])
    private var verbose: Boolean = false

    override fun run() {
        printVerbose("disassembling '${inputPath.name}' into path '$outputPath' ...")

        val startTime = System.nanoTime()

        val outputStreamFactory = if (outputPath != null) {
            if (outputPath!!.notExists()) {
                outputPath!!.createDirectories()
            }
            FileOutputStreamFactory(outputPath!!, "jasm")
        } else {
            ConsoleOutputStreamFactory(System.out)
        }

        val processClassFile = { entry: DataEntry ->
            entry.getInputStream().use {
                printVerbose("  de-assembling file '${entry.name}'")

                val classFile = ClassFile.empty()
                val reader    = ClassFileReader(it)
                reader.visitClassFile(classFile)

                classFile.accept(Disassembler(outputStreamFactory))
            }
        }

        val inputSource = PathInputSource.of(inputPath)
        inputSource.pumpDataEntries(
            unwrapArchives(
            filterDataEntriesBy(fileNameMatcher("**.class"),
            processClassFile)))

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
            val cmdLine = CommandLine(DeJasmCommand())
            cmdLine.execute(*args)
        }
    }
}