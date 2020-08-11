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
import com.github.netomi.bat.dexfile.MethodID;
import com.github.netomi.bat.dexfile.io.DexDataInput;
import com.github.netomi.bat.dexfile.io.DexDataOutput;
import com.github.netomi.bat.dexfile.visitor.EncodedValueVisitor;
import com.github.netomi.bat.util.Preconditions;

import java.util.Objects;

import static com.github.netomi.bat.dexfile.DexConstants.NO_INDEX;

public class EncodedMethodValue
extends      EncodedValue
{
    private int methodIndex;

    public static EncodedMethodValue of(int methodIndex) {
        Preconditions.checkArgument(methodIndex >= 0, "methodIndex must not be negative");
        return new EncodedMethodValue(methodIndex);
    }

    EncodedMethodValue() {
        this(NO_INDEX);
    }

    private EncodedMethodValue(int methodIndex) {
        this.methodIndex = methodIndex;
    }

    public int getMethodIndex() {
        return methodIndex;
    }

    public MethodID getMethod(DexFile dexFile) {
        return dexFile.getMethodID(methodIndex);
    }

    @Override
    public int getValueType() {
        return VALUE_METHOD;
    }

    @Override
    public void readValue(DexDataInput input, int valueArg) {
        methodIndex = input.readUnsignedInt(valueArg + 1);
    }

    @Override
    protected int writeType(DexDataOutput output) {
        return writeType(output, requiredBytesForUnsignedInt(methodIndex) - 1);
    }

    @Override
    public void writeValue(DexDataOutput output, int valueArg) {
        output.writeInt(methodIndex, valueArg + 1);
    }

    @Override
    public void accept(DexFile dexFile, EncodedValueVisitor visitor) {
        visitor.visitMethodValue(dexFile, this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EncodedMethodValue other = (EncodedMethodValue) o;
        return methodIndex == other.methodIndex;
    }

    @Override
    public int hashCode() {
        return Objects.hash(methodIndex);
    }

    @Override
    public String toString() {
        return String.format("EncodedMethodValue[methodIdx=%d]", methodIndex);
    }
}
