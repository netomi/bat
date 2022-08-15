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
import picocli.CommandLine.HelpCommand
import picocli.CommandLine.Mixin
import java.io.*

/**
 * Command-line tool to dump dex files.
 */
@CommandLine.Command(
    name                 = "bat-dexdump",
    description          = ["dumps dex files."],
    subcommands          = [ DumpCommand::class, ListCommand::class, HelpCommand::class ],
    parameterListHeading = "%nParameters:%n",
    optionListHeading    = "%nOptions:%n")
class DexDumpCommand {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val cmdLine = CommandLine(DexDumpCommand())
            cmdLine.execute(*args)
        }
    }
}

@CommandLine.Command(
    synopsisHeading      = "%nUsage:%n%n",
    descriptionHeading   = "%nDescription:%n%n",
    parameterListHeading = "%nParameters:%n%n",
    optionListHeading    = "%nOptions:%n%n",
    commandListHeading   = "%nCommands:%n%n")
open class ReusableOptions {
    @CommandLine.Parameters(index = "0", arity = "1", paramLabel = "inputfile", description = ["input file to process (*.dex)"])
    lateinit var inputFile: File

    @CommandLine.Option(names = ["-o"], description = ["output file name (defaults to stdout)"])
    var outputFile: File? = null
}

@CommandLine.Command(
    name        = "dump",
    aliases     = [ "d", "du" ],
    description = ["dump the content of a dex file similar to dexdump"])
private class DumpCommand: ReusableOptions(), Runnable {
    @CommandLine.Option(names = ["-a"], description = ["print annotations"])
    private var printAnnotations = false

    @CommandLine.Option(names = ["-f"], description = ["print file summary"])
    private var printFileSummary = false

    @CommandLine.Option(names = ["-d"], description = ["disassemble code sections"])
    private var disassembleCode = false

    @CommandLine.Option(names = ["-h"], description = ["print headers"])
    private var printHeaders = false

    @CommandLine.Option(names = ["-c"], description = ["class filter"])
    private var classNameFilter: String? = null

    override fun run() {
        processInput(inputFile, outputFile) { dexFile, os ->
            println("Processing '${inputFile.name}'...")
            println("Opened '${inputFile.name}', DEX version '${dexFile.dexFormat.version}'")

            if (classNameFilter != null) {
                dexFile.classDefsAcceptIndexed(
                    filteredByExternalClassName(
                        classNameFilter!!,
                        DexDumpPrinter(os, printFileSummary, printHeaders, printAnnotations, disassembleCode)
                    )
                )
            } else {
                dexFile.accept(DexDumpPrinter(os, printFileSummary, printHeaders, printAnnotations, disassembleCode))
            }
        }
    }
}

@CommandLine.Command(
    name        = "list",
    aliases     = [ "l"],
    description = ["list data items inside a dex file"])
private class ListCommand {

    @CommandLine.Command(
        name        = "strings",
        description = ["list string items"])
    fun listStrings(@Mixin opts: ReusableOptions) {
        processInput(opts.inputFile, opts.outputFile) { dexFile, os ->
            val printer = PrintWriter(os, true)
            for (string in dexFile.getStringIDs()) {
                printer.println(string.stringValue)
            }
        }
    }

    @CommandLine.Command(
        name        = "types",
        description = ["list type items"])
    fun listTypes(@Mixin opts: ReusableOptions) {
        processInput(opts.inputFile, opts.outputFile) { dexFile, os ->
            val printer = PrintWriter(os, true)
            for (type in dexFile.getTypeIDs()) {
                printer.println(type.getType(dexFile))
            }
        }
    }

    @CommandLine.Command(
        name        = "protos",
        description = ["list proto items"])
    fun listProtos(@Mixin opts: ReusableOptions) {
        processInput(opts.inputFile, opts.outputFile) { dexFile, os ->
            val printer = PrintWriter(os, true)
            for (proto in dexFile.getProtoIDs()) {
                printer.println(proto.getDescriptor(dexFile))
            }
        }
    }

    @CommandLine.Command(
        name        = "classes",
        description = ["list proto items"])
    fun listClasses(@Mixin opts: ReusableOptions) {
        processInput(opts.inputFile, opts.outputFile) { dexFile, os ->
            val printer = PrintWriter(os, true)
            for (clazz in dexFile.getClassDefs()) {
                printer.println(clazz.getClassName(dexFile))
            }
        }
    }

    @CommandLine.Command(
        name        = "fields",
        description = ["list field items"])
    fun listFields(@Mixin opts: ReusableOptions) {
        processInput(opts.inputFile, opts.outputFile) { dexFile, os ->
            val printer = PrintWriter(os, true)
            for (field in dexFile.getFieldIDs()) {
                printer.print(field.getClassType(dexFile))
                printer.print("->")
                printer.print(field.getName(dexFile))
                printer.print(":")
                printer.println(field.getType(dexFile))
            }
        }
    }

    @CommandLine.Command(
        name        = "methods",
        description = ["list method items"])
    fun listMethods(@Mixin opts: ReusableOptions) {
        processInput(opts.inputFile, opts.outputFile) { dexFile, os ->
            val printer = PrintWriter(os, true)
            for (method in dexFile.getMethodIDs()) {
                printer.print(method.getClassType(dexFile))
                printer.print("->")
                printer.print(method.getName(dexFile))
                printer.println(method.getProtoID(dexFile).getDescriptor(dexFile))
            }
        }
    }
}

private fun processInput(inputFile: File, outputFile: File?, callback: (DexFile, OutputStream) -> Unit) {
    FileInputStream(inputFile).use { `is` ->
        val os = if (outputFile == null) System.out else FileOutputStream(outputFile)
        val reader = DexFileReader(`is`)
        val dexFile = DexFile.empty()
        reader.visitDexFile(dexFile)

        callback(dexFile, os)

        if (outputFile != null) {
            os.close()
        }
    }
}