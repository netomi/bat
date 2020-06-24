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

import com.github.netomi.bat.dexfile.io.DexDataInput;

/**
 * @author Thomas Neidhart
 */
public class DebugInstructionFactory
{
    private DebugInstructionFactory() {}

    public static DebugInstruction read(DexDataInput input) {
        byte opCode = input.readByte();

        DebugInstruction debugInstruction = create(opCode);

        debugInstruction.read(input);
        return debugInstruction;
    }

    private static DebugInstruction create(byte opCode) {
        switch (opCode) {
            case DebugInstruction.DBG_END_SEQUENCE:         return new DebugEndSequence();
            case DebugInstruction.DBG_ADVANCE_PC  :         return new DebugAdvancePC();
            case DebugInstruction.DBG_ADVANCE_LINE:         return new DebugAdvanceLine();
            case DebugInstruction.DBG_START_LOCAL:          return new DebugStartLocal();
            case DebugInstruction.DBG_START_LOCAL_EXTENDED: return new DebugStartLocalExtended();
            case DebugInstruction.DBG_END_LOCAL:            return new DebugEndLocal();
            case DebugInstruction.DBG_RESTART_LOCAL:        return new DebugRestartLocal();
            case DebugInstruction.DBG_SET_PROLOGUE_END:     return new DebugSetPrologueEnd();
            case DebugInstruction.DBG_SET_EPILOGUE_BEGIN:   return new DebugSetEpilogueBegin();
            case DebugInstruction.DBG_SET_FILE:             return new DebugSetFile();
            default:                       return new DebugAdvanceLineAndPC(opCode);
        }

    }
}
