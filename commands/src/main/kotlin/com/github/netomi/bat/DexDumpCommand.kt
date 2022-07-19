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

import com.github.netomi.bat.dexdump.DexDumpPrinter
import com.github.netomi.bat.dexfile.DexFile
import com.github.netomi.bat.dexfile.io.DexFileReader
import com.github.netomi.bat.dexfile.visitor.filteredByExternalClassName
import picocli.CommandLine
import java.io.*

/**
 * Command-line tool to dump dex files.
 */
@CommandLine.Command(
    name                 = "bat-dexdump",
    description          = ["dumps dex files."],
    parameterListHeading = "%nParameters:%n",
    optionListHeading    = "%nOptions:%n")
class DexDumpCommand : Runnable {

    @CommandLine.Parameters(index = "0", arity = "1", paramLabel = "inputfile", description = ["input file to process (*.dex)"])
    private var inputFile: File? = null

    @CommandLine.Option(names = ["-o"], description = ["output file name (defaults to stdout)"])
    private var outputFile: File? = null

    @CommandLine.Option(names = ["-a"], description = ["print annotations"])
    private var printAnnotations = false

    @CommandLine.Option(names = ["-f"], description = ["print file summary"])
    private var printFileSummary = false

    @CommandLine.Option(names = ["-h"], description = ["print headers"])
    private var printHeaders = false

    @CommandLine.Option(names = ["-c"], description = ["class filter"])
    private var classNameFilter: String? = null

    override fun run() {
        inputFile?.apply {
            FileInputStream(this).use { `is` ->
                val output = if (outputFile == null) System.out else FileOutputStream(outputFile!!)
                output.use { os ->
                    val reader  = DexFileReader(`is`)
                    val dexFile = DexFile()
                    reader.visitDexFile(dexFile)

                    println("Processing '$name'...")
                    println("Opened '$name', DEX version '${dexFile.dexFormat?.version}'")

                    if (classNameFilter != null) {
                        dexFile.classDefsAccept(
                            filteredByExternalClassName(classNameFilter!!,
                            DexDumpPrinter(os, printFileSummary, printHeaders, printAnnotations)))
                    } else {
                        dexFile.accept(DexDumpPrinter(os, printFileSummary, printHeaders, printAnnotations))
                    }
                }
            }
        }

    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val cmdLine = CommandLine(DexDumpCommand())
            cmdLine.execute(*args)
        }
    }
}