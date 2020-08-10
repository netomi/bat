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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

/**
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
    private static final int[] EMPTY_ARRAY = new int[0];

    public int   lineStart;      // uleb128
    public int   parametersSize; // uleb128
    public int[] parameterNames; // uleb128p1[]

    public List<DebugInstruction> debugSequence;

    public static DebugInfo readContent(DexDataInput input) {
        DebugInfo debugInfo = new DebugInfo();
        debugInfo.read(input);
        return debugInfo;
    }

    private DebugInfo() {
        lineStart      = 0;
        parametersSize = 0;
        parameterNames = EMPTY_ARRAY;
        debugSequence  = Collections.emptyList();
    }

    public int getParameterCount() {
        return parameterNames.length;
    }

    public String getParameterName(DexFile dexFile, int parameterIndex) {
        return dexFile.getString(parameterNames[parameterIndex]);
    }

    @Override
    protected void read(DexDataInput input) {
        lineStart      = input.readUleb128();
        parametersSize = input.readUleb128();
        parameterNames = new int[parametersSize];
        for (int i = 0; i < parametersSize; i++) {
            parameterNames[i] = input.readUleb128p1();
        }

        debugSequence = new ArrayList<>();
        DebugInstruction debugInstruction;
        do {
            debugInstruction = DebugInstruction.readInstruction(input);
            debugSequence.add(debugInstruction);
        } while (debugInstruction.getOpcode() != DebugInstruction.DBG_END_SEQUENCE);
    }

    @Override
    protected void write(DexDataOutput output) {
        output.writeUleb128(lineStart);
        output.writeUleb128(parametersSize);
        for (int parameterIndex : parameterNames) {
            output.writeUleb128p1(parameterIndex);
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
}
