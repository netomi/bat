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
public class DexInstruction
{
    public DexOpCode opcode;

    public DexInstruction(DexOpCode opcode) {
        this.opcode = opcode;
    }

    public int getLength() {
        return opcode.getLength();
    }

    public static DexInstruction create(short[] instructions, int offset) {
        byte opcode = (byte) (instructions[offset] & 0xff);

        DexOpCode opCode = DexOpCode.get(opcode);

        if (opCode != null) {
            return new DexInstruction(opCode);
        }
        return null;
    }

    public String toString() {
        return opcode.getMnemonic();
    }
}
