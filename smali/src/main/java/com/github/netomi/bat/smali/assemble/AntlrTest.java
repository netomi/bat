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

package com.github.netomi.bat.smali.assemble;

import com.github.netomi.bat.dexfile.ClassDef;
import com.github.netomi.bat.dexfile.DexFile;
import com.github.netomi.bat.smali.Disassembler;
import com.github.netomi.bat.smali.parser.SmaliLexer;
import com.github.netomi.bat.smali.parser.SmaliParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

public class AntlrTest {

    public static void main(String[] args) {
        //String input = ".class public interface abstract annotation Landroid/annotation/SuppressLint;\n.super Ljava/lang/Object;\n.source \"SuppressLint.java\""; //\n\n# interfaces\n.implements Ljava/lang/annotation/Annotation;";
        String input =
            ".class public interface abstract annotation Landroid/annotation/SuppressLint;\n" +
            ".super Ljava/lang/Object;\n\n\n" +
            "# interfaces\n.implements Ljava/lang/annotation/Annotation;\n\n" +
            ".field public publicStringField:Ljava/lang/String;\n" +
            ".field public publicIntegerField:I\n" +
            ".field public publicObjectField:Ljava/lang/Object;\n" +
            ".field public publicStringArrayField:[Ljava/lang/String;\n" +
            ".field private privateStringField:Ljava/lang/String;\n" +
            ".field public static publicStaticStringField:Ljava/lang/String;\n" +
            ".field private static privateStaticStringField:Ljava/lang/String;\n";

        SmaliLexer smaliLexer = new SmaliLexer(CharStreams.fromString(input));
        CommonTokenStream commonTokenStream = new CommonTokenStream(smaliLexer);
        SmaliParser smaliParser = new SmaliParser(commonTokenStream);

        DexFile dexFile = new DexFile();

        ClassDef classDef = new ClassDefAssembler(dexFile).visit(smaliParser.smaliclass());
        new Disassembler(className -> System.out).visitClassDef(dexFile, 0, classDef);
    }
}
