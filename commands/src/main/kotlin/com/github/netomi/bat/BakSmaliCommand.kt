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
import com.github.netomi.bat.dexfile.visitor.ClassDefVisitor
import com.github.netomi.bat.io.FileOutputStreamFactory
import com.github.netomi.bat.smali.Disassembler
import picocli.CommandLine
import java.io.File
import java.io.FileInputStream
import java.nio.file.Files
import kotlin.io.path.exists

/**
 * Command-line tool to disassemble dex files in smali format.
 */
@CommandLine.Command(
    name                 = "baksmali",
    description          = ["disassembles dex files."],
    parameterListHeading = "%nParameters:%n",
    optionListHeading    = "%nOptions:%n")
class BakSmaliCommand : Runnable {

    @CommandLine.Parameters(index = "0", arity = "1", paramLabel = "inputfile", description = ["input file to process (*.dex)"])
    private var inputFile: File? = null

    @CommandLine.Option(names = ["-o"], arity = "1", defaultValue = "out", description = ["output directory"])
    private var outputFile: File? = null

    override fun run() {
        inputFile?.apply {
            FileInputStream(this).use { `is` ->
                val outputPath = outputFile?.toPath()
                if (outputPath != null) {
                    if (!outputPath.exists()) {
                        Files.createDirectories(outputPath)
                    }

                    val dexFile = DexFile()
                    val reader  = DexFileReader(`is`, false)

                    println("Disassembling '$name' into directory ${outputPath} ...")
                    reader.visitDexFile(dexFile)

                    dexFile.classDefsAccept(
                        ClassDefVisitor.Multi.of(
                            { df, _, classDef -> println("  disassembling class '${classDef.getClassName(df)}'") },
                            Disassembler(FileOutputStreamFactory(outputPath, "smali")))
                        )

                    println("done.")
                }
            }
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