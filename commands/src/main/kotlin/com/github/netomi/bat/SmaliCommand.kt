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

import com.github.netomi.bat.dexfile.DexFile
import com.github.netomi.bat.dexfile.DexFormat
import com.github.netomi.bat.dexfile.io.DexFileWriter
import com.github.netomi.bat.smali.Assembler
import com.github.netomi.bat.smali.SmaliAssembleException
import picocli.CommandLine
import java.io.File
import java.nio.file.Path
import kotlin.io.path.name
import kotlin.io.path.pathString
import kotlin.io.path.relativeTo

/**
 * Command-line tool to assemble dex files from smali input files.
 */
@CommandLine.Command(
    name                 = "bat-smali",
    description          = ["assembles smali files to dex format."],
    parameterListHeading = "%nParameters:%n",
    optionListHeading    = "%nOptions:%n")
class SmaliCommand : Runnable {

    @CommandLine.Parameters(index = "0", arity = "1..*", paramLabel = "inputfile", description = ["input file / directory to process (*.smali)"])
    private lateinit var inputFiles: Array<File>

    @CommandLine.Option(names = ["-o"], arity = "1", defaultValue = "out.dex", description = ["output file (default=out.dex)"])
    private lateinit var outputFile: File

    @CommandLine.Option(names = ["-a"], defaultValue = "15", description = ["api level (default=15)"])
    private var apiLevel: Int = 0

    @CommandLine.Option(names = ["-v"], description = ["verbose output"])
    private var verbose: Boolean = false

    override fun run() {
        val format = DexFormat.forApiLevel(apiLevel)
        val dexFile = DexFile.of(format)

        printVerbose("Using format '${format.version}' for generated dex file '${outputFile.name}'")

        val startTime = System.nanoTime()

        inputFiles.forEach { file ->
            val filePath = file.toPath()

            try {
                if (file.isDirectory) {
                    printVerbose("Assembling directory '${file.name}' into file ${outputFile.name} ...")
                    val assembledClasses = Assembler(dexFile).assemble(filePath, ::assembleFile)
                    printVerbose("Assembled ${assembledClasses.size} class(es).")
                } else {
                    printVerbose("Assembling file '${file.name}' into file ${outputFile.name} ...")
                    Assembler(dexFile).assemble(filePath, ::assembleFile)
                    printVerbose("Assembled 1 class.")
                }
            } catch (exception: SmaliAssembleException) {
                System.err.println(exception.message)
                System.err.println("aborting.")
            }
        }

        val endTime = System.nanoTime()
        printVerbose("done, took ${(endTime - startTime) / 1e6} ms.")

        DexFileWriter(outputFile.outputStream()).visitDexFile(dexFile)
    }

    private fun assembleFile(baseDirectory: Path, file: Path) {
        val relativePath = file.relativeTo(baseDirectory)
        printVerbose("  assembling file '${relativePath.pathString}'")
    }
    private fun printVerbose(text: String) {
        if (verbose) {
            println(text)
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val cmdLine = CommandLine(SmaliCommand())
            cmdLine.execute(*args)
        }
    }
}