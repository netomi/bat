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
package com.github.netomi.bat.test;

import com.github.netomi.bat.dexfile.DexFile;
import com.github.netomi.bat.dexfile.io.DexFileReader;
import com.github.netomi.bat.dexfile.io.DexFileWriter;
import com.github.netomi.bat.dexfile.io.DexFilePrinter;

import java.io.*;

public class Test {

    public static void main(String[] args) {
        DexFile dexFile = new DexFile();

        try (InputStream  is = new FileInputStream("classes-io.dex");
             OutputStream os = new FileOutputStream("classes2.dex")) {

            DexFileReader reader = new DexFileReader(is);

            reader.visitDexFile(dexFile);

            dexFile.accept(new DexFilePrinter(new FileOutputStream("test.log")));
            //dexFile.accept(new DexFilePrinter());

//            DexFileWriter writer = new DexFileWriter(os);
//
//            dexFile.accept(writer);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
