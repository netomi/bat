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

import com.github.netomi.bat.dexfile.io.DexDataInput;
import com.github.netomi.bat.dexfile.io.DexDataOutput;

/**
 * @author Thomas Neidhart
 */
public class DebugStartLocal
extends      DebugInstruction
{
    public int registerNum;
    public int nameIndex;
    public int typeIndex;

    public DebugStartLocal() {
        super(DBG_START_LOCAL);
    }

    protected DebugStartLocal(byte opCode) {
        super(opCode);
    }

    @Override
    public void read(DexDataInput input) {
        registerNum = input.readUleb128();
        nameIndex   = input.readUleb128p1();
        typeIndex   = input.readUleb128p1();
    }

    @Override
    public void write(DexDataOutput output) {
        output.writeByte(getOpcode());
        output.writeUleb128(registerNum);
        output.writeUleb128p1(nameIndex);
        output.writeUleb128p1(typeIndex);
    }
}
