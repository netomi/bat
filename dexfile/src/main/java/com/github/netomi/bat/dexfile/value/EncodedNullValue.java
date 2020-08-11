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

public class EncodedNullValue
extends      EncodedValue
{
    private static final EncodedNullValue INSTANCE = new EncodedNullValue();

    public static EncodedNullValue instance() {
        return INSTANCE;
    }

    private EncodedNullValue() {}

    @Override
    public int getValueType() {
        return VALUE_NULL;
    }

    @Override
    public void readValue(DexDataInput input, int valueArg) {}

    @Override
    protected int writeType(DexDataOutput output) {
        return writeType(output, 0);
    }

    @Override
    public void writeValue(DexDataOutput output, int valueArg) {}

    @Override
    public void accept(DexFile dexFile, EncodedValueVisitor visitor) {
        visitor.visitNullValue(dexFile, this);
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(INSTANCE);
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj;
    }

    @Override
    public String toString() {
        return String.format("EncodedNullValue[]");
    }
}
