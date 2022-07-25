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
package com.github.netomi.bat.smali.disassemble

import com.github.netomi.bat.dexfile.Code
import com.github.netomi.bat.dexfile.DexFile
import com.github.netomi.bat.io.IndentingPrinter

internal object TryCatchPrinter {

    @JvmStatic
    fun printTryCatchLabels(code: Code, offset: Int, printer: IndentingPrinter) {
        val labels: MutableSet<String> = HashSet()

        for (currentTry in code.tryList) {
            if (currentTry.startAddr == offset) {
                labels.add(":try_start_" + Integer.toHexString(offset))
            }

            val catchHandler = currentTry.catchHandler
            if (catchHandler.catchAllAddr == offset) {
                labels.add(":catchall_" + Integer.toHexString(offset))
            }

            for (addrPair in catchHandler.handlers) {
                if (addrPair.address == offset) {
                    labels.add(":catch_" + Integer.toHexString(offset))
                }
            }
        }

        for (label in labels) {
            printer.println(label)
        }
    }

    @JvmStatic
    fun printTryEndLabel(dexFile: DexFile, code: Code, offset: Int, instructionLength: Int, printer: IndentingPrinter) {
        for (currentTry in code.tryList) {
            if (currentTry.endAddr == offset + instructionLength - 1) {
                printer.println(":try_end_" + Integer.toHexString(offset + instructionLength))
                val catchHandler = currentTry.catchHandler
                for (addrPair in catchHandler.handlers) {
                    printer.println(".catch " + addrPair.getType(dexFile) +
                                    " {:try_start_" + Integer.toHexString(currentTry.startAddr) + " .. " +
                                    ":try_end_" + Integer.toHexString(offset + instructionLength) + "} " +
                                    ":catch_" + Integer.toHexString(addrPair.address)
                    )
                }
                if (catchHandler.catchAllAddr != -1) {
                    printer.println(".catchall " +
                                    "{:try_start_" + Integer.toHexString(currentTry.startAddr) + " .. " +
                                    ":try_end_" + Integer.toHexString(offset + instructionLength) + "} " +
                                    ":catchall_" + Integer.toHexString(catchHandler.catchAllAddr)
                    )
                }
            }
        }
    }
}