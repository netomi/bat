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
package com.github.netomi.bat.dexdump;

import com.github.netomi.bat.dexfile.DexFile;
import com.github.netomi.bat.dexfile.io.DexFileReader;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class FullDumpTest {

    private static final String[] DEX_FILES = {
        "all.dex",
        "bytecodes.dex",
        "checkers.dex",
        "const-method-handle.dex",
        // TODO: finish support for missing instructions.
        // "invoke-custom.dex",
        "invoke-polymorphic.dex",
        "staticfields.dex",
        "values.dex"
    };

    @Test
    public void fullDexDump() {
        for (String testFile : DEX_FILES) {
            String expectedFile = testFile.replace(".dex", ".txt");

            try (InputStream is = getClass().getResourceAsStream("/dex/" + testFile))
            {
                DexFileReader reader = new DexFileReader(is);

                DexFile dexFile = new DexFile();
                reader.visitDexFile(dexFile);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                DexDumpPrinter printer = new DexDumpPrinter(baos, true, true, true);

                dexFile.accept(printer);

                assertArrayEquals(toBytes(getClass().getResourceAsStream("/dex/" + expectedFile)), baos.toByteArray());
            } catch (IOException ex) {
                fail(ex);
            }
        }
    }

    private static byte[] toBytes(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int read;
        byte[] data = new byte[8192];

        while ((read = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, read);
        }

        return buffer.toByteArray();
    }
}
