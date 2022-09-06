/*
 *  Copyright (c) 2020 Thomas Neidhart.
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

package com.github.netomi.bat.classfile

import com.github.netomi.bat.classfile.io.ClassFileReader
import com.github.netomi.bat.classfile.io.ClassFileWriter
import com.github.netomi.bat.classfile.printer.ClassFilePrinter
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.attribute.BasicFileAttributes
import java.util.function.BiPredicate
import java.util.function.Predicate
import kotlin.io.path.*

object Main {

    fun main2() {
//        val classFileReader = ClassFileReader(FileInputStream("Equivalence.class"))
        val classFileReader = ClassFileReader(FileInputStream("module-info.class"))

        val classFile = ClassFile.empty()
        classFileReader.visitClassFile(classFile)
        classFile.accept(ClassFilePrinter())
    }

    @Throws(IOException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        main2()

        System.exit(0)

        val input = Paths.get("tmp/in")
        val output = Paths.get("tmp/out")

        val inputFiles = Files.find(input, Int.MAX_VALUE, REGULAR_FILE)

        inputFiles.use {
            it.filter(CLASS_FILE)
                .sorted()
                .forEach { path ->
                    val inputPath = path.relativeTo(input)
                    println("handling file $inputPath")

                    val classFileReader = ClassFileReader(path.inputStream())
                    val classFile = ClassFile.empty()
                    classFileReader.visitClassFile(classFile)
                    classFile.accept(ClassFilePrinter())

                    val outputPath = output.resolve(inputPath)
                    outputPath.parent.createDirectories()

                    val classFileWriter = ClassFileWriter(output.resolve(inputPath).outputStream())
                    classFileWriter.visitClassFile(classFile)
                    classFileWriter.close()
                }
        }
    }
}

private val REGULAR_FILE = BiPredicate { _: Path, attr: BasicFileAttributes -> attr.isRegularFile }
private val CLASS_FILE   = Predicate   { path: Path -> path.name.endsWith(".class") }
