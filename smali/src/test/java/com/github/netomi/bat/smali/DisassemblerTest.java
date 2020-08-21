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

import com.github.netomi.bat.dexfile.DexFile;
import com.github.netomi.bat.dexfile.io.DexFileReader;
import com.github.netomi.bat.io.OutputStreamFactory;
import com.github.netomi.bat.util.Arrays;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.*;

public class DisassemblerTest {

    private static final String[] DEX_FILES = {
        "all.dex",
        "bytecodes.dex",
        "checkers.dex",
        "const-method-handle.dex",
        "invoke-custom.dex",
        "invoke-polymorphic.dex",
        "staticfields.dex",
        "values.dex"
    };

    @Test
    public void batchDissamble() {
        for (String testFile : DEX_FILES) {
            try (InputStream is = getClass().getResourceAsStream("/dex/" + testFile))
            {
                DexFileReader reader = new DexFileReader(is);

                DexFile dexFile = new DexFile();
                reader.visitDexFile(dexFile);

                TestOutputStreamFactory outputStreamFactory = new TestOutputStreamFactory();
                Disassembler disassembler = new Disassembler(outputStreamFactory);

                dexFile.classDefsAccept(disassembler);

                String expectedArchive = testFile.replaceAll(".dex", ".zip");
                try (ZipInputStream zipInputStream = new ZipInputStream(getClass().getResourceAsStream("/dex/" + expectedArchive))) {
                    ZipEntry zipEntry;
                    while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                        if (zipEntry.getName().endsWith(".smali")) {
                            String className = zipEntry.getName().replaceAll(".smali", "");
                            System.out.println("comparing " + className + "...");

                            byte[] expectedBytes = readContent(zipInputStream, zipEntry);
                            byte[] actualBytes = outputStreamFactory.getOutputStream(className).toByteArray();

                            // testing purposes only.
                            if (!Arrays.equals(expectedBytes, actualBytes, expectedBytes.length)) {
                                Files.write(Paths.get("expected.smali"), expectedBytes);
                                Files.write(Paths.get("actual.smali"), actualBytes);
                            }

                            assertArrayEquals(expectedBytes, actualBytes);
                        }
                        zipInputStream.closeEntry();
                    }
                }
            } catch (IOException ex) {
                fail(ex);
            }
        }
    }

    private static byte[] readContent(ZipInputStream inputStream, ZipEntry entry) throws IOException {
        byte[] buffer = new byte[8192];

        ByteArrayOutputStream baos =
                new ByteArrayOutputStream((int) entry.getSize());

        int len;
        while ((len = inputStream.read(buffer)) > 0) {
            baos.write(buffer, 0, len);
        }

        return baos.toByteArray();
    }

    private static class TestOutputStreamFactory
    implements           OutputStreamFactory
    {
        private final Map<String, ByteArrayOutputStream> outputStreamMap;

        public TestOutputStreamFactory() {
            outputStreamMap = new TreeMap<>();
        }

        public Iterable<String> getClassNames() {
            return outputStreamMap.keySet();
        }

        public ByteArrayOutputStream getOutputStream(String className) {
            return outputStreamMap.get(className);
        }

        @Override
        public OutputStream createOutputStream(String className) throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            outputStreamMap.put(className, baos);
            return baos;
        }
    }

}
