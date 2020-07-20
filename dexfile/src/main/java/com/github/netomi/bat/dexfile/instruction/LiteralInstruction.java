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

public class LiteralInstruction
extends      DexInstruction
{
    private long value;

    static LiteralInstruction create(DexOpCode opCode, byte ident) {
        return new LiteralInstruction(opCode);
    }

    LiteralInstruction(DexOpCode opcode) {
        super(opcode);
        value = Long.MAX_VALUE;
    }

    public long getValue() {
        return value;
    }

    public float asFloat() {
        return Float.intBitsToFloat((int) value);
    }

    public double asDouble() {
        return Double.longBitsToDouble(value);
    }

    @Override
    public void read(short[] instructions, int offset) {
        super.read(instructions, offset);

        switch (opcode.getFormat()) {
            case FORMAT_11n:
                value = instructions[offset] >> 12;
                break;

            case FORMAT_21s:
                value = instructions[offset + 1];
                break;

            case FORMAT_31i:
                value =
                    (instructions[offset + 1] & 0xffff) |
                    (instructions[offset + 2] << 16);
                break;

            case FORMAT_21h:
                value = opcode.isWide() ?
                    ((long)instructions[offset + 1] << 48) :
                    (instructions[offset + 1] << 16);
                break;

            case FORMAT_51l:
                value =
                    (instructions[offset + 1] & 0xffff)               |
                    ((long)(instructions[offset + 2] & 0xffff) << 16) |
                    ((long)(instructions[offset + 3] & 0xffff) << 32) |
                    ((long)(instructions[offset + 4] & 0xffff) << 48);
                break;

            default:
                throw new IllegalStateException("unexpected format for opcode " + opcode.getMnemonic());
        }
    }

    @Override
    public void accept(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, InstructionVisitor visitor) {
        visitor.visitLiteralInstruction(dexFile, classDef, method, code, offset, this);
    }

    @Override
    public String toString(DexFile dexFile, int offset) {
        StringBuilder sb = new StringBuilder(super.toString(dexFile, offset));

        sb.append(", ");

        if (opcode.isWide()) {
            sb.append("#long ");
            sb.append(value);
            sb.append(" #double ");
            sb.append(asDouble());

            sb.append(" // #");
            sb.append(Long.toHexString(value));
        } else {
            sb.append("#int ");
            sb.append(value);
            sb.append(" #float ");
            sb.append(asFloat());

            sb.append(" // #");
            sb.append(Long.toHexString(value));
        }

        return sb.toString();
    }
}
