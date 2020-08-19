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
import com.github.netomi.bat.dexfile.util.PrimitiveIterable;
import com.github.netomi.bat.dexfile.visitor.TypeVisitor;
import com.github.netomi.bat.util.IntArray;

import java.util.Objects;

/**
 * A class representing a list of type ids inside a dex file.
 *
 * @see <a href="https://source.android.com/devices/tech/dalvik/dex-format#type-list">type list @ dex format</a>
 *
 * @author Thomas Neidhart
 */
@DataItemAnn(
    type          = DexConstants.TYPE_TYPE_LIST,
    dataAlignment = 4,
    dataSection   = false
)
public class TypeList
extends      DataItem
{
    //private int   size; // uint
    private IntArray typeList;

    /**
     * Returns a new empty TypeList instance.
     */
    public static TypeList empty() {
        return new TypeList();
    }

    /**
     * Returns a new TypeList instance containing the given type indices.
     *
     * @param typeIndices the type indices that the list should contain.
     * @return a new TypeList instance containing the type indices.
     */
    public static TypeList of(int... typeIndices) {
        TypeList typeList = new TypeList();
        for (int typeIndex : typeIndices) {
            typeList.addType(typeIndex);
        }
        return typeList;
    }

    public static TypeList readContent(DexDataInput input) {
        TypeList typeList = new TypeList();
        typeList.read(input);
        return typeList;
    }

    private TypeList() {
        this.typeList = new IntArray(0);
    }

    /**
     * Returns the number of types contained in this TypeList.
     */
    public int getTypeCount() {
        return typeList.size();
    }

    public String getType(DexFile dexFile, int index) {
        return dexFile.getTypeID(typeList.get(index)).getType(dexFile);
    }

    public int getTypeIndex(int index) {
        return typeList.get(index);
    }

    public Iterable<String> getTypes(DexFile dexFile) {
        return PrimitiveIterable.of(dexFile,
                                    (df, idx) -> df.getTypeID(idx).getType(df),
                                    typeList);
    }

    public void addType(int typeIDIndex) {
        typeList.add(typeIDIndex);
    }

    public void addType(DexFile dexFile, String type) {
        addType(dexFile.addOrGetTypeIDIndex(type));
    }

    @Override
    protected void read(DexDataInput input) {
        input.skipAlignmentPadding(getDataAlignment());

        int size = (int) input.readUnsignedInt();
        typeList.clear();
        typeList.resize(size);
        for (int i = 0; i < size; i++) {
            int typeIndex = input.readUnsignedShort();
            typeList.set(i, typeIndex);
        }
    }

    @Override
    protected void write(DexDataOutput output) {
        output.writeAlignmentPadding(getDataAlignment());

        int size = typeList.size();
        output.writeInt(size);
        for (int i = 0; i < size; i++) {
            output.writeUnsignedShort(typeList.get(i));
        }
    }

    public void typesAccept(DexFile dexFile, TypeVisitor visitor) {
        int size = typeList.size();
        for (int i = 0; i < size; i++) {
            visitor.visitType(dexFile, this, i, dexFile.getTypeID(typeList.get(i)).getType(dexFile));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TypeList other = (TypeList) o;
        return Objects.equals(typeList, other.typeList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(typeList);
    }

    @Override
    public String toString() {
        return String.format("TypeList[types=%s]", typeList);
    }
}
