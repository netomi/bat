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

import java.util.Objects;

/**
 * An class representing a short value inside a dex file.
 *
 * @author Thomas Neidhart
 */
public class EncodedShortValue
extends      EncodedValue
{
    private short value;

    public static EncodedShortValue of(short value) {
        return new EncodedShortValue(value);
    }

    EncodedShortValue() {
        this((short) 0);
    }

    private EncodedShortValue(short value) {
        this.value = value;
    }

    public short getValue() {
        return value;
    }

    @Override
    public int getValueType() {
        return VALUE_SHORT;
    }

    @Override
    public void readValue(DexDataInput input, int valueArg) {
        value = input.readShort(valueArg + 1);
    }

    @Override
    protected int writeType(DexDataOutput output) {
        return writeType(output, requiredBytesForSignedShort(value) - 1);
    }

    @Override
    public void writeValue(DexDataOutput output, int valueArg) {
        output.writeShort(value, valueArg + 1);
    }

    @Override
    public void accept(DexFile dexFile, EncodedValueVisitor visitor) {
        visitor.visitShortValue(dexFile, this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EncodedShortValue other = (EncodedShortValue) o;
        return value == other.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return String.format("EncodedShortValue[value=%d]", value);
    }
}
