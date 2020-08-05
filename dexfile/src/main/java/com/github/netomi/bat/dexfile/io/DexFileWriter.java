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
import com.github.netomi.bat.dexfile.visitor.DexFileVisitor;

import java.io.IOException;
import java.io.OutputStream;

public class DexFileWriter
implements   DexFileVisitor
{
    private final OutputStream outputStream;

    public DexFileWriter(OutputStream out) {
        this.outputStream = out;
    }

    @Override
    public void visitDexFile(DexFile dexFile) {
        try {
            dexFile.write(outputStream);
        } catch (IOException ioe) {
            throw new RuntimeException("Failed to write dex file", ioe);
        }
    }
}
