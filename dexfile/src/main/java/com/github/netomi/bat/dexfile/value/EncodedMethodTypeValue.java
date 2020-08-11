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
import com.github.netomi.bat.dexfile.ProtoID;
import com.github.netomi.bat.dexfile.io.DexDataInput;
import com.github.netomi.bat.dexfile.io.DexDataOutput;
import com.github.netomi.bat.dexfile.visitor.EncodedValueVisitor;
import com.github.netomi.bat.util.Preconditions;

import java.util.Objects;

import static com.github.netomi.bat.dexfile.DexConstants.NO_INDEX;

public class EncodedMethodTypeValue
extends      EncodedValue
{
    private int protoIndex;

    public static EncodedMethodTypeValue of(int protoIndex) {
        Preconditions.checkArgument(protoIndex >= 0, "protoIndex must not be negative");
        return new EncodedMethodTypeValue(protoIndex);
    }

    EncodedMethodTypeValue() {
        this(NO_INDEX);
    }

    private EncodedMethodTypeValue(int protoIndex) {
        this.protoIndex = protoIndex;
    }

    public int getProtoIndex() {
        return protoIndex;
    }

    public ProtoID getProtoID(DexFile dexFile) {
        return dexFile.getProtoID(protoIndex);
    }

    @Override
    public int getValueType() {
        return VALUE_METHOD_TYPE;
    }

    @Override
    public void readValue(DexDataInput input, int valueArg) {
        protoIndex = input.readUnsignedInt(valueArg + 1);
    }

    @Override
    protected int writeType(DexDataOutput output) {
        return writeType(output, requiredBytesForUnsignedInt(protoIndex) - 1);
    }

    @Override
    public void writeValue(DexDataOutput output, int valueArg) {
        output.writeInt(protoIndex, valueArg + 1);
    }

    @Override
    public void accept(DexFile dexFile, EncodedValueVisitor visitor) {
        visitor.visitMethodTypeValue(dexFile, this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EncodedMethodTypeValue other = (EncodedMethodTypeValue) o;
        return protoIndex == other.protoIndex;
    }

    @Override
    public int hashCode() {
        return Objects.hash(protoIndex);
    }

    @Override
    public String toString() {
        return String.format("EncodedMethodTypeValue[protoIdx=%d]", protoIndex);
    }
}
