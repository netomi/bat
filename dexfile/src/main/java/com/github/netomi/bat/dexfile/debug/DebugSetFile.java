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
package com.github.netomi.bat.dexfile.debug;

import com.github.netomi.bat.dexfile.io.DexDataOutput;
import com.github.netomi.bat.dexfile.io.DexDataInput;

/**
 * @author Thomas Neidhart
 */
public class DebugSetFile
extends      DebugInstruction
{
    public int nameIndex;

    public DebugSetFile() {
        super(DBG_SET_FILE);
    }

    @Override
    public void read(DexDataInput input) {
        nameIndex = input.readUleb128p1();
    }

    @Override
    public void write(DexDataOutput output) {
        output.writeByte(getOpcode());
        output.writeUleb128p1(nameIndex);
    }
}
