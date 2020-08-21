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
import com.github.netomi.bat.dexfile.MethodHandle;
import com.github.netomi.bat.dexfile.io.DexDataInput;
import com.github.netomi.bat.dexfile.io.DexDataOutput;
import com.github.netomi.bat.dexfile.visitor.EncodedValueVisitor;
import com.github.netomi.bat.util.Preconditions;

import java.util.Objects;

import static com.github.netomi.bat.dexfile.DexConstants.NO_INDEX;

/**
 * An class representing a referenced method handle (MethodHandle) value inside a dex file.
 *
 * @author Thomas Neidhart
 */
public class EncodedMethodHandleValue
extends      EncodedValue
{
    private int handleIndex;

    public static EncodedMethodHandleValue of(int handleIndex) {
        Preconditions.checkArgument(handleIndex >= 0, "handleIndex must not be negative");
        return new EncodedMethodHandleValue(handleIndex);
    }

    EncodedMethodHandleValue() {
        this(NO_INDEX);
    }

    private EncodedMethodHandleValue(int handleIndex) {
        this.handleIndex = handleIndex;
    }

    public int getHandleIndex() {
        return handleIndex;
    }

    public MethodHandle getMethodHandle(DexFile dexFile) {
        return dexFile.getMethodHandle(handleIndex);
    }

    @Override
    public int getValueType() {
        return VALUE_METHOD_HANDLE;
    }

    @Override
    public void readValue(DexDataInput input, int valueArg) {
        handleIndex = input.readUnsignedInt(valueArg + 1);
    }

    @Override
    protected int writeType(DexDataOutput output) {
        return writeType(output, requiredBytesForUnsignedInt(handleIndex) - 1);
    }

    @Override
    public void writeValue(DexDataOutput output, int valueArg) {
        output.writeInt(handleIndex, valueArg + 1);
    }

    @Override
    public void accept(DexFile dexFile, EncodedValueVisitor visitor) {
        visitor.visitMethodHandleValue(dexFile, this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EncodedMethodHandleValue other = (EncodedMethodHandleValue) o;
        return handleIndex == other.handleIndex;
    }

    @Override
    public int hashCode() {
        return Objects.hash(handleIndex);
    }

    @Override
    public String toString() {
        return String.format("EncodedMethodHandleValue[methodHandleIdx=%d]", handleIndex);
    }
}
