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

import static com.github.netomi.bat.dexfile.DexConstants.NO_INDEX;

public class EncodedField implements DexContent
{
    public int fieldIndex;  // uleb128
    public int accessFlags; // uleb128

    public EncodedField() {
        fieldIndex  = NO_INDEX;
        accessFlags = 0;
    }

    public FieldID getFieldIDItem(DexFile dexFile) {
        return dexFile.getFieldID(fieldIndex);
    }

    public String getName(DexFile dexFile) {
        return getFieldIDItem(dexFile).getName(dexFile);
    }

    public String getType(DexFile dexFile) {
        return getFieldIDItem(dexFile).getType(dexFile);
    }

    @Override
    public void read(DexDataInput input) {
        int fieldIndexDiff = input.readUleb128();
        fieldIndex  = fieldIndexDiff + input.getLastMemberIndex();
        input.setLastMemberIndex(fieldIndex);

        accessFlags = input.readUleb128();
    }

    @Override
    public void write(DexDataOutput output) {
        int fieldIndexDiff = fieldIndex - output.getLastMemberIndex();
        output.writeUleb128(fieldIndexDiff);

        output.writeUleb128(accessFlags);
    }
}
