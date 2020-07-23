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

package com.github.netomi.bat.dump;

import com.github.netomi.bat.dexfile.util.Mutf8;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

class      BufferedPrinter
implements AutoCloseable
{
    private final BufferedWriter out;
    private final OutputStream   outputStream;

    public BufferedPrinter(OutputStream outputStream) {
        this.out = new BufferedWriter(
                   new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), 8192);

        this.outputStream = outputStream;
    }

    @Override
    public void close() throws Exception {
        out.close();
    }

    public void printAsMutf8(String s, boolean escapeControlChars) {
        try {
            if (escapeControlChars) {
                s = s.replaceAll("\r", "\\\\r");
                s = s.replaceAll("\n", "\\\\n");
                s = s.replaceAll("\t", "\\\\t");
            }

            byte[] arr = Mutf8.encode(s);

            out.flush();
            outputStream.write(arr, 0, arr.length);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void print(String s) {
        try {
            out.write(s);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void println(String s) {
        try {
            out.write(s);
            out.write('\n');
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void println() {
        try {
            out.write('\n');
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
