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

public class EncodedMethod
implements   DexContent
{
    public int methodIndex; // uleb128
    public int accessFlags; // uleb128
    public int codeOffset;  // uleb128

    public Code code;

    public EncodedMethod() {
        methodIndex = DexConstants.NO_INDEX;
        accessFlags = 0;
        codeOffset  = 0;
        code        = null;
    }

    public EncodedMethod(int methodIndex) {
        this.methodIndex = methodIndex;
    }

    public MethodID getMethodIDItem(DexFile dexFile) {
        return dexFile.getMethodID(methodIndex);
    }

    public String getName(DexFile dexFile) {
        return getMethodIDItem(dexFile).getName(dexFile);
    }

    public String getType(DexFile dexFile) {
        return getMethodIDItem(dexFile).getProtoID(dexFile).getShorty(dexFile);
    }

    @Override
    public void read(DexDataInput input) {
        int methodIndexDiff = input.readUleb128();
        methodIndex = methodIndexDiff + input.getLastMemberIndex();
        input.setLastMemberIndex(methodIndex);

        accessFlags = input.readUleb128();
        codeOffset  = input.readUleb128();
    }

    @Override
    public void write(DexDataOutput output) {
        int methodIndexDiff = methodIndex - output.getLastMemberIndex();
        output.writeUleb128(methodIndexDiff);

        output.writeUleb128(accessFlags);
        output.writeUleb128(codeOffset);
    }

    public void codeAccept(DexFile dexFile, ClassDef classDef, ClassData classData, CodeVisitor visitor) {
        visitor.visitCode(dexFile, classDef, classData, this, code);
    }
}
