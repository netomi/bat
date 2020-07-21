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
import com.github.netomi.bat.dexfile.util.Primitives;
import com.github.netomi.bat.dexfile.visitor.InstructionVisitor;

import static com.github.netomi.bat.dexfile.instruction.DexInstructionFormat.FORMAT_22b;
import static com.github.netomi.bat.dexfile.instruction.DexInstructionFormat.FORMAT_22s;

public class ArithmeticInstruction
extends      DexInstruction
{
    private int value;

    static ArithmeticInstruction create(DexOpCode opCode, byte ident) {
        return new ArithmeticInstruction(opCode);
    }

    ArithmeticInstruction(DexOpCode opcode) {
        super(opcode);
    }

    public boolean containsLiteral() {
        DexInstructionFormat format = opcode.getFormat();
        return format == FORMAT_22b || format == FORMAT_22s;
    }

    public int getLiteral() {
        return value;
    }

    @Override
    public void read(short[] instructions, int offset) {
        super.read(instructions, offset);

        switch (opcode.getFormat()) {
            case FORMAT_22b:
                value = instructions[offset + 1] >> 8;
                break;

            case FORMAT_22s:
                value = instructions[offset + 1];
                break;

            case FORMAT_12x:
            case FORMAT_23x:
                break;

            default:
                throw new IllegalStateException("unexpected format for opcode " + opcode.getMnemonic());
        }
    }

    @Override
    public void accept(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, InstructionVisitor visitor) {
        visitor.visitArithmeticInstruction(dexFile, classDef, method, code, offset, this);
    }

    @Override
    public String toString(DexFile dexFile, int offset) {
        StringBuilder sb = new StringBuilder(super.toString(dexFile, offset));

        if (containsLiteral())  {
            sb.append(", ");
            sb.append("#int ");
            sb.append(value);
            sb.append(" // #");

            switch (opcode.getFormat()) {
                case FORMAT_22s:
                    sb.append(Primitives.asHexValue((short) value));
                    break;

                case FORMAT_22b:
                    sb.append(Primitives.asHexValue((byte) value));
                    break;
            }
        }

        return sb.toString();
    }
}
