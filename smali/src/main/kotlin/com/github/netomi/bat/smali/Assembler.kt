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
import com.github.netomi.bat.dexfile.editor.DexEditor
import com.github.netomi.bat.smali.assemble.ClassDefAssembler
import com.github.netomi.bat.smali.parser.SmaliLexer
import com.github.netomi.bat.smali.parser.SmaliParser
import org.antlr.v4.runtime.*
import org.antlr.v4.runtime.InputMismatchException
import org.antlr.v4.runtime.misc.IntervalSet
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes
import java.util.function.BiPredicate
import java.util.function.Predicate
import kotlin.io.path.name

class Assembler(dexFile: DexFile) {

    private val dexEditor: DexEditor = DexEditor.of(dexFile)

    @Throws(IOException::class)
    fun assemble(input: Path, callback: (Path, Path) -> Unit = fun(_, _) {}): Collection<ClassDef> {
        val assembledClasses: MutableCollection<ClassDef> = mutableListOf()

        val assembleFile = { path: Path ->
            Files.newInputStream(path).use { `is` ->
                callback(input, path)
                val classDef = assemble(`is`)
                assembledClasses.add(classDef)
            }
            Unit
        }

        if (Files.isDirectory(input)) {
            val inputFiles = Files.find(input, Int.MAX_VALUE, REGULAR_FILE)

            inputFiles.use {
                it.filter(SMALI_FILE)
                  .sorted()
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

        lexer.removeErrorListeners()
        parser.removeErrorListeners()
        parser.errorHandler = ExceptionErrorStrategy()

        return ClassDefAssembler(dexEditor).visit(parser.sFiles())!!
    }

    companion object {
        private val REGULAR_FILE = BiPredicate { _: Path, attr: BasicFileAttributes -> attr.isRegularFile }
        private val SMALI_FILE   = Predicate   { path: Path -> path.name.endsWith(".smali") }
    }
}

class ExceptionErrorStrategy : DefaultErrorStrategy() {

    override fun recover(recognizer: Parser?, e: RecognitionException?) {
        throw e!!
    }

    @Throws(RecognitionException::class)
    override fun reportInputMismatch(recognizer: Parser, e: InputMismatchException) {
        val t = recognizer.currentToken
        val line = t.line
        val col  = t.charPositionInLine

        val msg = "line $line:$col -> mismatched input ${getTokenErrorDisplay(e.offendingToken)} " +
                  "expecting one of ${e.expectedTokens.toString(recognizer.vocabulary)}"

        val ex = RecognitionException(msg, recognizer, recognizer.inputStream, recognizer.context)
        ex.initCause(e)
        throw ex
    }

    override fun reportUnwantedToken(recognizer: Parser) {
        throw InputMismatchException(recognizer)
    }

    override fun reportMissingToken(recognizer: Parser) {
        beginErrorCondition(recognizer)

        val t = recognizer.currentToken
        val line = t.line
        val col  = t.charPositionInLine
        val expecting: IntervalSet = getExpectedTokens(recognizer)

        val msg = "line $line:$col -> missing ${expecting.toString(recognizer.vocabulary)}" +
                  " at ${getTokenErrorDisplay(t)}"

        throw RecognitionException(msg, recognizer, recognizer.inputStream, recognizer.context)
    }


}