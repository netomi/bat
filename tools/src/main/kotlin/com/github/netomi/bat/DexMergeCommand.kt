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
import com.github.netomi.bat.dexfile.editor.ClassDefAdder
import com.github.netomi.bat.dexfile.io.DexFileReader
import com.github.netomi.bat.dexfile.io.DexFileWriter
import picocli.CommandLine
import java.io.File
import java.io.FileInputStream

/**
 * Command-line tool to merge multiple dex files.
 */
@CommandLine.Command(
    name                 = "bat-dexmerger",
    description          = ["merges multiple dex files."],
    parameterListHeading = "%nParameters:%n",
    optionListHeading    = "%nOptions:%n")
class DexMergeCommand : Runnable {

    @CommandLine.Parameters(index = "0", arity = "1..*", paramLabel = "inputfile", description = ["input file(s) to process (*.dex)"])
    private lateinit var inputFiles: Array<File>

    @CommandLine.Option(names = ["-o"], arity = "1", defaultValue = "out.dex", description = ["output file (default=out.dex)"])
    private lateinit var outputFile: File

    @CommandLine.Option(names = ["-a"], defaultValue = "15", description = ["api level (default=15)"])
    private var apiLevel: Int = 0

    @CommandLine.Option(names = ["-v", "--verbose"], description = ["verbose output"])
    private var verbose: Boolean = false

    override fun run() {
        val format = DexFormat.forApiLevel(apiLevel)
        val mergedDexFile = DexFile.of(format)

        printVerbose("using format '${format.version}' for merged dex file '${outputFile.name}'")

        val startTime = System.nanoTime()

        inputFiles.forEach { file ->
            FileInputStream(file).use { `is` ->
                printVerbose("  reading file '${file.name}' ...")

                val dexFile = DexFile.empty()
                val reader = DexFileReader(`is`)
                reader.visitDexFile(dexFile)

                // set the dex format of the merged file to the maximum value of
                // each individual dex file.
                if (dexFile.dexFormat > mergedDexFile.dexFormat) {
                    printVerbose("  updating dex format to version '${dexFile.dexFormat.version}' due to input file '${file.name}'")
                    mergedDexFile.dexFormat = dexFile.dexFormat
                }

                dexFile.classDefsAccept(ClassDefAdder(mergedDexFile))
            }
        }

        DexFileWriter(outputFile.outputStream()).visitDexFile(mergedDexFile)

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
            val cmdLine = CommandLine(DexMergeCommand())
            cmdLine.execute(*args)
        }
    }
}