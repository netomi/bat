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

public class FillArrayPayload
extends      Payload
{
    private static final byte[] EMPTY_VALUES = new byte[0];

    public int    elementWidth;
    public byte[] values;

    FillArrayPayload(DexOpCode opcode) {
        super(opcode);
        values = EMPTY_VALUES;
    }

    @Override
    public int getLength() {
        return (values.length + 1) / 2 + 4;
    }

    public int getElements() {
        return values.length / elementWidth;
    }

    public long getElement(int index) {
        int  currentIndex = index * elementWidth;
        long result       = 0;

        int shift = 0;
        for (int i = 0; i < elementWidth; i++, currentIndex++) {
            result |= (values[currentIndex] & 0xff) << shift;
            shift += 8;
        }

        return result;
    }

    @Override
    public void read(short[] instructions, int offset) {
        elementWidth = instructions[++offset] & 0xffff;

        int elements =
            (instructions[++offset] & 0xffff) |
            (instructions[++offset] << 16);

        int size = elements * elementWidth;
        values = new byte[size];

        for (int idx = 0; idx < size;) {
            values[idx++] = (byte) instructions[++offset];

            if (idx < size) {
                values[idx++] = (byte) (instructions[offset] >> 8);
            }
        }
    }

    @Override
    public void accept(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, InstructionVisitor visitor) {
        visitor.visitFillArrayPayload(dexFile, classDef, method, code, offset, this);
    }

    @Override
    public String toString() {
        return String.format("array-data (%d units)", getLength());
    }
}
