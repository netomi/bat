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

public class BasicInstruction
extends      DexInstruction
{
    static DexInstruction create(DexOpCode opCode, byte ident) {
        switch (opCode) {
            case NOP:
                if (ident == 0x01) {
                    return new PackedSwitchPayload(opCode);
                } else if (ident == 0x02) {
                    return new SparseSwitchPayload(opCode);
                } else if (ident == 0x03) {
                    return new FillArrayPayload(opCode);
                } else {
                    return new BasicInstruction(opCode);
                }

            default:
                return new BasicInstruction(opCode);
        }
    }

    BasicInstruction(DexOpCode opcode) {
        super(opcode);
    }

    @Override
    public void read(short[] instructions, int offset) {
        super.read(instructions, offset);
    }

    @Override
    public void accept(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, InstructionVisitor visitor) {
        visitor.visitBasicInstruction(dexFile, classDef, method, code, offset, this);
    }
}
