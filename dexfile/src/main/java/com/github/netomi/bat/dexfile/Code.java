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
import com.github.netomi.bat.dexfile.visitor.TryVisitor;

import java.util.*;

/**
 * @author Thomas Neidhart
 */
@DataItemAnn(
    type          = DexConstants.TYPE_CODE_ITEM,
    dataAlignment = 4,
    dataSection   = true
)
public class Code
extends      DataItem
{
    private static final short[]   EMPTY_INSTRUCTIONS = new short[0];

    public  int     registersSize;   // ushort
    public  int     insSize;         // ushort
    public  int     outsSize;        // ushort
    //public  int     triesSize;       // ushort
    private int     debugInfoOffset; // uint
    public  int     insnsSize;       // uint
    public  short[] insns;           // ushort[]
    //public  int     padding;         // ushort (optional)

    public List<Try>                 tries;
    //public int                       catchHandlerSize;  // uleb128
    public List<EncodedCatchHandler> catchHandlerList;
    public DebugInfo                 debugInfo;

    public static Code readItem(DexDataInput input) {
        Code code = new Code();
        code.read(input);
        return code;
    }

    private Code() {
        registersSize    = 0;
        insSize          = 0;
        outsSize         = 0;
        debugInfoOffset  = 0;
        insnsSize        = 0;
        insns            = EMPTY_INSTRUCTIONS;
        tries            = Collections.emptyList();
        catchHandlerList = Collections.emptyList();
        debugInfo        = null;
    }

    public int getDebugInfoOffset() {
        return debugInfoOffset;
    }

    @Override
    protected void read(DexDataInput input) {
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
            // read padding
            input.readUnsignedShort();
        }

        if (triesSize > 0) {
            tries = new ArrayList<>(triesSize);
            for (int i = 0; i < triesSize; i++) {
                Try tryItem = new Try();
                tryItem.read(input);
                tries.add(tryItem);
            }

            int startOffset = input.getOffset();

            int catchHandlerSize = input.readUleb128();
            catchHandlerList = new ArrayList<>(catchHandlerSize);
            HashMap<Integer, EncodedCatchHandler> offsetMap = new HashMap<>();

            for (int i = 0; i < catchHandlerSize; i++) {
                int currentOffset = input.getOffset();

                EncodedCatchHandler encodedCatchHandler = new EncodedCatchHandler();
                encodedCatchHandler.read(input);
                catchHandlerList.add(encodedCatchHandler);
                offsetMap.put(currentOffset - startOffset, encodedCatchHandler);
            }

            // initialize the associated catch handlers for each try
            for (Try currentTry : tries) {
                currentTry.catchHandler = offsetMap.get(currentTry.getHandlerOffset());
            }
        }
    }

    @Override
    protected void readLinkedDataItems(DexDataInput input) {
        if (debugInfoOffset != 0) {
            input.setOffset(debugInfoOffset);
            debugInfo = DebugInfo.readContent(input);
        }
    }

    @Override
    protected void updateOffsets(DataItem.Map dataItemMap) {
        debugInfoOffset = dataItemMap.getOffset(debugInfo);
    }

    @Override
    protected void write(DexDataOutput output) {
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
            // TODO: correctly update handler offset in Try
            for (Try tryItem : tries) {
                tryItem.write(output);
            }

            output.writeUleb128(catchHandlerList.size());
            for (EncodedCatchHandler encodedCatchHandler : catchHandlerList) {
                encodedCatchHandler.write(output);
            }
        }
    }

    public void instructionsAccept(DexFile            dexFile,
                                   ClassDef           classDef,
                                   EncodedMethod      method,
                                   Code               code,
                                   InstructionVisitor visitor) {
        for (int offset = 0; offset < insnsSize;) {
            DexInstruction instruction = DexInstruction.create(insns, offset);
            instruction.accept(dexFile, classDef, method, code, offset, visitor);
            offset += instruction.getLength();
        }
    }

    public void triesAccept(DexFile       dexFile,
                            ClassDef      classDef,
                            EncodedMethod method,
                            Code          code,
                            TryVisitor    visitor) {
        ListIterator<Try> it = tries.listIterator();

        while (it.hasNext()) {
            int index = it.nextIndex();
            Try currentTry = it.next();

            visitor.visitTry(dexFile, classDef, method, code, index, currentTry);
        }
    }

    @Override
    public void dataItemsAccept(DexFile dexFile, DataItemVisitor visitor) {
        if (debugInfo != null) {
            visitor.visitDebugInfo(dexFile, this, debugInfo);
        }
    }
}
