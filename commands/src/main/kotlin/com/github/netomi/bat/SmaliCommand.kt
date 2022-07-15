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
import com.github.netomi.bat.dexfile.io.DexFileReader
import com.github.netomi.bat.dexfile.io.DexFileWriter
import com.github.netomi.bat.dexfile.visitor.ClassDefVisitor
import com.github.netomi.bat.io.FileOutputStreamFactory
import com.github.netomi.bat.smali.Assembler
import com.github.netomi.bat.smali.Disassembler
import com.sun.jdi.BooleanValue
import picocli.CommandLine
import java.io.File
import java.io.FileInputStream
import java.nio.file.Files
import kotlin.io.path.exists

/**
 * Command-line tool to disassemble dex files in smali format.
 */
@CommandLine.Command(
    name                 = "bat-smali",
    description          = ["assembles smali files to dex format."],
    parameterListHeading = "%nParameters:%n",
    optionListHeading    = "%nOptions:%n")
class SmaliCommand : Runnable {

    @CommandLine.Parameters(index = "0", arity = "1", paramLabel = "inputfile", description = ["input file / directory to process (*.smali)"])
    private var inputFile: File? = null

    @CommandLine.Option(names = ["-o"], arity = "1", defaultValue = "out.dex", description = ["output file"])
    private var outputFile: File? = null

    @CommandLine.Option(names = ["-v"], description = ["verbose output"])
    private var verbose: Boolean = false

    override fun run() {
        inputFile?.apply {
            val dexFile = DexFile.of(DexFormat.FORMAT_035)

            if (this.isDirectory) {
                printVerbose("Assembling directory '$name' into file ${outputFile?.name} ...")
                val assembledClasses = Assembler(dexFile).assemble(toPath())
                printVerbose("done assembling ${assembledClasses.size} classes.")
            } else {
                printVerbose("Assembling file '$name' into file ${outputFile?.name} ...")
                Assembler(dexFile).assemble(toPath())
                printVerbose("done assembling '${name}'.")
            }

            DexFileWriter(outputFile!!.outputStream()).visitDexFile(dexFile)
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
            val cmdLine = CommandLine(SmaliCommand())
            cmdLine.execute(*args)
        }
    }
}