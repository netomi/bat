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
import com.github.netomi.bat.classfile.io.ClassFilePrinter
import picocli.CommandLine
import java.io.*

/**
 * Command-line tool to dump dex files.
 */
@CommandLine.Command(
    name                 = "bat-classdump",
    description          = ["dumps class files."],
    parameterListHeading = "%nParameters:%n",
    optionListHeading    = "%nOptions:%n")
class ClassDumpCommand : Runnable {

    @CommandLine.Parameters(index = "0", arity = "1", paramLabel = "inputfile", description = ["input file to process (*.class / *.jar)"])
    private var inputFile: File? = null

    @CommandLine.Option(names = ["-o"], description = ["output file name (defaults to stdout)"])
    private var outputFile: File? = null

    @CommandLine.Option(names = ["-a"], description = ["print annotations"])
    private var printAnnotations = false

    @CommandLine.Option(names = ["-c"], description = ["class filter"])
    private var classNameFilter: String? = null

    override fun run() {
        inputFile?.apply {
            FileInputStream(this).use { `is` ->
                val os = if (outputFile == null) System.out else FileOutputStream(outputFile!!)

                println("Processing '$name'...")

                // TODO: currently supporting only single class files.
                val classFile = ClassFile.readClassFile(DataInputStream(`is`))
                classFile.accept(ClassFilePrinter(os))

                if (outputFile != null) {
                    os.close()
                }
            }
        }

    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val cmdLine = CommandLine(ClassDumpCommand())
            cmdLine.execute(*args)
        }
    }
}