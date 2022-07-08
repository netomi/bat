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
package com.github.netomi.bat.smali;

import com.github.netomi.bat.dexfile.ClassDef;
import com.github.netomi.bat.dexfile.DexFile;
import com.github.netomi.bat.dexfile.DexFormat;
import com.github.netomi.bat.dexfile.io.DexFileWriter;
import com.github.netomi.bat.smali.assemble.ClassDefAssembler;
import com.github.netomi.bat.smali.disassemble.SmaliPrinter;
import com.github.netomi.bat.smali.parser.SmaliLexer;
import com.github.netomi.bat.smali.parser.SmaliParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class Assembler
{
    private static final BiPredicate<Path, BasicFileAttributes> REGULAR_FILE = (path, attr) -> attr.isRegularFile();
    private static final Predicate<Path>                        SMALI_FILE   = (path) -> !path.endsWith(".smali");

    private final DexFile dexFile;

    public Assembler(DexFile dexFile) {
        this.dexFile        = dexFile;
    }

    public void assemble(Path inputDirectory) throws IOException {
        Files.find(inputDirectory, Integer.MAX_VALUE, REGULAR_FILE)
             .filter(SMALI_FILE)
             .forEach((path) -> {
                 System.out.println(" visiting " + path);
                 try (InputStream is = Files.newInputStream(path)) {
                     assemble(is);
                 } catch (IOException ioe) {
                     // should not happen.
                     throw new RuntimeException(ioe);
                 }
             });
    }

    public ClassDef assemble(InputStream inputStream) throws IOException {
        SmaliLexer        lexer       = new SmaliLexer(CharStreams.fromStream(inputStream));
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        SmaliParser       parser      = new SmaliParser(tokenStream);

        ClassDef classDef = new ClassDefAssembler(dexFile).visit(parser.sFiles());
        return classDef;
    }

    public static void main(String[] args) throws IOException {
        String fileName = "smali/src/test/resources/smali/R$attr.smali";

        DexFile dexFile = DexFile.of(DexFormat.FORMAT_035);
        try (InputStream is = Files.newInputStream(Paths.get(fileName)))
        {
            ClassDef classDef = new Assembler(dexFile).assemble(is);

            classDef.accept(dexFile, new SmaliPrinter());

            dexFile.addClassDef(classDef);
            dexFile.accept(new DexFileWriter(new FileOutputStream("out.dex")));
        }
    }
}
