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

import com.github.netomi.bat.dexfile.*;
import com.github.netomi.bat.dexfile.io.DexFileReader;
import com.github.netomi.bat.dexfile.visitor.*;
import com.github.netomi.bat.io.FileOutputStreamFactory;
import com.github.netomi.bat.io.OutputStreamFactory;
import com.github.netomi.bat.smali.disassemble.SmaliPrinter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

public class Disassembler
implements   ClassDefVisitor
{
    private final OutputStreamFactory outputStreamFactory;

    public Disassembler(OutputStreamFactory outputStreamFactory) {
        this.outputStreamFactory = outputStreamFactory;
    }

    @Override
    public void visitClassDef(DexFile dexFile, int index, ClassDef classDef) {
        try (OutputStream   os  = outputStreamFactory.createOutputStream(classDef.getClassName(dexFile));
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8), 8192))
        {
            new SmaliPrinter(out).visitClassDef(dexFile, index, classDef);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void main(String[] args) {
        DexFile dexFile = new DexFile();

        try (InputStream is = new FileInputStream("classes-io.dex"))
        {
            DexFileReader reader = new DexFileReader(is);
            reader.visitDexFile(dexFile);

            dexFile.classDefsAccept(new Disassembler(new FileOutputStreamFactory(Paths.get("out2"))));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
