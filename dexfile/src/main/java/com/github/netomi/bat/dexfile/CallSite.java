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
import com.github.netomi.bat.dexfile.value.EncodedMethodHandleValue;
import com.github.netomi.bat.dexfile.value.EncodedMethodTypeValue;
import com.github.netomi.bat.dexfile.value.EncodedStringValue;

/**
 * A class representing a callsite item inside a dex file.
 *
 * @see <a href="https://source.android.com/devices/tech/dalvik/dex-format#call-site-item">callsite item @ dex format</a>
 *
 * @author Thomas Neidhart
 */
public class CallSite
extends      EncodedArray
{
    public static CallSite of(int methodHandleIndex, int nameIndex, int protoIndex) {
        CallSite callSite = new CallSite();

        callSite.encodedArrayValue.addEncodedValue(EncodedMethodHandleValue.of(methodHandleIndex));
        callSite.encodedArrayValue.addEncodedValue(EncodedStringValue.of(nameIndex));
        callSite.encodedArrayValue.addEncodedValue(EncodedMethodTypeValue.of(protoIndex));

        return callSite;
    }

    public static CallSite readContent(DexDataInput input) {
        CallSite callSite = new CallSite();
        callSite.read(input);
        return callSite;
    }

    private CallSite() {}

    public EncodedMethodHandleValue getMethodHandle() {
        return (EncodedMethodHandleValue) encodedArrayValue.getEncodedValue(0);
    }

    public MethodHandle getMethodHandle(DexFile dexFile) {
        return getMethodHandle().getMethodHandle(dexFile);
    }

    public EncodedStringValue getMethodName() {
        return (EncodedStringValue) encodedArrayValue.getEncodedValue(1);
    }

    public String getMethodName(DexFile dexFile) {
        return getMethodName().getStringValue(dexFile);
    }

    public EncodedMethodTypeValue getMethodType() {
        return (EncodedMethodTypeValue) encodedArrayValue.getEncodedValue(2);
    }

    public ProtoID getMethodType(DexFile dexFile) {
        return getMethodType().getProtoID(dexFile);
    }

    @Override
    public String toString() {
        return String.format("CallSite[values=%s]", encodedArrayValue);
    }
}
