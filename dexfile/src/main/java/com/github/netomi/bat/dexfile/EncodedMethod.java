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
import com.github.netomi.bat.dexfile.visitor.CodeVisitor;
import com.github.netomi.bat.dexfile.visitor.DataItemVisitor;

public class EncodedMethod
implements   DexContent
{
    private int deltaMethodIndex; // uleb128
    public  int methodIndex;
    public  int accessFlags;      // uleb128
    private int codeOffset;       // uleb128

    public  Code code;

    public EncodedMethod() {
        methodIndex = DexConstants.NO_INDEX;
        accessFlags = 0;
        codeOffset  = 0;
        code        = null;
    }

    public EncodedMethod(int methodIndex) {
        this.methodIndex = methodIndex;
    }

    public int getCodeOffset() {
        return codeOffset;
    }

    public MethodID getMethodID(DexFile dexFile) {
        return dexFile.getMethodID(methodIndex);
    }

    public ProtoID getProtoID(DexFile dexFile) {
        return getMethodID(dexFile).getProtoID(dexFile);
    }

    public String getName(DexFile dexFile) {
        return getMethodID(dexFile).getName(dexFile);
    }

    public String getShortyType(DexFile dexFile) {
        return getMethodID(dexFile).getProtoID(dexFile).getShorty(dexFile);
    }

    public String getDescriptor(DexFile dexFile) {
        ProtoID protoID = getProtoID(dexFile);
        return protoID.getDescriptor(dexFile);
    }

    @Override
    public void read(DexDataInput input) {
        deltaMethodIndex = input.readUleb128();
        accessFlags      = input.readUleb128();
        codeOffset       = input.readUleb128();
    }

    @Override
    public void readLinkedDataItems(DexDataInput input) {
        if (codeOffset != 0) {
            input.setOffset(codeOffset);

            code = new Code();
            code.read(input);
            code.readLinkedDataItems(input);
        }
    }

    public int updateMethodIndex(int lastIndex) {
        methodIndex = deltaMethodIndex + lastIndex;
        return methodIndex;
    }

    public int updateDeltaMethodIndex(int lastIndex) {
        deltaMethodIndex = methodIndex - lastIndex;
        return methodIndex;
    }

    @Override
    public void updateOffsets(DataItem.Map dataItemMap) {
        codeOffset = dataItemMap.getOffset(code);
    }

    @Override
    public void write(DexDataOutput output) {
        output.writeUleb128(deltaMethodIndex);
        output.writeUleb128(accessFlags);
        output.writeUleb128(codeOffset);
    }

    public void codeAccept(DexFile dexFile, ClassDef classDef, CodeVisitor visitor) {
        if (code != null) {
            visitor.visitCode(dexFile, classDef, this, code);
        }
    }

    @Override
    public void dataItemsAccept(DexFile dexFile, DataItemVisitor visitor) {
        if (code != null) {
            visitor.visitCode(dexFile, this, code);
            code.dataItemsAccept(dexFile, visitor);
        }
    }
}
