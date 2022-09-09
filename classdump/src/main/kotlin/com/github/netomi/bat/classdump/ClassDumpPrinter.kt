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

package com.github.netomi.bat.classdump

import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.attribute.SourceFileAttribute
import com.github.netomi.bat.classfile.io.ClassFileReader
import com.github.netomi.bat.io.IndentingPrinter
import com.google.common.hash.Hashing
import java.io.InputStream
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.nio.file.Files
import java.nio.file.Path
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.io.path.getLastModifiedTime
import kotlin.io.path.readBytes

class ClassDumpPrinter constructor(private val printHeader: Boolean = true) {
    fun dumpClassFile(inputPath: Path, `is`: InputStream, os: OutputStream) {
        val classFile = ClassFile.empty()
        val reader    = ClassFileReader(`is`)
        reader.visitClassFile(classFile)

        if (printHeader) {
            val writer  = OutputStreamWriter(os)
            val printer = IndentingPrinter(writer, 2)

            printer.println("Classfile ${inputPath.toAbsolutePath()}")
            printer.levelUp()

            val formatter = DateTimeFormatter.ofPattern("LLL d, yyyy").withZone(ZoneId.systemDefault())
            val lastModified = inputPath.getLastModifiedTime().toInstant()
            val fileSize = Files.size(inputPath)
            printer.println("Last modified ${formatter.format(lastModified)}; size $fileSize bytes")

            val checksum = Hashing.sha256().hashBytes(inputPath.readBytes()).toString()
            printer.println("SHA-256 checksum $checksum")

            val sourceFile = classFile.attributes.filterIsInstance<SourceFileAttribute>()
                                                 .singleOrNull()?.getSourceFile(classFile)
            if (sourceFile != null) {
                printer.println("Compiled from \"$sourceFile\"")
            }

            printer.levelDown()
            printer.flush()
        }

        classFile.accept(ClassFilePrinter(os))
    }
}