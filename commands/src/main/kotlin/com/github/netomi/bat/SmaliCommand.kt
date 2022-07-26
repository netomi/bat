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
import picocli.CommandLine
import java.io.File
import java.nio.file.Path
import kotlin.io.path.pathString

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

        printVerbose("Using format ${format.version} for generated dex file '${outputFile.name}'")

        inputFiles.forEach {
            if (it.isDirectory) {
                printVerbose("Assembling directory '${it.name}' into file ${outputFile.name} ...")
                val assembledClasses = Assembler(dexFile).assemble(it.toPath(), ::assembleFile)
                printVerbose("Assembled ${assembledClasses.size} class(es).")
            } else {
                printVerbose("Assembling file '${it.name}' into file ${outputFile.name} ...")
                Assembler(dexFile).assemble(it.toPath(), ::assembleFile)
                printVerbose("Assembled 1 class.")
            }
        }

        DexFileWriter(outputFile.outputStream()).visitDexFile(dexFile)
    }

    private fun assembleFile(file: Path) {
        printVerbose("  assembling file ${file.pathString}")
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