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
import com.github.netomi.bat.dexfile.util.Mutf8;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@DataItemAnn(
    type          = DexConstants.TYPE_STRING_DATA_ITEM,
    dataAlignment = 1,
    dataSection   = true
)
public class StringData
extends      DataItem
{
    // private int    utf16Size; // uleb128
    // private byte[] data;      // ubyte[]
    private String stringValue;

    public static StringData readContent(DexDataInput input) {
        StringData stringData = new StringData();
        stringData.read(input);
        return stringData;
    }

    public static StringData of(String value) {
        return new StringData(value);
    }

    private StringData() {
        stringValue = null;
    }

    private StringData(String value) {
        this.stringValue = value;
    }

    public String getString() {
        return stringValue;
    }

    @Override
    protected void read(DexDataInput input) {
        int    utf16Size = input.readUleb128();
        byte[] data      = input.readMUTF8Bytes(utf16Size);

        stringValue = Mutf8.decode(data, utf16Size);
    }

    @Override
    protected void write(DexDataOutput output) {
        int utf16Size = stringValue.length();
        output.writeUleb128(utf16Size);

        byte[] data = Mutf8.encode(stringValue);
        output.writeBytes(data);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StringData other = (StringData) o;
        return Objects.equals(stringValue, other.stringValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stringValue);
    }

    @Override
    public String toString() {
        return String.format("StringData[value=%s]", stringValue);
    }
}
