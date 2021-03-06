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

import com.github.netomi.bat.dexfile.ClassDef;
import com.github.netomi.bat.dexfile.Code;
import com.github.netomi.bat.dexfile.DexFile;
import com.github.netomi.bat.dexfile.EncodedMethod;
import com.github.netomi.bat.dexfile.visitor.InstructionVisitor;

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
            case FORMAT_10t:
                branchOffset = instructions[offset] >> 8;
                break;

            case FORMAT_20t:
            case FORMAT_21t:
            case FORMAT_22t:
                branchOffset = instructions[offset + 1];
                break;

            case FORMAT_30t:
            case FORMAT_31t:
                branchOffset =
                    (instructions[offset + 1] & 0xffff) |
                    (instructions[offset + 2] << 16);
                break;

            default:
                throw new IllegalStateException("unexpected format for opcode " + opcode.getMnemonic());
        }
    }

    @Override
    public void accept(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, InstructionVisitor visitor) {
        visitor.visitBranchInstruction(dexFile, classDef, method, code, offset, this);
    }
}
