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

/**
 * @author Thomas Neidhart
 */
public class TypeAddrPair
implements   DexContent
{
    public int typeIndex; // uleb128
    public int addr;      // uleb128

    public TypeAddrPair() {
        typeIndex = NO_INDEX;
        addr      = 0;
    }

    @Override
    public void read(DexDataInput input) {
        typeIndex = input.readUleb128();
        addr      = input.readUleb128();
    }

    @Override
    public void write(DexDataOutput output) {
        output.writeUleb128(typeIndex);
        output.writeUleb128(addr);
    }
}
