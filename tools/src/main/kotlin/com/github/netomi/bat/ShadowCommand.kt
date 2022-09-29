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
import com.github.netomi.bat.classfile.editor.ClassRenamer
import com.github.netomi.bat.classfile.editor.Renamer
import com.github.netomi.bat.classfile.io.ClassFileReader
import com.github.netomi.bat.classfile.io.ClassFileWriter
import com.github.netomi.bat.io.*
import com.github.netomi.bat.util.*
import picocli.CommandLine
import java.nio.file.Path

/**
 * Command-line tool to create uber jars with optional relocation of class files.
 */
@CommandLine.Command(
    name                 = "bat-shadow",
    description          = ["create uber jars and relocate class files."],
    parameterListHeading = "%nParameters:%n",
    optionListHeading    = "%nOptions:%n")
class ShadowCommand : Runnable {

    @CommandLine.Parameters(index = "0", arity = "1..*", paramLabel = "inputfile", description = ["input file(s) to process (*.class / *.jar)"])
    private lateinit var inputPaths: Array<Path>

    @CommandLine.Option(names = ["-o"], arity = "1", defaultValue = "out.jar", description = ["output file name (defaults to out.jar)"])
    private var outputPath: Path? = null

    @CommandLine.Option(names = ["-r"], arity = "1..*", description = ["relocate pattern"])
    private var relocatePatterns: Array<String> = arrayOf()

    @CommandLine.Option(names = ["-v"], description = ["verbose output"])
    private var verbose: Boolean = false

    override fun run() {
        val dumpToArchive = isArchive(outputPath)
        val patterns      = TransformPatterns.of(relocatePatterns)
        val classRenamer  = ClassRenamer(PatternRenamer(patterns))

        val writer =
            if (dumpToArchive) {
                ZipOutputSink.of(outputPath!!)
            } else {
                DirectoryOutputSink.of(outputPath!!)
            }

        writer.use {
            val processClassFile = { entry: DataEntry ->
                entry.getInputStream().use { `is` ->
                    val classFile = ClassFile.empty()
                    val reader    = ClassFileReader(`is`)
                    reader.visitClassFile(classFile)

                    val oldClassName = classFile.className

                    classRenamer.visitClassFile(classFile)

                    val (replaced, outputEntryName) = patterns.replace(entry.name)
                    val outputEntry = if (replaced) {
                        val newClassName = classFile.className
                        printVerbose("  relocate class '${oldClassName.toExternalClassName()}' to '${newClassName.toExternalClassName()}'")
                        TransformedDataEntry.of(outputEntryName, entry)
                    } else {
                        entry
                    }

                    val os = writer.createOutputStream(outputEntry)
                    val classFileWriter = ClassFileWriter(os)
                    classFileWriter.visitClassFile(classFile)
                }
            }

            for (inputPath in inputPaths) {
                printVerbose("shadow class(es) from '${inputPath}' into '$outputPath' ...")

                val inputSource = PathInputSource.of(inputPath, true)
                inputSource.pumpDataEntries(
                    unwrapArchives(
                    filterDataEntriesBy(fileNameMatcher("**.class"),
                    processClassFile)))
            }
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
            val cmdLine = CommandLine(ShadowCommand())
            cmdLine.execute(*args)
        }
    }
}

class PatternRenamer constructor(private val transformPatterns: TransformPatterns): Renamer() {
    override fun renameClassName(className: JvmClassName): JvmClassName {
        return transformPatterns.replace(className.toInternalClassName()).second.asInternalClassName()
    }
}

data class TransformPatterns private constructor(private val transformPatterns: List<TransformPattern>) {

    fun replace(value: String): Pair<Boolean, String> {
        for (pattern in transformPatterns) {
            if (pattern.matches(value)) {
                return Pair(true, pattern.replace(value))
            }
        }
        return Pair(false, value)
    }

    companion object {
        fun of(patterns: Array<String>): TransformPatterns {
            val list = mutableListOf<TransformPattern>()
            for (pattern in patterns) {
                list.add(TransformPattern.of(pattern))
            }
            return TransformPatterns(list)
        }
    }
}

data class TransformPattern constructor(val match: String, val replacement: String) {

    fun matches(value: String): Boolean {
        return value.startsWith(match)
    }

    fun replace(value: String): String {
        return value.replace(match, replacement)
    }

    companion object {
        fun of(pattern: String): TransformPattern {
            val tokens = pattern.split("=")
            return TransformPattern(tokens[0], tokens[1])
        }
    }
}