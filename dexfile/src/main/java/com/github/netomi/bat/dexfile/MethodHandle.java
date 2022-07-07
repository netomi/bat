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
import com.github.netomi.bat.util.Preconditions;

import java.util.Objects;
import java.util.Optional;

import static com.github.netomi.bat.dexfile.DexConstants.NO_INDEX;

/**
 * A class representing a method handle item inside a dex file.
 *
 * @see <a href="https://source.android.com/devices/tech/dalvik/dex-format#method-handle-item">method handle item @ dex format</a>
 */
@DataItemAnn(
    type          = DexConstants.TYPE_METHOD_HANDLE_ITEM,
    dataAlignment = 4,
    dataSection   = false
)
public class MethodHandle
extends      DataItem
{
    private int methodHandleType; // ushort
    // unused - ushort
    private int fieldOrMethodId;  // ushort
    // unused - ushort

    public static MethodHandle of(MethodHandleType methodHandleType, int fieldOrMethodId) {
        Objects.requireNonNull(methodHandleType, "methodHandleType must not be null");
        return of(methodHandleType.getValue(), fieldOrMethodId);
    }

    public static MethodHandle of(int methodHandleType, int fieldOrMethodId) {
        Preconditions.checkArgument(fieldOrMethodId >= 0, "fieldOrMethodId must be non negative");
        return new MethodHandle(methodHandleType, fieldOrMethodId);
    }

    public static MethodHandle readContent(DexDataInput input) {
        MethodHandle methodHandle = new MethodHandle();
        methodHandle.read(input);
        return methodHandle;
    }

    private MethodHandle() {
        this(0, NO_INDEX);
    }

    private MethodHandle(int methodHandleType, int fieldOrMethodId) {
        this.methodHandleType = methodHandleType;
        this.fieldOrMethodId  = fieldOrMethodId;
    }

    public MethodHandleType getMethodHandleType() {
        return MethodHandleType.of(methodHandleType);
    }

    public int getMethodHandleTypeValue() {
        return methodHandleType;
    }

    public int getFieldOrMethodId() {
        return fieldOrMethodId;
    }

    public FieldID getFieldID(DexFile dexFile) {
        return getMethodHandleType().targetsField() ?
            dexFile.getFieldID(fieldOrMethodId) :
            null;
    }

    public MethodID getMethodID(DexFile dexFile) {
        return getMethodHandleType().targetsField() ?
            null :
            dexFile.getMethodID(fieldOrMethodId);
    }

    public String getTargetClassType(DexFile dexFile) {
        MethodHandleType methodHandleType = getMethodHandleType();
        if (methodHandleType.targetsField()) {
            FieldID fieldID = getFieldID(dexFile);
            return fieldID.getClassType(dexFile);
        } else {
            MethodID methodID = getMethodID(dexFile);
            return methodID.getClassType(dexFile);
        }
    }

    public String getTargetMemberName(DexFile dexFile) {
        MethodHandleType methodHandleType = getMethodHandleType();
        if (methodHandleType.targetsField()) {
            FieldID fieldID = getFieldID(dexFile);
            return fieldID.getName(dexFile);
        } else {
            MethodID methodID = getMethodID(dexFile);
            return methodID.getName(dexFile);
        }
    }

    public String getTargetMemberDescriptor(DexFile dexFile) {
        MethodHandleType methodHandleType = getMethodHandleType();
        if (methodHandleType.targetsField()) {
            FieldID fieldID = getFieldID(dexFile);
            return fieldID.getType(dexFile);
        } else {
            MethodID methodID = getMethodID(dexFile);
            return methodID.getProtoID(dexFile).getDescriptor(dexFile);
        }
    }

    public String getTargetDecriptor(DexFile dexFile) {
        MethodHandleType methodHandleType = getMethodHandleType();
        return methodHandleType.targetsInstance() ?
            String.format("(%s%s", getTargetClassType(dexFile), getTargetMemberDescriptor(dexFile).substring(1)) :
            getTargetMemberDescriptor(dexFile);
    }

    @Override
    protected void read(DexDataInput input) {
        input.skipAlignmentPadding(getDataAlignment());

        methodHandleType = input.readUnsignedShort();
        input.readUnsignedShort();
        fieldOrMethodId = input.readUnsignedShort();
        input.readUnsignedShort();
    }

    @Override
    public void write(DexDataOutput output) {
        output.writeAlignmentPadding(getDataAlignment());

        output.writeUnsignedShort(methodHandleType);
        output.writeUnsignedShort(0x0);
        output.writeUnsignedShort(fieldOrMethodId);
        output.writeUnsignedShort(0x0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MethodHandle other = (MethodHandle) o;
        return methodHandleType == other.methodHandleType &&
               fieldOrMethodId  == other.fieldOrMethodId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(methodHandleType, fieldOrMethodId);
    }

    @Override
    public String toString() {
        return String.format("MethodHandle[type=%02x,fieldOrMethodId=%d]", methodHandleType, fieldOrMethodId);
    }
}
