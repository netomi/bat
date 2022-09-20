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
import com.github.netomi.bat.dexfile.io.DexFileReader
import com.github.netomi.bat.dexfile.visitor.multiClassDefVisitorOf
import com.github.netomi.bat.io.*
import com.github.netomi.bat.smali.Disassembler
import com.github.netomi.bat.util.fileNameMatcher
import kotlinx.coroutines.Dispatchers
import picocli.CommandLine
import java.lang.Runnable
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists

/**
 * Command-line tool to disassemble dex files in smali format.
 */
@CommandLine.Command(
    name                 = "bat-baksmali",
    description          = ["disassembles dex files."],
    parameterListHeading = "%nParameters:%n",
    optionListHeading    = "%nOptions:%n")
class BakSmaliCommand : Runnable {

    @CommandLine.Parameters(index = "0", arity = "1", paramLabel = "inputfile", description = ["input file to process (*.dex)"])
    private lateinit var inputPath: Path

    @CommandLine.Option(names = ["-o"], arity = "1", defaultValue = "out", description = ["output directory"])
    private lateinit var outputPath: Path

    @CommandLine.Option(names = ["-v"], description = ["verbose output"])
    private var verbose: Boolean = false

    override fun run() {
        val processDexFile = { entry: DataEntry ->
            if (!outputPath.exists()) {
                Files.createDirectories(outputPath)
            }

            entry.getInputStream().use { `is` ->
                val dexFile = DexFile.empty()
                val reader  = DexFileReader(`is`, false)

                printVerbose("disassembling '${entry.fullName}' into directory '$outputPath' ...")
                reader.visitDexFile(dexFile)

                val startTime = System.nanoTime()

                dexFile.parallelClassDefsAccept(Dispatchers.IO) {
                    multiClassDefVisitorOf(
                        { df, classDef -> printVerbose("  disassembling class '${classDef.getClassName(df)}'") },
                        Disassembler(FileOutputStreamFactory(outputPath, "smali"))
                    )
                }

                val endTime = System.nanoTime()
                printVerbose("done, took ${(endTime - startTime) / 1e6} ms.")
            }
        }

        val inputSource = PathInputSource.of(inputPath)
        inputSource.pumpDataEntries(
            filterDataEntriesBy(fileNameMatcher("**.dex"),
            processDexFile))
    }

    private fun printVerbose(text: String) {
        if (verbose) {
            println(text)
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val cmdLine = CommandLine(BakSmaliCommand())
            cmdLine.execute(*args)
        }
    }
}