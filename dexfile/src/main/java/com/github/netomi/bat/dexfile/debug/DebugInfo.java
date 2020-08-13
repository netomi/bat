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
import com.github.netomi.bat.util.IntArray;

import java.util.*;

/**
 * A class representing a debug info item inside a dex file.
 *
 * @see <a href="https://source.android.com/devices/tech/dalvik/dex-format#debug-info-item">debug info item @ dex format</a>
 *
 * @author Thomas Neidhart
 */
@DataItemAnn(
    type          = DexConstants.TYPE_DEBUG_INFO_ITEM,
    dataAlignment = 1,
    dataSection   = true
)
public class DebugInfo
extends      DataItem
{
    private int      lineStart;      // uleb128
    // private int   parametersSize; // uleb128
    private IntArray parameterNames; // uleb128p1[]

    private ArrayList<DebugInstruction> debugSequence;

    public static DebugInfo readContent(DexDataInput input) {
        DebugInfo debugInfo = new DebugInfo();
        debugInfo.read(input);
        return debugInfo;
    }

    private DebugInfo() {
        lineStart      = 0;
        parameterNames = new IntArray(0);
        debugSequence  = new ArrayList<>(0);
    }

    public int getLineStart() {
        return lineStart;
    }

    public int getParameterCount() {
        return parameterNames.size();
    }

    public String getParameterName(DexFile dexFile, int parameterIndex) {
        return dexFile.getString(parameterNames.get(parameterIndex));
    }

    @Override
    protected void read(DexDataInput input) {
        lineStart          = input.readUleb128();
        int parametersSize = input.readUleb128();
        parameterNames.resize(parametersSize);
        for (int i = 0; i < parametersSize; i++) {
            parameterNames.add(i, input.readUleb128p1());
        }

        DebugInstruction debugInstruction;
        do {
            debugInstruction = DebugInstruction.readInstruction(input);
            debugSequence.add(debugInstruction);
        } while (debugInstruction.getOpcode() != DebugInstruction.DBG_END_SEQUENCE);
    }

    @Override
    protected void write(DexDataOutput output) {
        output.writeUleb128(lineStart);
        output.writeUleb128(parameterNames.size());
        for (int i = 0; i < parameterNames.size(); i++) {
            output.writeUleb128p1(parameterNames.get(i));
        }

        for (DebugInstruction debugInstruction : debugSequence) {
            debugInstruction.writeInternal(output);
        }
    }

    public void debugSequenceAccept(DexFile              dexFile,
                                    DebugSequenceVisitor visitor) {
        for (DebugInstruction debugInstruction : debugSequence) {
            debugInstruction.accept(dexFile, this, visitor);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DebugInfo other = (DebugInfo) o;
        return lineStart == other.lineStart &&
               Objects.equals(parameterNames, other.parameterNames) &&
               Objects.equals(debugSequence,  other.debugSequence);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lineStart, parameterNames, debugSequence);
    }

    @Override
    public String toString() {
        return String.format("DebugInfo[lineStart=%d,parameterNames=%s,debugSequence=%d]",
                             lineStart,
                             parameterNames,
                             debugSequence.size());
    }
}
