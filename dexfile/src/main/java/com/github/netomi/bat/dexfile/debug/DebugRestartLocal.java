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

import com.github.netomi.bat.dexfile.DexFile;
import com.github.netomi.bat.dexfile.io.DexDataOutput;
import com.github.netomi.bat.dexfile.io.DexDataInput;
import com.github.netomi.bat.dexfile.visitor.DebugSequenceVisitor;

/**
 * @author Thomas Neidhart
 */
public class DebugRestartLocal
extends      DebugInstruction
{
    public int registerNum;

    public DebugRestartLocal() {
        super(DBG_RESTART_LOCAL);
    }

    @Override
    protected void read(DexDataInput input) {
        registerNum = input.readUleb128();
    }

    @Override
    protected void write(DexDataOutput output) {
        output.writeByte(getOpcode());
        output.writeUleb128(registerNum);
    }

    @Override
    public void accept(DexFile dexFile, DebugInfo debugInfo, DebugSequenceVisitor visitor) {
        visitor.visitRestartLocal(dexFile, debugInfo, this);
    }
}
