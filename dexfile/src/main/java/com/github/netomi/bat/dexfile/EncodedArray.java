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
import com.github.netomi.bat.dexfile.value.EncodedArrayValue;
import com.github.netomi.bat.dexfile.value.EncodedValue;
import com.github.netomi.bat.dexfile.visitor.EncodedValueVisitor;

import java.util.Objects;

@DataItemAnn(
    type          = DexConstants.TYPE_ENCODED_ARRAY_ITEM,
    dataAlignment = 1,
    dataSection   = true
)
public class EncodedArray
extends      DataItem
{
    protected EncodedArrayValue encodedArrayValue;

    public static EncodedArray empty() {
        return new EncodedArray();
    }

    public static EncodedArray readContent(DexDataInput input) {
        EncodedArray encodedArray = new EncodedArray();
        encodedArray.read(input);
        return encodedArray;
    }

    protected EncodedArray() {
        encodedArrayValue = EncodedArrayValue.empty();
    }

    public EncodedArrayValue getArray() {
        return encodedArrayValue;
    }

    @Override
    protected void read(DexDataInput input) {
        encodedArrayValue.readValue(input, 0);
    }

    @Override
    protected void write(DexDataOutput output) {
        encodedArrayValue.writeValue(output, 0);
    }

    public void accept(DexFile dexFile, EncodedValueVisitor visitor) {
        for (int i = 0; i < encodedArrayValue.getValueCount(); i++) {
            encodedArrayValue.getValue(i).accept(dexFile, visitor);
        }
    }

    public void accept(DexFile dexFile, int index, EncodedValueVisitor visitor) {
        if (index >= 0 && index < encodedArrayValue.getValueCount()) {
            encodedArrayValue.getValue(index).accept(dexFile, visitor);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EncodedArray other = (EncodedArray) o;
        return Objects.equals(encodedArrayValue, other.encodedArrayValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(encodedArrayValue);
    }

    @Override
    public String toString() {
        return String.format("EncodedArray[array=%s]", encodedArrayValue);
    }
}
