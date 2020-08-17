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

import com.github.netomi.bat.dexfile.*;
import com.github.netomi.bat.dexfile.visitor.InstructionVisitor;

public class ArrayTypeInstruction
extends      ArrayInstruction
{
    private int typeIndex;

    static ArrayTypeInstruction create(DexOpCode opCode, byte ident) {
        return new ArrayTypeInstruction(opCode);
    }

    ArrayTypeInstruction(DexOpCode opcode) {
        super(opcode);
        typeIndex = DexConstants.NO_INDEX;
    }

    public int getTypeIndex() {
        return typeIndex;
    }

    public TypeID getTypeID(DexFile dexFile) {
        return dexFile.getTypeID(typeIndex);
    }

    @Override
    public void read(short[] instructions, int offset) {
        super.read(instructions, offset);

        switch (opcode.getFormat()) {
            case FORMAT_3rc:
            case FORMAT_35c:
                typeIndex = instructions[offset + 1] & 0xffff;
                break;

            default:
                throw new IllegalStateException("unexpected format for opcode " + opcode.getMnemonic());
        }
    }

    @Override
    public void accept(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, InstructionVisitor visitor) {
        visitor.visitArrayTypeInstruction(dexFile, classDef, method, code, offset, this);
    }
}
