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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

class      BufferedPrinter
implements AutoCloseable
{
    private final BufferedWriter out;

    public BufferedPrinter(OutputStream outputStream) {
        out = new BufferedWriter(
              new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), 8192);
    }

    @Override
    public void close() throws Exception {
        out.close();
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
