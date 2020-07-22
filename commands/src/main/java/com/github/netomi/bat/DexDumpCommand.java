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
package com.github.netomi.bat;

import com.github.netomi.bat.dexfile.DexFile;
import com.github.netomi.bat.dexfile.io.DexFilePrinter;
import com.github.netomi.bat.dexfile.io.DexFileReader;
import com.github.netomi.bat.dexfile.visitor.ClassNameFilter;
import picocli.CommandLine;
import picocli.CommandLine.*;

import java.io.*;

/**
 * Command-line tool to dump dex files.
 */
@Command(name                 = "dexdump",
         description          = "dumps dex files.",
         parameterListHeading = "%nParameters:%n",
         optionListHeading    = "%nOptions:%n")
public class DexDumpCommand implements Runnable
{
    @Parameters(index = "0", arity = "1", paramLabel = "inputfile", description = "inputfile to process (*.dex)")
    private File inputFile;

    @Option(names = "-a", description = "print annotations")
    private boolean printAnnotations = false;

    @Option(names = "-f", description = "print file summary")
    private boolean printFileSummary = false;

    @Option(names = "-h", description = "print headers")
    private boolean printHeaders = false;

    @Option(names = "-c", description = "class to dump")
    private String classNameFilter = null;

    public void run()
    {
        try (InputStream is = new FileInputStream(inputFile)) {

            DexFileReader reader = new DexFileReader(is);

            DexFile dexFile = new DexFile();
            reader.visitDexFile(dexFile);

            if (classNameFilter != null) {
                dexFile.classDefsAccept(
                    new ClassNameFilter(classNameFilter,
                    new DexFilePrinter(System.out, printFileSummary, printHeaders, printAnnotations)));
            } else {
                dexFile.accept(new DexFilePrinter());
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args)
    {
        CommandLine cmdLine = new CommandLine(new DexDumpCommand());
        cmdLine.execute(args);
    }
}
