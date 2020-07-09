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
import com.github.netomi.bat.dexfile.value.EncodedArrayValue;

@DataItemAnn(
    type          = DexConstants.TYPE_ENCODED_ARRAY_ITEM,
    dataAlignment = 1,
    dataSection   = true
)
public class EncodedArray
implements   DataItem
{
    private static final EncodedArrayValue EMPTY_ARRAY = new EncodedArrayValue();

    public EncodedArrayValue encodedArrayValue;

    public EncodedArray() {
        encodedArrayValue = EMPTY_ARRAY;
    }

    @Override
    public void read(DexDataInput input) {
        encodedArrayValue = new EncodedArrayValue();
        encodedArrayValue.read(input, 0);
    }

    @Override
    public void write(DexDataOutput output) {
        encodedArrayValue.write(output);
    }
}
