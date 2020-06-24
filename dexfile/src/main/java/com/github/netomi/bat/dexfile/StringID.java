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

@DataItemAnn(
    type          = DexConstants.TYPE_STRING_ID_ITEM,
    dataAlignment = 4,
    dataSection   = false
)
public class StringID extends DataItem
{
    public int        stringDataOffset; // uint
    public StringData stringData;

    public StringID() {
        stringDataOffset = 0;
        stringData       = null;
    }

    public String getStringValue() {
        return stringData.getString();
    }

    @Override
    public void read(DexDataInput input) {
        input.skipAlignmentPadding(getDataAlignment());
        stringDataOffset = input.readInt();
    }

    @Override
    public void readLinkedDataItems(DexDataInput input) {
        input.setOffset(stringDataOffset);
        stringData = new StringData();
        stringData.read(input);
    }

    @Override
    public void write(DexDataOutput output) {
        output.writeAlignmentPadding(getDataAlignment());
        output.writeInt(stringDataOffset);
    }

    @Override
    public void dataItemsAccept(DexFile dexFile, DataItemVisitor visitor) {
        if (stringData != null) {
            visitor.visitStringData(dexFile, this, stringData);
            stringData.dataItemsAccept(dexFile, visitor);
        }
    }

    @Override
    public String toString() {
        return String.format("StringID[offset=0x%04x]", stringDataOffset);
    }
}
