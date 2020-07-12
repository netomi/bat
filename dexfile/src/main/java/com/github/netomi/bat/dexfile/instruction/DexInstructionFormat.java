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
package com.github.netomi.bat.dexfile.instruction;

/**
 * @author Thomas Neidhart
 */
public enum DexInstructionFormat
{
    FORMAT_00x (1, 0),
    FORMAT_10x (1, 0),
    FORMAT_12x (1, 2),
    FORMAT_11n (1, 2),
    FORMAT_11x (1, 2),
    FORMAT_10t (1, 2),
    FORMAT_20t (2, 2),
    FORMAT_20bc(2, 2),
    FORMAT_22x (2, 2),
    FORMAT_21t (2, 2),
    FORMAT_21s (2, 2),
    FORMAT_21h (2, 2),
    FORMAT_21c (2, 2),
    FORMAT_23x (2, 2),
    FORMAT_22b (2, 2),
    FORMAT_22t (2, 2),
    FORMAT_22s (2, 2),
    FORMAT_22c (2, 2),
    FORMAT_22cs(2, 2),
    FORMAT_30t (3, 2),
    FORMAT_32x (3, 2),
    FORMAT_31i (3, 2),
    FORMAT_31t (3, 2),
    FORMAT_31c (3, 2),
    FORMAT_35c (3, 2),
    FORMAT_35ms(3, 2),
    FORMAT_35mi(3, 2),
    FORMAT_3rc (3, 2),
    FORMAT_3rms(3, 2),
    FORMAT_3rmi(3, 2),
    FORMAT_45cc(4, 2),
    FORMAT_4rcc(4, 2),
    FORMAT_51l (5, 2);

    private final int instructionLength; // length in 16-bit words
    private final int arguments;

    DexInstructionFormat(int length, int arguments) {
        this.instructionLength = length;
        this.arguments         = arguments;
    }

    public int getInstructionLength() {
        return instructionLength;
    }

    public int getArgumentCount() {
        return arguments;
    }

    public byte opCode(short[] instructions, int offset) {
        byte opCode = (byte) (instructions[offset] & 0xFF);
        return opCode;
    }
}
