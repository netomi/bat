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
import com.github.netomi.bat.util.Preconditions;

import java.util.Objects;

import static com.github.netomi.bat.dexfile.DexConstants.NO_INDEX;

/**
 * A class representing an encoded type address pair inside a dex file.
 *
 * @see <a href="https://source.android.com/devices/tech/dalvik/dex-format#encoded-type-addr-pair">type addr pair @ dex format</a>
 *
 * @author Thomas Neidhart
 */
public class TypeAddrPair
extends      DexContent
{
    private int typeIndex; // uleb128
    private int addr;      // uleb128

    public static TypeAddrPair of(int typeIndex, int addr) {
        Preconditions.checkArgument(typeIndex >= 0, "typeIndex must be non-negative");
        Preconditions.checkArgument(addr >= 0,      "addr must be non-negative");
        return new TypeAddrPair(typeIndex, addr);
    }

    public static TypeAddrPair readContent(DexDataInput input) {
        TypeAddrPair typeAddrPair = new TypeAddrPair();
        typeAddrPair.read(input);
        return typeAddrPair;
    }

    private TypeAddrPair() {
        this(NO_INDEX, 0);
    }

    private TypeAddrPair(int typeIndex, int addr) {
        this.typeIndex = typeIndex;
        this.addr      = addr;
    }

    public int getTypeIndex() {
        return typeIndex;
    }

    public String getType(DexFile dexFile) {
        return dexFile.getTypeID(typeIndex).getType(dexFile);
    }

    public int getAddress() {
        return addr;
    }

    @Override
    protected void read(DexDataInput input) {
        typeIndex = input.readUleb128();
        addr      = input.readUleb128();
    }

    @Override
    public void write(DexDataOutput output) {
        output.writeUleb128(typeIndex);
        output.writeUleb128(addr);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TypeAddrPair other = (TypeAddrPair) o;
        return typeIndex == other.typeIndex &&
               addr      == other.addr;
    }

    @Override
    public int hashCode() {
        return Objects.hash(typeIndex, addr);
    }

    @Override
    public String toString() {
        return String.format("TypeAddrPair[type=%d,addr=%d]", typeIndex, addr);
    }
}
