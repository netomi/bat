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
package com.github.netomi.bat.io;

import java.io.IOException;
import java.io.Writer;

public class IndentingPrinter
implements   AutoCloseable
{
    private final Writer delegateWriter;

    private int     level;
    private boolean indentedLine;

    public IndentingPrinter(Writer delegateWriter) {
        this.delegateWriter = delegateWriter;
        this.level          = 0;
        this.indentedLine   = false;
    }

    public void levelUp() {
        level++;
    }

    public void levelDown() {
        level--;
    }

    public void print(CharSequence text) {
        try {
            printIndentation();
            delegateWriter.write(text.toString());
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    public void println(CharSequence text) {
        try {
            printIndentation();
            delegateWriter.write(text.toString());
            println();
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    public void println() {
        try {
            delegateWriter.write('\n');
            indentedLine = false;
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    @Override
    public void close() throws Exception {
        delegateWriter.close();
    }

    private void printIndentation() throws IOException {
        if (level > 0 && !indentedLine) {
            int spaces = level * 4;
            delegateWriter.write(String.format("%" + spaces + "s", ""));
            indentedLine = true;
        }
    }
}
