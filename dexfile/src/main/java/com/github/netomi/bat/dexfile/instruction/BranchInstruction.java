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

import com.github.netomi.bat.dexfile.DexFile;
import com.github.netomi.bat.dexfile.util.Primitives;

public class BranchInstruction
extends      DexInstruction
{
    private int branchOffset;

    static BranchInstruction create(DexOpCode opCode, byte ident) {
        return new BranchInstruction(opCode);
    }

    BranchInstruction(DexOpCode opcode) {
        super(opcode);
        branchOffset = 0;
    }

    public int getBranchOffset() {
        return branchOffset;
    }

    @Override
    public void read(short[] instructions, int offset) {
        super.read(instructions, offset);

        switch (opcode.getFormat()) {
            case FORMAT_21t:
            case FORMAT_22t:
                branchOffset = instructions[offset + 1];
                break;

            default:
                throw new IllegalStateException("unexpected format for opcode " + opcode.getMnemonic());
        }
    }

    @Override
    public String toString(DexFile dexFile, int offset) {
        StringBuilder sb = new StringBuilder(super.toString(dexFile, offset));

        sb.append(", ");
        sb.append(Primitives.asHexValue(offset + branchOffset, 4));
        sb.append(" // ");

        if (branchOffset < 0) {
            sb.append('-');
            sb.append(Primitives.asHexValue(-branchOffset, 4));
        } else {
            sb.append(Primitives.asHexValue(branchOffset, 4));
        }

        return sb.toString();
    }
}
