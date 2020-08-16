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
import com.github.netomi.bat.dexfile.util.DexClasses;
import com.github.netomi.bat.dexfile.visitor.*;
import com.github.netomi.bat.util.Preconditions;

import java.util.Objects;

import static com.github.netomi.bat.dexfile.DexConstants.NO_INDEX;

/**
 * A class representing a method id item inside a dex file.
 *
 * @see <a href="https://source.android.com/devices/tech/dalvik/dex-format#method-id-item">method id item @ dex format</a>
 *
 * @author Thomas Neidhart
 */
@DataItemAnn(
    type          = DexConstants.TYPE_METHOD_ID_ITEM,
    dataAlignment = 4,
    dataSection   = false
)
public class MethodID
extends      DataItem
{
    private int classIndex; // ushort
    private int protoIndex; // ushort
    private int nameIndex;  // uint;

    public static MethodID of(int classIndex, int protoIndex, int nameIndex) {
        Preconditions.checkArgument(classIndex >= 0, "class index must be non negative");
        Preconditions.checkArgument(protoIndex >= 0, "proto index must be non negative");
        Preconditions.checkArgument(nameIndex  >= 0, "name index must be non negative");

        return new MethodID(classIndex, protoIndex, nameIndex);
    }

    public static MethodID readContent(DexDataInput input) {
        MethodID methodID = new MethodID();
        methodID.read(input);
        return methodID;
    }

    private MethodID() {
        this(NO_INDEX, NO_INDEX, NO_INDEX);
    }

    private MethodID(int classIndex, int protoIndex, int nameIndex) {
        this.classIndex = classIndex;
        this.protoIndex = protoIndex;
        this.nameIndex  = nameIndex;
    }

    public int getClassIndex() {
        return classIndex;
    }

    public TypeID getClassTypeID(DexFile dexFile) {
        return dexFile.getTypeID(classIndex);
    }

    public String getClassType(DexFile dexFile) {
        return getClassTypeID(dexFile).getType(dexFile);
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
    protected void read(DexDataInput input) {
        input.skipAlignmentPadding(getDataAlignment());
        classIndex = input.readUnsignedShort();
        protoIndex = input.readUnsignedShort();
        nameIndex  = input.readInt();
    }

    @Override
    protected void write(DexDataOutput output) {
        output.writeAlignmentPadding(getDataAlignment());
        output.writeUnsignedShort(classIndex);
        output.writeUnsignedShort(protoIndex);
        output.writeInt(nameIndex);
    }

    public void accept(DexFile dexFile, EncodedMethodVisitor visitor) {
        String   className = DexClasses.internalClassNameFromType(getClassType(dexFile));
        ClassDef classDef  = dexFile.getClassDef(className);

        if (classDef != null) {
            classDef.classDataAccept(dexFile,
                new AllEncodedMethodsVisitor(
                new MethodNameAndProtoFilter(getName(dexFile), getProtoID(dexFile),
                visitor)));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MethodID other = (MethodID) o;
        return classIndex == other.classIndex &&
               protoIndex == other.protoIndex &&
               nameIndex  == other.nameIndex;
    }

    @Override
    public int hashCode() {
        return Objects.hash(classIndex, protoIndex, nameIndex);
    }

    @Override
    public String toString() {
        return String.format("MethodID[classIdx=%d,typeIdx=%d,nameIdx=%d]", classIndex, protoIndex, nameIndex);
    }
}
