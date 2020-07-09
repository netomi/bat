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
public class DebugAdvanceLineAndPC
extends      DebugInstruction
{
    public int lineDiff;
    public int addrDiff;

    public DebugAdvanceLineAndPC(byte byteCode) {
        super(byteCode);
    }

    @Override
    public void read(DexDataInput input) {
        int adjustedOpCode = getOpcode() - DBG_FIRST_SPECIAL;

        lineDiff = DBG_LINE_BASE + (adjustedOpCode % DBG_LINE_RANGE);
        addrDiff = (adjustedOpCode / DBG_LINE_RANGE);
    }

    @Override
    public void write(DexDataOutput output) {
        output.writeByte(getOpcode());
    }
}
