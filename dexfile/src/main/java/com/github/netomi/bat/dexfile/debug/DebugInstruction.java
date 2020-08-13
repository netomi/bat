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
package com.github.netomi.bat.dexfile.debug;

import com.github.netomi.bat.dexfile.*;
import com.github.netomi.bat.dexfile.io.DexDataInput;
import com.github.netomi.bat.dexfile.io.DexDataOutput;
import com.github.netomi.bat.dexfile.visitor.DebugSequenceVisitor;

/**
 * Represents a debug instruction as contained in the debug sequence of a debug info item.
 *
 * @author Thomas Neidhart
 */
public abstract class DebugInstruction
extends               DexContent
{
    static final byte DBG_END_SEQUENCE         = 0x00;
    static final byte DBG_ADVANCE_PC           = 0x01;
    static final byte DBG_ADVANCE_LINE         = 0x02;
    static final byte DBG_START_LOCAL          = 0x03;
    static final byte DBG_START_LOCAL_EXTENDED = 0x04;
    static final byte DBG_END_LOCAL            = 0x05;
    static final byte DBG_RESTART_LOCAL        = 0x06;
    static final byte DBG_SET_PROLOGUE_END     = 0x07;
    static final byte DBG_SET_EPILOGUE_BEGIN   = 0x08;
    static final byte DBG_SET_FILE             = 0x09;
    static final byte DBG_FIRST_SPECIAL        = 0x0a;

    static final int  DBG_LINE_BASE  = -4;
    static final int  DBG_LINE_RANGE = 15;

    private final byte opcode;

    protected DebugInstruction(byte opcode) {
        this.opcode = opcode;
    }

    public byte getOpcode() {
        return opcode;
    }

    void writeInternal(DexDataOutput output) {
        write(output);
    }

    public abstract void accept(DexFile              dexFile,
                                DebugInfo            debugInfo,
                                DebugSequenceVisitor visitor);

    public static DebugInstruction readInstruction(DexDataInput input) {
        byte opCode = input.readByte();

        DebugInstruction debugInstruction = create(opCode);

        debugInstruction.read(input);
        return debugInstruction;
    }

    private static DebugInstruction create(byte opCode) {
        switch (opCode) {
            case DebugInstruction.DBG_END_SEQUENCE:         return DebugEndSequence.instance();
            case DebugInstruction.DBG_ADVANCE_PC  :         return new DebugAdvancePC();
            case DebugInstruction.DBG_ADVANCE_LINE:         return new DebugAdvanceLine();
            case DebugInstruction.DBG_START_LOCAL:          return new DebugStartLocal();
            case DebugInstruction.DBG_START_LOCAL_EXTENDED: return new DebugStartLocalExtended();
            case DebugInstruction.DBG_END_LOCAL:            return new DebugEndLocal();
            case DebugInstruction.DBG_RESTART_LOCAL:        return new DebugRestartLocal();
            case DebugInstruction.DBG_SET_PROLOGUE_END:     return DebugSetPrologueEnd.instance();
            case DebugInstruction.DBG_SET_EPILOGUE_BEGIN:   return DebugSetEpilogueBegin.instance();
            case DebugInstruction.DBG_SET_FILE:             return new DebugSetFile();
            default:                                        return new DebugAdvanceLineAndPC(opCode);
        }

    }
}
