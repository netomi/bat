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
import com.github.netomi.bat.jasm.assemble.ClassFileAssembler
import com.github.netomi.bat.jasm.parser.JasmLexer
import com.github.netomi.bat.jasm.parser.JasmParser
import org.antlr.v4.runtime.*
import org.antlr.v4.runtime.InputMismatchException
import org.antlr.v4.runtime.misc.IntervalSet
import java.io.IOException
import java.io.InputStream
import java.io.PrintWriter
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes
import java.util.function.BiPredicate
import java.util.function.Predicate
import kotlin.io.path.absolutePathString
import kotlin.io.path.name

class Assembler(private val lenientMode:    Boolean      = false,
                private val warningPrinter: PrintWriter? = null) {

    @Throws(IOException::class)
    fun assemble(input: Path, callback: (Path, Path) -> Unit = fun(_, _) {}): List<ClassFile> {
        val assembledClasses = mutableListOf<ClassFile>()

        val assembleFile = { path: Path ->
            Files.newInputStream(path).use { `is` ->
                callback(input, path)
                val classDefList = assemble(`is`, path.absolutePathString())
                assembledClasses.addAll(classDefList)
            }
            Unit
        }

        if (Files.isDirectory(input)) {
            val inputFiles = Files.find(input, Int.MAX_VALUE, REGULAR_FILE)

            inputFiles.use {
                it.filter(JASM_FILE)
                  .sorted()
                  .forEach(assembleFile)
            }
        } else {
            assembleFile(input)
        }

        return assembledClasses
    }

    fun assemble(inputStream: InputStream, name: String? = null): List<ClassFile> {
        val lexer       = JasmLexer(CharStreams.fromStream(inputStream))
        val tokenStream = CommonTokenStream(lexer)
        val parser      = JasmParser(tokenStream)

        lexer.removeErrorListeners()
        parser.removeErrorListeners()
        parser.errorHandler = ExceptionErrorStrategy()

        try {
            return ClassFileAssembler(lenientMode, warningPrinter).visit(parser.cFiles())
        } catch (exception: RuntimeException) {
            if (name != null) {
                throw JasmAssembleException("failed to assemble input from '$name': ${exception.message}", exception)
            } else {
                throw JasmAssembleException("failed to assemble input: ${exception.message}", exception)
            }
        }
    }

    companion object {
        private val REGULAR_FILE = BiPredicate { _: Path, attr: BasicFileAttributes -> attr.isRegularFile }
        private val JASM_FILE    = Predicate   { path: Path -> path.name.endsWith(".jasm") }
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