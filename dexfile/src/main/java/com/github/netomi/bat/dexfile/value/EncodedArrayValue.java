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

import java.util.*;
import java.util.function.BiConsumer;

/**
 * An class representing an array of values inside a dex file.
 *
 * @author Thomas Neidhart
 */
public class EncodedArrayValue
extends      EncodedValue
{
    //public int                    size; // uleb128
    private ArrayList<EncodedValue> values;

    public static EncodedArrayValue empty() {
        return new EncodedArrayValue();
    }

    public static EncodedArrayValue of(EncodedValue... values) {
        return new EncodedArrayValue(values);
    }

    EncodedArrayValue() {
        values = new ArrayList<>(0);
    }

    private EncodedArrayValue(EncodedValue... values) {
        this.values = new ArrayList<>(values.length);
        this.values.addAll(Arrays.asList(values));
    }

    public int getValueCount() {
        return values.size();
    }

    public EncodedValue getValue(int index) {
        return values.get(index);
    }

    public void addValue(EncodedValue value) {
        values.add(value);
    }

    @Override
    public int getValueType() {
        return VALUE_ARRAY;
    }

    @Override
    public void readValue(DexDataInput input, int valueArg) {
        int size = input.readUleb128();
        values.clear();
        values.ensureCapacity(size);
        for (int i = 0; i < size; i++) {
            values.add(EncodedValue.read(input));
        }
    }

    @Override
    protected int writeType(DexDataOutput output) {
        return writeType(output, 0);
    }

    @Override
    public void writeValue(DexDataOutput output, int valueArg) {
        output.writeUleb128(values.size());
        for (EncodedValue value : values) {
            value.write(output);
        }
    }

    @Override
    public void accept(DexFile dexFile, EncodedValueVisitor visitor) {
        visitor.visitArrayValue(dexFile, this);
    }

    public void valuesAccept(DexFile dexFile, EncodedValueVisitor visitor) {
        for (EncodedValue value : values) {
            value.accept(dexFile, visitor);
        }
    }

    public void valueAccept(DexFile dexFile, int index, EncodedValueVisitor visitor) {
        if (index >= 0 && index < values.size()) {
            values.get(index).accept(dexFile, visitor);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EncodedArrayValue other = (EncodedArrayValue) o;
        return Objects.equals(values, other.values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(values);
    }

    @Override
    public String toString() {
        return String.format("EncodedArrayValue[values=%s]", values);
    }
}
