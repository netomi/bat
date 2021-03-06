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

public class SparseSwitchPayload
extends      SwitchPayload
{
    private static final int[] EMPTY_ARRAY = new int[0];

    public int[] keys;
    public int[] branchTargets;

    SparseSwitchPayload(DexOpCode opcode) {
        super(opcode);

        keys           = EMPTY_ARRAY;
        branchTargets  = EMPTY_ARRAY;
    }

    @Override
    public int getLength() {
        return branchTargets.length * 4 + 2;
    }

    @Override
    public void read(short[] instructions, int offset) {
        int size = instructions[++offset] & 0xffff;

        keys = new int[size];

        for (int idx = 0; idx < size; idx++) {
            keys[idx] =
                (instructions[++offset] & 0xffff) |
                (instructions[++offset] << 16);
        }

        branchTargets = new int[size];

        for (int idx = 0; idx < size; idx++) {
            branchTargets[idx] =
                (instructions[++offset] & 0xffff) |
                (instructions[++offset] << 16);
        }
    }

    @Override
    public void accept(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, InstructionVisitor visitor) {
        visitor.visitSparseSwitchPayload(dexFile, classDef, method, code, offset, this);
    }

    @Override
    public String toString() {
        return String.format("sparse-switch-data (%d units)", getLength());
    }
}
