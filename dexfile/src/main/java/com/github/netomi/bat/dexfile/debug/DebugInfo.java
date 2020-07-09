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

import com.github.netomi.bat.dexfile.DataItem;
import com.github.netomi.bat.dexfile.DataItemAnn;
import com.github.netomi.bat.dexfile.DexConstants;
import com.github.netomi.bat.dexfile.io.DexDataInput;
import com.github.netomi.bat.dexfile.io.DexDataOutput;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Thomas Neidhart
 */
@DataItemAnn(
    type          = DexConstants.TYPE_DEBUG_INFO_ITEM,
    dataAlignment = 1,
    dataSection   = true
)
public class DebugInfo
implements   DataItem
{
    private static final int[] EMPTY_ARRAY = new int[0];

    public int   lineStart;      // uleb128
    public int   parametersSize; // uleb128
    public int[] parameterNames; // uleb128p1[]

    public List<DebugInstruction> debugSequence;

    public DebugInfo() {
        lineStart      = 0;
        parametersSize = 0;
        parameterNames = EMPTY_ARRAY;
        debugSequence  = Collections.emptyList();
    }

    @Override
    public void read(DexDataInput input) {
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
    public void write(DexDataOutput output) {
        output.writeUleb128(lineStart);
        output.writeUleb128(parametersSize);
        for (int parameterIndex : parameterNames) {
            output.writeUleb128p1(parameterIndex);
        }

        for (DebugInstruction debugInstruction : debugSequence) {
            debugInstruction.write(output);
        }
    }
}
