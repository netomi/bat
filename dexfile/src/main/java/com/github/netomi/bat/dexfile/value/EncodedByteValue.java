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

public class EncodedByteValue
extends      EncodedValue
{
    private byte value;

    public static EncodedByteValue of(byte value) {
        return new EncodedByteValue(value);
    }

    EncodedByteValue() {
        this((byte) 0);
    }

    private EncodedByteValue(byte value) {
        this.value = value;
    }

    public byte getValue() {
        return value;
    }

    @Override
    public int getValueType() {
        return VALUE_BYTE;
    }

    @Override
    public void readValue(DexDataInput input, int valueArg) {
        value = input.readByte();
    }

    @Override
    protected int writeType(DexDataOutput output) {
        return writeType(output, 0);
    }

    @Override
    public void writeValue(DexDataOutput output, int valueArg) {
        output.writeByte(value);
    }

    @Override
    public void accept(DexFile dexFile, EncodedValueVisitor visitor) {
        visitor.visitByteValue(dexFile, this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EncodedByteValue other = (EncodedByteValue) o;
        return value == other.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return String.format("EncodedByteValue[value=0x%02x]", value);
    }
}
