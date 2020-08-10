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
    type          = DexConstants.TYPE_METHOD_ID_ITEM,
    dataAlignment = 4,
    dataSection   = false
)
public class MethodID
implements   DataItem
{
    private int classIndex; // ushort
    private int protoIndex; // ushort
    private int nameIndex;  // uint;

    public static MethodID readItem(DexDataInput input) {
        MethodID methodID = new MethodID();
        methodID.read(input);
        return methodID;
    }

    private MethodID() {
        classIndex = DexConstants.NO_INDEX;
        protoIndex = DexConstants.NO_INDEX;
        nameIndex  = DexConstants.NO_INDEX;
    }

    public int getClassIndex() {
        return classIndex;
    }

    public TypeID getClassType(DexFile dexFile) {
        return dexFile.getTypeID(classIndex);
    }

    public String getClassName(DexFile dexFile) {
        return getClassType(dexFile).getType(dexFile);
    }

    public int getProtoIndex() {
        return protoIndex;
    }

    public ProtoID getProtoID(DexFile dexFile) {
        return dexFile.getProtoID(protoIndex);
    }

    public int getNameIndex() {
        return nameIndex;
    }

    public String getName(DexFile dexFile) {
        return dexFile.getStringID(nameIndex).getStringValue();
    }

    public String getShortyType(DexFile dexFile) {
        return dexFile.getProtoID(protoIndex).getShorty(dexFile);
    }

    @Override
    public void read(DexDataInput input) {
        input.skipAlignmentPadding(getDataAlignment());
        classIndex = input.readUnsignedShort();
        protoIndex = input.readUnsignedShort();
        nameIndex  = input.readInt();
    }

    @Override
    public void write(DexDataOutput output) {
        output.writeAlignmentPadding(getDataAlignment());
        output.writeUnsignedShort(classIndex);
        output.writeUnsignedShort(protoIndex);
        output.writeInt(nameIndex);
    }

    @Override
    public String toString() {
        return String.format("MethodID[classIdx=%d,typeIdx=%d,nameIdx=%d]", classIndex, protoIndex, nameIndex);
    }
}
