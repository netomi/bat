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
 * An class representing a int value inside a dex file.
 *
 * @author Thomas Neidhart
 */
public class EncodedLongValue
extends      EncodedValue
{
    private long value;

    public static EncodedLongValue of(long value) {
        return new EncodedLongValue(value);
    }

    EncodedLongValue() {
        this(0);
    }

    private EncodedLongValue(long value) {
        this.value = value;
    }

    public long getValue() {
        return value;
    }

    @Override
    public int getValueType() {
        return VALUE_LONG;
    }

    @Override
    public void readValue(DexDataInput input, int valueArg) {
        value = input.readLong(valueArg + 1);
    }

    @Override
    protected int writeType(DexDataOutput output) {
        return writeType(output, requiredBytesForSignedLong(value) - 1);
    }

    @Override
    public void writeValue(DexDataOutput output, int valueArg) {
        output.writeLong(value, valueArg + 1);
    }

    @Override
    public void accept(DexFile dexFile, EncodedValueVisitor visitor) {
        visitor.visitLongValue(dexFile, this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EncodedLongValue other = (EncodedLongValue) o;
        return value == other.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return String.format("EncodedLongValue[value=%d]", value);
    }
}
