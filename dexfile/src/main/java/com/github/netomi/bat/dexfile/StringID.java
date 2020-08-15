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
import com.github.netomi.bat.dexfile.visitor.DataItemVisitor;
import com.github.netomi.bat.util.Preconditions;

import java.util.Objects;

/**
 * A class representing a string id item inside a dex file.
 *
 * @see <a href="https://source.android.com/devices/tech/dalvik/dex-format#string-item">string id item @ dex format</a>
 *
 * @author Thomas Neidhart
 */
@DataItemAnn(
    type          = DexConstants.TYPE_STRING_ID_ITEM,
    dataAlignment = 4,
    dataSection   = false
)
public class StringID
extends      DataItem
{
    private int        stringDataOffset; // uint
    private StringData stringData;

    public static StringID of(String value) {
        Objects.requireNonNull(value, "value must not be null");
        return new StringID(StringData.of(value));
    }

    public static StringID readContent(DexDataInput input) {
        StringID stringID = new StringID();
        stringID.read(input);
        return stringID;
    }

    private StringID() {
        this(null);
    }

    private StringID(StringData data) {
        this.stringData = data;
    }

    public int getStringDataOffset() {
        return stringDataOffset;
    }

    public StringData getStringData() {
        return stringData;
    }

    public String getStringValue() {
        return stringData.getString();
    }

    @Override
    protected void read(DexDataInput input) {
        input.skipAlignmentPadding(getDataAlignment());
        stringDataOffset = input.readInt();
    }

    @Override
    protected void readLinkedDataItems(DexDataInput input) {
        input.setOffset(stringDataOffset);
        stringData = StringData.readContent(input);
    }

    @Override
    protected void updateOffsets(DataItem.Map dataItemMap) {
        stringDataOffset = dataItemMap.getOffset(stringData);
    }

    @Override
    protected void write(DexDataOutput output) {
        output.writeAlignmentPadding(getDataAlignment());
        output.writeInt(stringDataOffset);
    }

    @Override
    public void dataItemsAccept(DexFile dexFile, DataItemVisitor visitor) {
        if (stringData != null) {
            visitor.visitStringData(dexFile, this, stringData);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StringID other = (StringID) o;
        return Objects.equals(stringData, other.stringData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stringData);
    }

    @Override
    public String toString() {
        return String.format("StringID[data=%s]", stringData);
    }
}
