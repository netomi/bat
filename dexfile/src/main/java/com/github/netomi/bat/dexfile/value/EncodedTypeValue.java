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
package com.github.netomi.bat.dexfile.value;

import com.github.netomi.bat.dexfile.DexFile;
import com.github.netomi.bat.dexfile.io.DexDataInput;
import com.github.netomi.bat.dexfile.io.DexDataOutput;
import com.github.netomi.bat.dexfile.visitor.EncodedValueVisitor;
import com.github.netomi.bat.util.Preconditions;

import java.util.Objects;

import static com.github.netomi.bat.dexfile.DexConstants.NO_INDEX;

public class EncodedTypeValue
extends      EncodedValue
{
    private int typeIndex;

    public static EncodedTypeValue of(int typeIndex) {
        Preconditions.checkArgument(typeIndex >= 0, "typeIndex must not be negative");
        return new EncodedTypeValue(typeIndex);
    }

    EncodedTypeValue() {
        this(NO_INDEX);
    }

    private EncodedTypeValue(int typeIndex) {
        this.typeIndex = typeIndex;
    }

    public int getTypeIndex() {
        return typeIndex;
    }

    public String getType(DexFile dexFile) {
        return dexFile.getTypeID(typeIndex).getType(dexFile);
    }

    @Override
    public int getValueType() {
        return VALUE_TYPE;
    }

    @Override
    public void readValue(DexDataInput input, int valueArg) {
        typeIndex = input.readUnsignedInt(valueArg + 1);
    }

    @Override
    protected int writeType(DexDataOutput output) {
        return writeType(output, requiredBytesForUnsignedInt(typeIndex) - 1);
    }

    @Override
    public void writeValue(DexDataOutput output, int valueArg) {
        output.writeInt(typeIndex, valueArg + 1);
    }

    @Override
    public void accept(DexFile dexFile, EncodedValueVisitor visitor) {
        visitor.visitTypeValue(dexFile, this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EncodedTypeValue other = (EncodedTypeValue) o;
        return typeIndex == other.typeIndex;
    }

    @Override
    public int hashCode() {
        return Objects.hash(typeIndex);
    }

    @Override
    public String toString() {
        return String.format("EncodedTypeValue[typeIdx=%d]", typeIndex);
    }
}
