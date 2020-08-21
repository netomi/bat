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
package com.github.netomi.bat.smali.disassemble;

import com.github.netomi.bat.dexfile.*;
import com.github.netomi.bat.io.IndentingPrinter;

import java.util.HashSet;
import java.util.Set;

class TryPrinter
{
    private TryPrinter() {}

    public static void printTryCatchLabels(Code code, int offset, IndentingPrinter printer) {
        Set<String> labels = new HashSet<>();

        for (Try currentTry : code.tries) {
            if (currentTry.getStartAddr() == offset) {
                labels.add(":try_start_" + Integer.toHexString(offset));
            }

            EncodedCatchHandler catchHandler = currentTry.getCatchHandler();
            if (catchHandler.getCatchAllAddr() == offset) {
                labels.add(":catchall_" + Integer.toHexString(offset));
            }

            for (TypeAddrPair addrPair : catchHandler.getHandlers()) {
                if (addrPair.getAddress() == offset) {
                    labels.add(":catch_" + Integer.toHexString(offset));
                }
            }
        }

        for (String label : labels) {
            printer.println(label);
        }
    }

    public static void printTryEndLabel(DexFile dexFile, Code code, int offset, int instructionLength, IndentingPrinter printer) {
        for (Try currentTry : code.tries) {
            if (currentTry.getEndAddr() == (offset + instructionLength - 1)) {
                printer.println(":try_end_" + Integer.toHexString(offset + instructionLength));

                EncodedCatchHandler catchHandler = currentTry.getCatchHandler();

                for (TypeAddrPair addrPair : catchHandler.getHandlers()) {
                    printer.println(".catch " + addrPair.getType(dexFile) +
                                    " {:try_start_" + Integer.toHexString(currentTry.getStartAddr()) + " .. " +
                                    ":try_end_" + Integer.toHexString(offset + instructionLength) + "} " +
                                    ":catch_" + Integer.toHexString(addrPair.getAddress()));
                }

                if (catchHandler.getCatchAllAddr() != -1) {
                    printer.println(".catchall " +
                                    "{:try_start_" + Integer.toHexString(currentTry.getStartAddr()) + " .. " +
                                    ":try_end_" + Integer.toHexString(offset + instructionLength) + "} " +
                                    ":catchall_" + Integer.toHexString(catchHandler.getCatchAllAddr()));
                }
            }
        }
    }
}
