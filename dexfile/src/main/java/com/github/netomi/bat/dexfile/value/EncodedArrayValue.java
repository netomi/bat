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

import com.github.netomi.bat.dexfile.io.DexDataInput;
import com.github.netomi.bat.dexfile.io.DexDataOutput;

import java.util.Arrays;

public class EncodedArrayValue extends EncodedValue
{
    public int            size; // uleb128
    public EncodedValue[] values;

    @Override
    public int getValueType() {
        return VALUE_ARRAY;
    }

    @Override
    public void read(DexDataInput input, int valueArg) {
        size   = input.readUleb128();
        values = new EncodedValue[size];
        for (int i = 0; i < size; i++) {
            values[i] = EncodedValueFactory.readValue(input);
        }
    }

    @Override
    public void write(DexDataOutput output) {
        writeType(output, 0);
        output.writeUleb128(size);
        for (EncodedValue value : values) {
            value.write(output);
        }
    }

    public String toString() {
        return String.format("EncodedArrayValue[size=%d,value=%s]", size, Arrays.toString(values));
    }
}
