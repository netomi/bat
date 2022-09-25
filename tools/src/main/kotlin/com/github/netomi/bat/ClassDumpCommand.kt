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
import com.github.netomi.bat.io.*
import com.github.netomi.bat.util.allMatcher
import com.github.netomi.bat.util.asInternalClassName
import com.github.netomi.bat.util.classNameMatcher
import com.github.netomi.bat.util.fileNameMatcher
import picocli.CommandLine
import java.nio.file.Path

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
    private lateinit var inputPath: Path

    @CommandLine.Option(names = ["-o"], arity = "1", description = ["output file name (defaults to stdout)"])
    private var outputPath: Path? = null

    @CommandLine.Option(names = ["-c"], description = ["class filter"])
    private var classNameFilter: String? = null

    @CommandLine.Option(names = ["-s"], arity = "1", defaultValue = "dump", description = ["file suffix (defaults to 'dump')"])
    private lateinit var suffix: String

    @CommandLine.Option(names = ["-h"], description = ["print header"])
    private var printHeader = false

    @CommandLine.Option(names = ["-v"], description = ["verbose output"])
    private var verbose: Boolean = false

    override fun run() {
        val dumpToConsole  = outputPath == null
        val dumpSingleFile = inputPath.endsWith(".class")

        if (!dumpToConsole) {
            if (dumpSingleFile) {
                printVerbose("dumping class file '${inputPath}' to file '$outputPath' ...")
            } else {
                printVerbose("dumping class file(s) from '${inputPath}' into directory '$outputPath' ...")
            }
        }

        val writer =
            if (dumpToConsole) {
                ConsoleOutputSink.of(System.out)
            } else if (dumpSingleFile) {
                FileOutputSink.of(outputPath!!)
            } else {
                transformOutputDataEntriesWith({ name -> name.replace(".class", ".$suffix") },
                DirectoryOutputSink.of(outputPath!!))
            }

        val classDumper      = ClassDumpPrinter(writer, printHeader)
        val classNameMatcher = if (classNameFilter != null) classNameMatcher(classNameFilter!!) else allMatcher()

        val processClassFile = { entry: DataEntry ->
            // this is a bit of a hack: we treat the entry name as internal classname
            val className = entry.name.removeSuffix(".class").asInternalClassName().toExternalClassName()
            if (classNameMatcher.matches(className)) {
                if (!dumpToConsole) {
                    printVerbose("  dumping class '${entry.name}'")
                }

                classDumper.read(entry)
            }
        }

        val inputSource = PathInputSource.of(inputPath, true)
        inputSource.pumpDataEntries(
            unwrapArchives(
            filterDataEntriesBy(fileNameMatcher("**.class"),
            processClassFile)))
    }

    private fun printVerbose(text: String) {
        if (verbose) {
            println(text)
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