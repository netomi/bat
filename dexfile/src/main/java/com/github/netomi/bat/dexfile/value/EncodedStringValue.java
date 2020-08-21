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

/**
 * An class representing a referenced string (StringID) value inside a dex file.
 *
 * @author Thomas Neidhart
 */
public class EncodedStringValue
extends      EncodedValue
{
    private int stringIndex;

    public static EncodedStringValue of(int stringIndex) {
        Preconditions.checkArgument(stringIndex >= 0, "stringIndex must not be negative");
        return new EncodedStringValue(stringIndex);
    }

    EncodedStringValue() {
        this(NO_INDEX);
    }

    private EncodedStringValue(int stringIndex) {
        this.stringIndex = stringIndex;
    }

    public int getStringIndex() {
        return stringIndex;
    }

    public String getString(DexFile dexFile) {
        return dexFile.getStringID(stringIndex).getStringValue();
    }

    @Override
    public int getValueType() {
        return VALUE_STRING;
    }

    @Override
    public void readValue(DexDataInput input, int valueArg) {
        stringIndex = input.readUnsignedInt(valueArg + 1);
    }

    @Override
    protected int writeType(DexDataOutput output) {
        return writeType(output, requiredBytesForUnsignedInt(stringIndex) - 1);
    }

    @Override
    public void writeValue(DexDataOutput output, int valueArg) {
        output.writeInt(stringIndex, valueArg + 1);
    }

    @Override
    public void accept(DexFile dexFile, EncodedValueVisitor visitor) {
        visitor.visitStringValue(dexFile, this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EncodedStringValue other = (EncodedStringValue) o;
        return stringIndex == other.stringIndex;
    }

    @Override
    public int hashCode() {
        return Objects.hash(stringIndex);
    }

    @Override
    public String toString() {
        return String.format("EncodedStringValue[stringIdx=%d]", stringIndex);
    }
}
