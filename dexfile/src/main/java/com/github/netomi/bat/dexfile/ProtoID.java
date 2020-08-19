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
import com.github.netomi.bat.dexfile.visitor.TypeVisitor;
import com.github.netomi.bat.util.Preconditions;

import java.util.Objects;

import static com.github.netomi.bat.dexfile.DexConstants.NO_INDEX;

/**
 * A class representing a proto id item inside a dex file.
 *
 * @see <a href="https://source.android.com/devices/tech/dalvik/dex-format#proto-id-item">proto id item @ dex format</a>
 *
 * @author Thomas Neidhart
 */
@DataItemAnn(
    type          = DexConstants.TYPE_PROTO_ID_ITEM,
    dataAlignment = 4,
    dataSection   = false
)
public class ProtoID
extends      DataItem
{
    private int shortyIndex;      // uint
    private int returnTypeIndex;  // uint
    private int parametersOffset; // uint

    private TypeList parameters;

    public static ProtoID of(int shortyIndex, int returnTypeIndex, int... parameterTypeIndices) {
        Preconditions.checkArgument(shortyIndex >= 0,     "shorty index must be non-negative");
        Preconditions.checkArgument(returnTypeIndex >= 0, "return type index must be non-negative");

        ProtoID protoID = new ProtoID(shortyIndex, returnTypeIndex);
        for (int index : parameterTypeIndices) {
            Preconditions.checkArgument(index >= 0, "parameter type index must be non-negative");
            protoID.parameters.addType(index);
        }
        return protoID;
    }

    public static ProtoID readContent(DexDataInput input) {
        ProtoID protoID = new ProtoID();
        protoID.read(input);
        return protoID;
    }

    private ProtoID() {
        this(NO_INDEX, NO_INDEX);
    }

    private ProtoID(int shortyIndex, int returnTypeIndex) {
        this.shortyIndex      = shortyIndex;
        this.returnTypeIndex  = returnTypeIndex;
        this.parameters       = TypeList.empty();
    }

    public int getParametersOffset() {
        return parametersOffset;
    }

    public int getShortyIndex() {
        return shortyIndex;
    }

    public String getShorty(DexFile dexFile) {
        return dexFile.getStringID(shortyIndex).getStringValue();
    }

    public String getDescriptor(DexFile dexFile) {
        StringBuilder sb = new StringBuilder();
        sb.append('(');

        if (parameters != null) {
            for (String type : parameters.getTypes(dexFile)) {
                sb.append(type);
            }
        }
        sb.append(')');
        sb.append(getReturnType(dexFile));
        return sb.toString();
    }

    public int getReturnTypeIndex() {
        return returnTypeIndex;
    }

    public String getReturnType(DexFile dexFile) {
        return dexFile.getTypeID(returnTypeIndex).getType(dexFile);
    }

    public TypeList getParameters() {
        return parameters;
    }

    @Override
    protected void read(DexDataInput input) {
        input.skipAlignmentPadding(getDataAlignment());
        shortyIndex      = input.readInt();
        returnTypeIndex  = input.readInt();
        parametersOffset = input.readInt();
    }

    @Override
    protected void readLinkedDataItems(DexDataInput input) {
        if (parametersOffset != 0) {
            input.setOffset(parametersOffset);
            parameters = TypeList.readContent(input);
        }
    }

    @Override
    protected void updateOffsets(DataItem.Map dataItemMap) {
        parametersOffset = dataItemMap.getOffset(parameters);
    }

    @Override
    protected void write(DexDataOutput output) {
        output.writeAlignmentPadding(getDataAlignment());
        output.writeInt(shortyIndex);
        output.writeInt(returnTypeIndex);
        output.writeInt(parametersOffset);
    }

    public void parameterTypesAccept(DexFile dexFile, TypeVisitor visitor) {
        if (parameters != null) {
            for (int i = 0; i < parameters.getTypeCount(); i++) {
                visitor.visitType(dexFile, parameters, i, parameters.getType(dexFile, i));
            }
        }
    }

    @Override
    public void dataItemsAccept(DexFile dexFile, DataItemVisitor visitor) {
        if (parameters != null) {
            visitor.visitParameterTypes(dexFile, this, parameters);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProtoID other = (ProtoID) o;
        return shortyIndex     == other.shortyIndex &&
               returnTypeIndex == other.returnTypeIndex &&
               Objects.equals(parameters, other.parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(shortyIndex, returnTypeIndex, parameters);
    }

    @Override
    public String toString() {
        return String.format("ProtoID[shortyIdx=%d,returnTypeIdx=%d,parameters=%s]", shortyIndex, returnTypeIndex, parameters);
    }
}
