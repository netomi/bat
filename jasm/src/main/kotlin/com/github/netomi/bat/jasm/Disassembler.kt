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

package com.github.netomi.bat.jasm

import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.visitor.ClassFileVisitor
import com.github.netomi.bat.io.FileOutputStreamFactory
import com.github.netomi.bat.io.OutputStreamFactory
import com.github.netomi.bat.jasm.disassemble.JasmPrinter
import java.nio.file.Path

class Disassembler(private val outputStreamFactory: OutputStreamFactory) : ClassFileVisitor {
    override fun visitClassFile(classFile: ClassFile) {
        outputStreamFactory.createOutputStream(classFile.className.toString()).bufferedWriter().use { writer ->
            JasmPrinter(writer).visitClassFile(classFile)
        }
    }
}

fun main(args: Array<String>) {
    val factory = FileOutputStreamFactory(Path.of("tmp"), "jasm")
    val disassembler = Disassembler(factory)


}