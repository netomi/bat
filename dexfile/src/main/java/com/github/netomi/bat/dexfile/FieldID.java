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

@DataItemAnn(
    type          = DexConstants.TYPE_FIELD_ID_ITEM,
    dataAlignment = 4,
    dataSection   = false
)
public class FieldID extends DataItem
{
    public int classIndex; // ushort
    public int typeIndex;  // ushort
    public int nameIndex;  // uint;

    public FieldID() {
        classIndex = DexConstants.NO_INDEX;
        typeIndex  = DexConstants.NO_INDEX;
        nameIndex  = DexConstants.NO_INDEX;
    }

    public ClassDef getClassDef(DexFile dexFile) {
        return dexFile.getClassDef(classIndex);
    }

    public String getClassName(DexFile dexFile) {
        return getClassDef(dexFile).getClassName(dexFile);
    }

    public String getType(DexFile dexFile) {
        return dexFile.getTypeID(typeIndex).getType(dexFile);
    }

    public String getName(DexFile dexFile) {
        return dexFile.getStringID(nameIndex).getStringValue();
    }

    @Override
    public void read(DexDataInput input) {
        input.skipAlignmentPadding(getDataAlignment());
        classIndex = input.readUnsignedShort();
        typeIndex  = input.readUnsignedShort();
        nameIndex  = input.readInt();
    }

    @Override
    public void write(DexDataOutput output) {
        output.writeAlignmentPadding(getDataAlignment());
        output.writeUnsignedShort(classIndex);
        output.writeUnsignedShort(typeIndex);
        output.writeInt(nameIndex);
    }

    @Override
    public String toString() {
        return String.format("FieldID[classIdx=%d,typeIdx=%d,nameIdx=%d]", classIndex, typeIndex, nameIndex);
    }
}
