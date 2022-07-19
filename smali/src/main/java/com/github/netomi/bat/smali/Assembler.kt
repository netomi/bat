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
package com.github.netomi.bat.smali

import com.github.netomi.bat.dexfile.ClassDef
import com.github.netomi.bat.dexfile.DexFile
import com.github.netomi.bat.smali.assemble.ClassDefAssembler
import com.github.netomi.bat.smali.parser.SmaliLexer
import com.github.netomi.bat.smali.parser.SmaliParser
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes
import java.util.*
import java.util.function.BiPredicate
import java.util.function.Predicate
import kotlin.collections.ArrayList
import kotlin.io.path.name

class Assembler(private val dexFile: DexFile) {

    @Throws(IOException::class)
    fun assemble(input: Path): Collection<ClassDef> {
        val assembledClasses: MutableCollection<ClassDef> = mutableListOf()

        val assembleFile = { path: Path ->
            Files.newInputStream(path).use { `is` ->
                val classDef = assemble(`is`)
                assembledClasses.add(classDef)
            }
            Unit
        }

        if (Files.isDirectory(input)) {
            val inputFiles = Files.find(input, Int.MAX_VALUE, REGULAR_FILE)

            inputFiles.use {
                it.filter(SMALI_FILE)
                  .forEach(assembleFile)
            }
        } else {
            assembleFile(input)
        }

        return assembledClasses
    }

    fun assemble(inputStream: InputStream): ClassDef {
        val lexer       = SmaliLexer(CharStreams.fromStream(inputStream))
        val tokenStream = CommonTokenStream(lexer)
        val parser      = SmaliParser(tokenStream)

        return ClassDefAssembler(dexFile).visit(parser.sFiles())!!
    }

    companion object {
        private val REGULAR_FILE = BiPredicate { _: Path, attr: BasicFileAttributes -> attr.isRegularFile }
        private val SMALI_FILE   = Predicate   { path: Path -> path.name.endsWith(".smali") }
    }
}