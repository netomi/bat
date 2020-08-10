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
 * A class representing a type id item inside a dex file.
 *
 * @see <a href="https://source.android.com/devices/tech/dalvik/dex-format#type-id-item">type id item @ dex format</a>
 *
 * @author Thomas Neidhart
 */
@DataItemAnn(
    type          = DexConstants.TYPE_TYPE_ID_ITEM,
    dataAlignment = 4,
    dataSection   = false
)
public class TypeID
extends      DataItem
{
    private int descriptorIndex; // uint

    public static TypeID of(int descriptorIndex) {
        Preconditions.checkArgument(descriptorIndex >= 0, "descriptor index must be non-negative");
        return new TypeID(descriptorIndex);
    }

    public static TypeID readContent(DexDataInput input) {
        TypeID typeID = new TypeID();
        typeID.read(input);
        return typeID;
    }

    private TypeID() {
        this(NO_INDEX);
    }

    private TypeID(int descriptorIndex) {
        this.descriptorIndex = descriptorIndex;
    }

    public int getDescriptorIndex() {
        return descriptorIndex;
    }

    public String getType(DexFile dexFile) {
        return dexFile.getStringID(descriptorIndex).getStringValue();
    }

    @Override
    protected void read(DexDataInput input) {
        input.skipAlignmentPadding(getDataAlignment());
        descriptorIndex = input.readInt();
    }

    @Override
    protected void write(DexDataOutput output) {
        output.writeAlignmentPadding(getDataAlignment());
        output.writeInt(descriptorIndex);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TypeID other = (TypeID) o;
        return descriptorIndex == other.descriptorIndex;
    }

    @Override
    public int hashCode() {
        return Objects.hash(descriptorIndex);
    }

    @Override
    public String toString() {
        return String.format("TypeID[descriptorIdx=%d]", descriptorIndex);
    }
}
