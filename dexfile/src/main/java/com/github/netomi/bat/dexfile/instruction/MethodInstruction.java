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
import com.github.netomi.bat.dexfile.util.Primitives;
import com.github.netomi.bat.dexfile.visitor.InstructionVisitor;

public class MethodInstruction
extends      DexInstruction
{
    private int methodIndex;

    static MethodInstruction create(DexOpCode opCode, byte ident) {
        return new MethodInstruction(opCode);
    }

    MethodInstruction(DexOpCode opcode) {
        super(opcode);
        methodIndex = DexConstants.NO_INDEX;
    }

    public int getMethodIndex() {
        return methodIndex;
    }

    public MethodID getMethod(DexFile dexFile) {
        return dexFile.getMethodID(methodIndex);
    }

    @Override
    public void read(short[] instructions, int offset) {
        super.read(instructions, offset);

        switch (opcode.getFormat()) {
            case FORMAT_35c:
                methodIndex = instructions[offset + 1] & 0xffff;
                break;

            default:
                throw new IllegalStateException("unexpected format for opcode " + opcode.getMnemonic());
        }
    }

    @Override
    public void accept(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, InstructionVisitor visitor) {
        visitor.visitMethodInstruction(dexFile, classDef, method, code, offset, this);
    }

    @Override
    public String toString(DexFile dexFile, int offset) {
        StringBuilder sb = new StringBuilder();

        sb.append(getMnemonic());

        if (registers.length > 0) {
            sb.append(' ');
            sb.append('{');
            for (int idx = 0; idx < registers.length; idx++) {
                if (idx > 0) {
                    sb.append(", ");
                }
                sb.append('v');
                sb.append(registers[idx]);
            }
            sb.append('}');
        }

        sb.append(", ");

        MethodID methodID = getMethod(dexFile);

        sb.append(methodID.getClassName(dexFile));
        sb.append('.');
        sb.append(methodID.getName(dexFile));
        sb.append(':');
        sb.append(methodID.getProtoID(dexFile).getDescriptor(dexFile));

        sb.append(" // method@");
        sb.append(Primitives.asHexValue(methodIndex, 4));

        return sb.toString();
    }
}
