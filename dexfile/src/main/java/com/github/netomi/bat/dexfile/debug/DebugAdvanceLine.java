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

import java.util.Objects;

/**
 * Represents a debug instruction that advances the line register.
 *
 * @author Thomas Neidhart
 */
public class DebugAdvanceLine
extends      DebugInstruction
{
    private int lineDiff;

    public static DebugAdvanceLine of(int lineDiff) {
        return new DebugAdvanceLine(lineDiff);
    }

    DebugAdvanceLine() {
        this(0);
    }

    private DebugAdvanceLine(int lineDiff) {
        super(DBG_ADVANCE_LINE);
        this.lineDiff = lineDiff;
    }

    public int getLineDiff() {
        return lineDiff;
    }

    @Override
    protected void read(DexDataInput input) {
        lineDiff = input.readSleb128();
    }

    @Override
    protected void write(DexDataOutput output) {
        output.writeByte(getOpcode());
        output.writeSleb128(lineDiff);
    }

    @Override
    public void accept(DexFile dexFile, DebugInfo debugInfo, DebugSequenceVisitor visitor) {
        visitor.visitAdvanceLine(dexFile, debugInfo, this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DebugAdvanceLine other = (DebugAdvanceLine) o;
        return lineDiff == other.lineDiff;
    }

    @Override
    public int hashCode() {
        return Objects.hash(lineDiff);
    }

    @Override
    public String toString() {
        return String.format("DebugAdvanceLine[lineDiff=%d]", lineDiff);
    }
}
