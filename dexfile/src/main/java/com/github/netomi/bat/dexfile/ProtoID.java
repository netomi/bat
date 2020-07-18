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

import com.github.netomi.bat.dexfile.visitor.DataItemVisitor;
import com.github.netomi.bat.dexfile.io.DexDataInput;
import com.github.netomi.bat.dexfile.io.DexDataOutput;

import static com.github.netomi.bat.dexfile.DexConstants.NO_INDEX;

@DataItemAnn(
    type          = DexConstants.TYPE_PROTO_ID_ITEM,
    dataAlignment = 4,
    dataSection   = false
)
public class ProtoID
implements   DataItem
{
    public  int shortyIndex;      // uint
    public  int returnTypeIndex;  // uint
    private int parametersOffset; // uint

    public  TypeList parameters;

    public ProtoID() {
        shortyIndex      = NO_INDEX;
        returnTypeIndex  = NO_INDEX;
        parametersOffset = 0;
        parameters       = null;
    }

    public int getParametersOffset() {
        return parametersOffset;
    }

    public String getShorty(DexFile dexFile) {
        return dexFile.getStringID(shortyIndex).getStringValue();
    }

    public String getDescriptor(DexFile dexFile) {
        StringBuilder sb = new StringBuilder();
        sb.append('(');

        if (parameters != null) {
            boolean deleteFinalSeparator = false;

            for (String type : parameters.getTypes(dexFile)) {
                sb.append(type);
                sb.append(',');
                deleteFinalSeparator = true;
            }

            if (deleteFinalSeparator) {
                sb.deleteCharAt(sb.length() - 1);
            }
        }
        sb.append(')');
        sb.append(getReturnType(dexFile));
        return sb.toString();
    }

    public String getReturnType(DexFile dexFile) {
        return dexFile.getTypeID(returnTypeIndex).getType(dexFile);
    }

    @Override
    public void read(DexDataInput input) {
        input.skipAlignmentPadding(getDataAlignment());
        shortyIndex      = input.readInt();
        returnTypeIndex  = input.readInt();
        parametersOffset = input.readInt();
    }

    @Override
    public void readLinkedDataItems(DexDataInput input) {
        if (parametersOffset != 0) {
            input.setOffset(parametersOffset);
            parameters = new TypeList();
            parameters.read(input);
        }
    }

    @Override
    public void updateOffsets(DataItem.Map dataItemMap) {
        parametersOffset = dataItemMap.getOffset(parameters);
    }

    @Override
    public void write(DexDataOutput output) {
        output.writeAlignmentPadding(getDataAlignment());
        output.writeInt(shortyIndex);
        output.writeInt(returnTypeIndex);
        output.writeInt(parametersOffset);
    }

    @Override
    public void dataItemsAccept(DexFile dexFile, DataItemVisitor visitor) {
        if (parameters != null) {
            visitor.visitParameterTypes(dexFile, this, parameters);
            parameters.dataItemsAccept(dexFile, visitor);
        }
    }

    @Override
    public String toString() {
        return String.format("ProtoID[shortyIdx=%d,returnTypeIdx=%d,parameters=%s]", shortyIndex, returnTypeIndex, parameters);
    }
}
