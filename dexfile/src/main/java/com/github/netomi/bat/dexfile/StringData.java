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

import java.nio.charset.Charset;

@DataItemAnn(
    type          = DexConstants.TYPE_STRING_DATA_ITEM,
    dataAlignment = 1,
    dataSection   = true
)
public class StringData extends DataItem
{
    private static final byte[] EMPTY_ARRAY = new byte[0];

    public int    utf16Size; // uleb128
    public byte[] data;      // ubyte[]
    public String stringValue;

    public StringData() {
        utf16Size   = 0;
        data        = EMPTY_ARRAY;
        stringValue = null;
    }

    public String getString() {
        return stringValue;
    }

    @Override
    public void read(DexDataInput input) {
        utf16Size   = input.readUleb128();
        data        = input.readMUTF8Bytes(utf16Size);
        stringValue = asString(data, utf16Size);
    }

    @Override
    public void write(DexDataOutput output) {
        output.writeUleb128(utf16Size);
        output.writeBytes(data);
    }

    private static String asString(byte[] data, int len) {
        return len >= 0 ?
                new String(data, 0, data.length, Charset.forName("UTF-8")) :
                null;
    }

    @Override
    public String toString() {
        return String.format("StringData[value=%s]", stringValue);
    }
}
