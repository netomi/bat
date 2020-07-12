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

import com.github.netomi.bat.dexfile.io.DexDataInput;
import com.github.netomi.bat.dexfile.io.DexDataOutput;
import com.github.netomi.bat.dexfile.visitor.TypeVisitor;

import java.util.Arrays;

@DataItemAnn(
    type          = DexConstants.TYPE_TYPE_LIST,
    dataAlignment = 4,
    dataSection   = false
)
public class TypeList
implements   DataItem
{
    private static final int[] EMPTY_ARRAY = new int[0];

    public int   size; // uint
    public int[] typeList;

    public TypeList() {
        this.size     = 0;
        this.typeList = EMPTY_ARRAY;
    }

    @Override
    public void read(DexDataInput input) {
        input.skipAlignmentPadding(getDataAlignment());

        size     = input.readInt();
        typeList = new int[size];
        for (int i = 0; i < size; i++) {
            int typeIndex = input.readUnsignedShort();
            typeList[i] = typeIndex;
        }
    }

    @Override
    public void write(DexDataOutput output) {
        output.writeAlignmentPadding(getDataAlignment());

        output.writeInt(size);
        for (int i = 0; i < size; i++) {
            output.writeUnsignedShort(typeList[i]);
        }
    }

    public void typesAccept(DexFile dexFile, TypeVisitor visitor) {
        for (int i = 0; i < typeList.length; i++) {
            visitor.visitType(dexFile, this, i, dexFile.getTypeID(typeList[i]).getType(dexFile));
        }
    }

    @Override
    public String toString() {
        return String.format("TypeList[size=%d,types=%s]", size, Arrays.toString(typeList));
    }
}
