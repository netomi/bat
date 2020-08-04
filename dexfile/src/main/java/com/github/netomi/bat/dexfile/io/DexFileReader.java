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

package com.github.netomi.bat.dexfile.io;

import com.github.netomi.bat.dexfile.*;
import com.github.netomi.bat.dexfile.visitor.DataItemVisitor;
import com.github.netomi.bat.dexfile.visitor.DexFileVisitor;
import com.github.netomi.bat.util.Primitives;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.Adler32;

public class DexFileReader
implements   DexFileVisitor
{
    private final DexDataInput input;
    private final boolean      strictParsing;

    public DexFileReader(InputStream in) throws IOException {
        this(in, true);
    }

    public DexFileReader(InputStream in, boolean strict) throws IOException {
        input         = new DexDataInput(in);
        strictParsing = strict;
    }

    @Override
    public void visitDexFile(DexFile dexFile) {
        dexFile.read(input);

        if (strictParsing) {
            verifyChecksum(dexFile);
        }
    }


    private void verifyChecksum(DexFile dexFile) {
        input.setOffset(12);

        Adler32 adler32 = new Adler32();
        input.update(adler32);

        long checksum = adler32.getValue();
        if (checksum != dexFile.header.checksum) {
            throw new DexFormatException(String.format("Calculated checksum [%s] does not match [%s].",
                                                       Primitives.toHexString(checksum),
                                                       Primitives.toHexString(dexFile.header.checksum)));
        }
    }
}
