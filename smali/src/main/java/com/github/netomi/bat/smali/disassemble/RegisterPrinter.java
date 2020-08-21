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

import com.github.netomi.bat.dexfile.Code;
import com.github.netomi.bat.io.IndentingPrinter;

public class RegisterPrinter
{
    private final int localRegisters;

    public RegisterPrinter(Code code) {
        localRegisters = code.registersSize - code.insSize;
    }

    public String formatRegister(int registerNum) {
        String registerPrefix = registerNum < localRegisters ? "v" : "p";
        int    registerIndex  = registerNum < localRegisters ?
                registerNum :
                registerNum - localRegisters;

        return registerPrefix + registerIndex;
    }

    public void printRegister(IndentingPrinter printer, int registerNum) {
        printer.print(formatRegister(registerNum));
    }
}