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
import com.github.netomi.bat.dexfile.annotation.*;
import com.github.netomi.bat.dexfile.io.DexFileReader;
import com.github.netomi.bat.dexfile.visitor.*;
import com.github.netomi.bat.smali.io.FileOutputStreamFactory;
import com.github.netomi.bat.smali.io.OutputStreamFactory;

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

    // Inner helper classes.

    private class SmaliPrinter
    implements    ClassDefVisitor,
                  ClassDataVisitor,
                  EncodedFieldVisitor,
                  TypeListVisitor
    {
        private final Writer writer;

        public SmaliPrinter(Writer writer) {
            this.writer = writer;
        }

        @Override
        public void visitClassDef(DexFile dexFile, int index, ClassDef classDef) {
            println(".class " +
                    DexAccessFlags.formatAsHumanReadable(classDef.accessFlags, DexAccessFlags.Target.CLASS).toLowerCase() + " " +
                    DexUtil.internalTypeFromClassName(classDef.getClassName(dexFile)));
            println(".super " + classDef.getSuperClassType(dexFile));
            println(".source \"" + classDef.getSourceFile(dexFile) + "\"");
            println();

            classDef.interfacesAccept(dexFile, this);

            classDef.classDataAccept(dexFile, this);
        }

        @Override
        public void visitClassData(DexFile dexFile, ClassDef classDef, ClassData classData) {
            if (!classData.staticFields.isEmpty()) {
                println("# static fields");
                classData.staticFieldsAccept(dexFile, classDef, this);
            }

            if (!classData.instanceFields.isEmpty()) {
                println("# instance fields");
                classData.instanceFieldsAccept(dexFile, classDef, this);
            }
            println();
        }

        @Override
        public void visitInterfaces(DexFile dexFile, ClassDef classDefItem, TypeList typeList) {
            println("# interfaces");
            typeList.typesAccept(dexFile, (dexFile1, typeList1, index, type) -> println(".implements " + type));
            println();
        }

        @Override
        public void visitInstanceField(DexFile dexFile, ClassDef classDef, int index, EncodedField field) {
            println(String.format(".field %s %s:%s",
                                  DexAccessFlags.formatAsHumanReadable(field.accessFlags, DexAccessFlags.Target.FIELD).toLowerCase(),
                                  field.getName(dexFile),
                                  field.getType(dexFile)));
            println();
        }

        @Override
        public void visitStaticField(DexFile dexFile, ClassDef classDef, int index, EncodedField field) {
            println(String.format(".field %s %s:%s",
                    DexAccessFlags.formatAsHumanReadable(field.accessFlags, DexAccessFlags.Target.FIELD).toLowerCase(),
                    field.getName(dexFile),
                    field.getType(dexFile)));
            println();
        }

        // Private utility methods.

        private void println(String s) {
            try {
                writer.write(s);
                writer.write('\n');
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }

        private void println() {
            try {
                writer.write('\n');
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
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
