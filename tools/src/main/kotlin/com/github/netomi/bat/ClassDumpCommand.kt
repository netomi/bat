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

import com.github.netomi.bat.classdump.ClassDumpPrinter
import picocli.CommandLine
import java.io.*
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.attribute.BasicFileAttributes
import java.util.function.BiPredicate
import java.util.function.Predicate
import kotlin.io.path.*

/**
 * Command-line tool to dump class files.
 */
@CommandLine.Command(
    name                 = "bat-classdump",
    description          = ["dumps class files."],
    parameterListHeading = "%nParameters:%n",
    optionListHeading    = "%nOptions:%n")
class ClassDumpCommand : Runnable {

    @CommandLine.Parameters(index = "0", arity = "1", paramLabel = "inputfile", description = ["input file(s) to process (*.class / *.jar)"])
    private lateinit var inputFile: File

    @CommandLine.Option(names = ["-o"], arity = "1", description = ["output file name (defaults to stdout)"])
    private var outputFile: File? = null

    @CommandLine.Option(names = ["-h"], description = ["print header"])
    private var printHeader = false

    @CommandLine.Option(names = ["-v"], description = ["verbose output"])
    private var verbose: Boolean = false

    override fun run() {
        val printer = ClassDumpPrinter(printHeader)

        val inputPath = inputFile.toPath()
        if (inputPath.isDirectory()) {
            val outputBasePath = if (outputFile == null) null else outputFile!!.toPath()

            printVerbose("Dumping class files from '${inputFile}' into directory '$outputBasePath' ...")

            val inputFiles = Files.find(inputPath, Int.MAX_VALUE, REGULAR_FILE)
            inputFiles.use {
                it.filter(CLASS_FILE)
                  .sorted()
                  .forEach { filePath ->
                      printVerbose("  dumping file '${filePath}'")

                      val os = if (outputBasePath != null) {
                          val relativeFilePath = filePath.relativeTo(inputPath)
                          val outputPath       = outputBasePath.resolve(relativeFilePath)
                          outputPath.parent.createDirectories()
                          Paths.get(outputBasePath.resolve(relativeFilePath).toString() + ".txt").outputStream()
                      } else {
                          System.out
                      }

                      filePath.inputStream().use { `is` ->
                          printer.dumpClassFile(filePath, `is`, os)
                      }
                  }
            }
        } else {
            FileInputStream(inputFile).use { `is` ->
                val os = if (outputFile == null) System.out else FileOutputStream(outputFile!!)

                // TODO: handle jar or general archive files
                printer.dumpClassFile(inputPath, `is`, os)

                if (outputFile != null) {
                    os.close()
                }
            }
        }
    }

    private fun printVerbose(text: String) {
        if (verbose) {
            println(text)
        }
    }

    companion object {
        private val REGULAR_FILE = BiPredicate { _: Path, attr: BasicFileAttributes -> attr.isRegularFile }
        private val CLASS_FILE   = Predicate   { path: Path -> path.name.endsWith(".class") }

        @JvmStatic
        fun main(args: Array<String>) {
            val cmdLine = CommandLine(ClassDumpCommand())
            cmdLine.execute(*args)
        }
    }
}