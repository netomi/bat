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
import com.github.netomi.bat.dexfile.FieldID;
import com.github.netomi.bat.dexfile.io.DexDataInput;
import com.github.netomi.bat.dexfile.io.DexDataOutput;
import com.github.netomi.bat.dexfile.visitor.EncodedValueVisitor;
import com.github.netomi.bat.util.Preconditions;

import java.util.Objects;

import static com.github.netomi.bat.dexfile.DexConstants.NO_INDEX;

/**
 * An class representing a referenced field (FieldID) value inside a dex file.
 *
 * @author Thomas Neidhart
 */
public class EncodedFieldValue
extends      EncodedValue
{
    private int fieldIndex;

    public static EncodedFieldValue of(int fieldIndex) {
        Preconditions.checkArgument(fieldIndex >= 0, "fieldIndex must not be negative");
        return new EncodedFieldValue(fieldIndex);
    }

    EncodedFieldValue() {
        this(NO_INDEX);
    }

    private EncodedFieldValue(int fieldIndex) {
        this.fieldIndex = fieldIndex;
    }

    public int getFieldIndex() {
        return fieldIndex;
    }

    public FieldID getField(DexFile dexFile) {
        return dexFile.getFieldID(fieldIndex);
    }

    @Override
    public int getValueType() {
        return VALUE_FIELD;
    }

    @Override
    public void readValue(DexDataInput input, int valueArg) {
        fieldIndex = input.readUnsignedInt(valueArg + 1);
    }

    @Override
    protected int writeType(DexDataOutput output) {
        return writeType(output, requiredBytesForUnsignedInt(fieldIndex) - 1);
    }

    @Override
    public void writeValue(DexDataOutput output, int valueArg) {
        output.writeInt(fieldIndex, valueArg + 1);
    }

    @Override
    public void accept(DexFile dexFile, EncodedValueVisitor visitor) {
        visitor.visitFieldValue(dexFile, this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EncodedFieldValue other = (EncodedFieldValue) o;
        return fieldIndex == other.fieldIndex;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fieldIndex);
    }

    @Override
    public String toString() {
        return String.format("EncodedFieldValue[fieldIdx=%d]", fieldIndex);
    }
}
