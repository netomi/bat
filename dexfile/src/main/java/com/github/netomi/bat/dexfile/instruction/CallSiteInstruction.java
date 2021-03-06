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

public class CallSiteInstruction
extends      DexInstruction
{
    private int callSiteIndex;

    static CallSiteInstruction create(DexOpCode opCode, byte ident) {
        return new CallSiteInstruction(opCode);
    }

    CallSiteInstruction(DexOpCode opcode) {
        super(opcode);
        callSiteIndex = DexConstants.NO_INDEX;
    }

    public int getCallSiteIndex() {
        return callSiteIndex;
    }

    public CallSiteID getCallSiteID(DexFile dexFile) {
        return dexFile.getCallSiteID(callSiteIndex);
    }

    @Override
    public void read(short[] instructions, int offset) {
        super.read(instructions, offset);

        switch (opcode.getFormat()) {
            case FORMAT_3rc:
            case FORMAT_35c:
                callSiteIndex = instructions[offset + 1] & 0xffff;
                break;

            default:
                throw new IllegalStateException("unexpected format for opcode " + opcode.getMnemonic());
        }
    }

    @Override
    public void accept(DexFile dexFile, ClassDef classDef, EncodedMethod method, Code code, int offset, InstructionVisitor visitor) {
        visitor.visitCallSiteInstruction(dexFile, classDef, method, code, offset, this);
    }
}
