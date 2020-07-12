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
package com.github.netomi.bat.dexfile;

import com.github.netomi.bat.dexfile.debug.DebugInfo;
import com.github.netomi.bat.dexfile.instruction.DexInstruction;
import com.github.netomi.bat.dexfile.io.DexDataInput;
import com.github.netomi.bat.dexfile.io.DexDataOutput;
import com.github.netomi.bat.dexfile.visitor.DataItemVisitor;
import com.github.netomi.bat.dexfile.visitor.InstructionVisitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Thomas Neidhart
 */
@DataItemAnn(
    type          = DexConstants.TYPE_CODE_ITEM,
    dataAlignment = 4,
    dataSection   = true
)
public class Code
implements   DataItem
{
    private static final short[]   EMPTY_INSTRUCTIONS = new short[0];

    public  int     registersSize;   // ushort
    public  int     insSize;         // ushort
    public  int     outsSize;        // ushort
    //public  int     triesSize;       // ushort
    private int     debugInfoOffset; // uint
    public  int     insnsSize;       // uint
    public  short[] insns;           // ushort[]
    public  int     padding;         // ushort (optional)

    public List<Try>                 tries;
    //public int                       catchHandlerSize;  // uleb128
    public List<EncodedCatchHandler> catchHandlerList;
    public DebugInfo debugInfo;

    public Code() {
        registersSize    = 0;
        insSize          = 0;
        outsSize         = 0;
        debugInfoOffset  = 0;
        insnsSize        = 0;
        insns            = EMPTY_INSTRUCTIONS;
        padding          = 0;
        tries            = Collections.emptyList();
        catchHandlerList = Collections.emptyList();
        debugInfo        = null;
    }

    public int getDebugInfoOffset() {
        return debugInfoOffset;
    }

    @Override
    public void read(DexDataInput input) {
        input.skipAlignmentPadding(getDataAlignment());

        registersSize   = input.readUnsignedShort();
        insSize         = input.readUnsignedShort();
        outsSize        = input.readUnsignedShort();
        int triesSize   = input.readUnsignedShort();
        debugInfoOffset = input.readInt();
        insnsSize       = input.readInt();

        insns = new short[insnsSize];
        for (int i = 0; i < insnsSize; i++) {
            insns[i] = input.readShort();
        }

        if (triesSize > 0 && (insnsSize % 2) == 1) {
            padding = input.readUnsignedShort();
        }

        if (triesSize > 0) {
            tries = new ArrayList<>(triesSize);
            for (int i = 0; i < triesSize; i++) {
                Try tryItem = new Try();
                tryItem.read(input);
                tries.add(tryItem);
            }

            int catchHandlerSize = input.readUleb128();
            catchHandlerList = new ArrayList<>(catchHandlerSize);
            for (int i = 0; i < catchHandlerSize; i++) {
                EncodedCatchHandler encodedCatchHandler = new EncodedCatchHandler();
                encodedCatchHandler.read(input);
                catchHandlerList.add(encodedCatchHandler);
            }
        }
    }

    @Override
    public void readLinkedDataItems(DexDataInput input) {
        if (debugInfoOffset != 0) {
            input.setOffset(debugInfoOffset);
            debugInfo = new DebugInfo();
            debugInfo.read(input);
        }
    }

    @Override
    public void updateOffsets(DataItem.Map dataItemMap) {
        debugInfoOffset = dataItemMap.getOffset(debugInfo);
    }

    @Override
    public void write(DexDataOutput output) {
        output.writeAlignmentPadding(getDataAlignment());

        output.writeUnsignedShort(registersSize);
        output.writeUnsignedShort(insSize);
        output.writeUnsignedShort(outsSize);
        output.writeUnsignedShort(tries.size());
        output.writeInt(debugInfoOffset);
        output.writeInt(insnsSize);

        for (int i = 0; i < insnsSize; i++) {
            output.writeShort(insns[i]);
        }

        if (tries.size() > 0 && (insnsSize % 2) == 1) {
            output.writeUnsignedShort(0x0);
        }

        if (tries.size() > 0) {
            for (Try tryItem : tries) {
                tryItem.write(output);
            }

            output.writeUleb128(catchHandlerList.size());
            for (EncodedCatchHandler encodedCatchHandler : catchHandlerList) {
                encodedCatchHandler.write(output);
            }
        }
    }

    public void instructionsAccept(DexFile dexFile, ClassDef classDef, ClassData classData, EncodedMethod method, Code code, InstructionVisitor visitor)
    {
        for (int offset = 0; offset < insnsSize;) {
            DexInstruction instruction = DexInstruction.create(insns, offset);
            visitor.visitInstruction(dexFile, classDef, classData, method, code, offset, instruction);
            offset += instruction.getLength();
        }
    }

    @Override
    public void dataItemsAccept(DexFile dexFile, DataItemVisitor visitor) {
        if (debugInfo != null) {
            visitor.visitDebugInfo(dexFile, this, debugInfo);
            debugInfo.dataItemsAccept(dexFile, visitor);
        }
    }
}
