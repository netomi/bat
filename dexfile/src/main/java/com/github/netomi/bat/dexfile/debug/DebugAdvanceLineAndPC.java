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
import com.github.netomi.bat.dexfile.io.DexDataInput;
import com.github.netomi.bat.dexfile.io.DexDataOutput;
import com.github.netomi.bat.dexfile.visitor.DebugSequenceVisitor;

import java.util.Objects;

/**
 * Represents a debug instruction that advances the line and address registers.
 *
 * @author Thomas Neidhart
 */
public class DebugAdvanceLineAndPC
extends      DebugInstruction
{
    private int lineDiff;
    private int addrDiff;

    DebugAdvanceLineAndPC(byte byteCode) {
        super(byteCode);
    }

    public int getLineDiff() {
        return lineDiff;
    }

    public int getAddrDiff() {
        return addrDiff;
    }

    @Override
    protected void read(DexDataInput input) {
        int adjustedOpCode = (getOpcode() & 0xff) - DBG_FIRST_SPECIAL;

        lineDiff = DBG_LINE_BASE + (adjustedOpCode % DBG_LINE_RANGE);
        addrDiff = (adjustedOpCode / DBG_LINE_RANGE);
    }

    @Override
    protected void write(DexDataOutput output) {
        output.writeByte(getOpcode());
    }

    @Override
    public void accept(DexFile dexFile, DebugInfo debugInfo, DebugSequenceVisitor visitor) {
        visitor.visitAdvanceLineAndPC(dexFile, debugInfo, this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DebugAdvanceLineAndPC other = (DebugAdvanceLineAndPC) o;
        return lineDiff == other.lineDiff &&
               addrDiff == other.addrDiff;
    }

    @Override
    public int hashCode() {
        return Objects.hash(lineDiff, addrDiff);
    }

    @Override
    public String toString() {
        return String.format("DebugAdvanceLineAndPC[lineDiff=%d,addrDiff=%d]", lineDiff, addrDiff);
    }
}
