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

import java.util.Objects;

/**
 * A class representing an encoded method inside a dex file.
 *
 * @see <a href="https://source.android.com/devices/tech/dalvik/dex-format#encoded-method-format">encoded method @ dex format</a>
 *
 * @author Thomas Neidhart
 */
public class EncodedMethod
extends      DexContent
{
    private int deltaMethodIndex; // uleb128
    private int methodIndex;
    private int accessFlags;      // uleb128
    private int codeOffset;       // uleb128

    public  Code code;

    public static EncodedMethod readContent(DexDataInput input, int lastIndex) {
        EncodedMethod encodedMethod = new EncodedMethod();
        encodedMethod.read(input);
        encodedMethod.updateMethodIndex(lastIndex);
        return encodedMethod;
    }

    private EncodedMethod() {
        methodIndex = DexConstants.NO_INDEX;
        accessFlags = 0;
        code        = null;
    }

    public EncodedMethod(int methodIndex) {
        this.methodIndex = methodIndex;
    }

    public int getCodeOffset() {
        return codeOffset;
    }

    public int getAccessFlags() {
        return accessFlags;
    }

    public int getMethodIndex() {
        return methodIndex;
    }

    public MethodID getMethodID(DexFile dexFile) {
        return dexFile.getMethodID(methodIndex);
    }

    public ProtoID getProtoID(DexFile dexFile) {
        return getMethodID(dexFile).getProtoID(dexFile);
    }

    public String getClassType(DexFile dexFile) {
        return dexFile.getMethodID(methodIndex).getClassType(dexFile).getType(dexFile);
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

    public boolean isStatic() {
        return (accessFlags & DexConstants.ACC_STATIC) != 0;
    }

    @Override
    protected void read(DexDataInput input) {
        deltaMethodIndex = input.readUleb128();
        accessFlags      = input.readUleb128();
        codeOffset       = input.readUleb128();
    }

    @Override
    protected void readLinkedDataItems(DexDataInput input) {
        if (codeOffset != 0) {
            input.setOffset(codeOffset);

            code = Code.readItem(input);
            code.readLinkedDataItems(input);
        }
    }

    private void updateMethodIndex(int lastIndex) {
        methodIndex = deltaMethodIndex + lastIndex;
    }

    private void updateDeltaMethodIndex(int lastIndex) {
        deltaMethodIndex = methodIndex - lastIndex;
    }

    @Override
    protected void updateOffsets(DataItem.Map dataItemMap) {
        codeOffset = dataItemMap.getOffset(code);
    }

    protected int write(DexDataOutput output, int lastIndex) {
        updateDeltaMethodIndex(lastIndex);
        write(output);
        return methodIndex;
    }

    @Override
    protected void write(DexDataOutput output) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EncodedMethod other = (EncodedMethod) o;
        return methodIndex == other.methodIndex &&
               accessFlags == other.accessFlags &&
               Objects.equals(code, other.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(methodIndex, accessFlags, code);
    }

    @Override
    public String toString() {
        return String.format("EncodedMethod[methodIndex=%d,accessFlags=%04x]", methodIndex, accessFlags);
    }
}
