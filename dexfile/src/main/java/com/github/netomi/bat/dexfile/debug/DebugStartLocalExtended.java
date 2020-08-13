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
 * Represents a debug instruction that starts a local variable at the current address with
 * extended information.
 *
 * @author Thomas Neidhart
 */
public class DebugStartLocalExtended
extends      DebugStartLocal
{
    private int sigIndex;

    public static DebugStartLocalExtended of(int registerNum, int nameIndex, int typeIndex, int sigIndex) {
        return new DebugStartLocalExtended(registerNum, nameIndex, typeIndex, sigIndex);
    }

    DebugStartLocalExtended() {
        super(DBG_START_LOCAL_EXTENDED);
    }

    private DebugStartLocalExtended(int registerNum, int nameIndex, int typeIndex, int sigIndex) {
        super(DBG_START_LOCAL_EXTENDED, registerNum, nameIndex, typeIndex);
        this.sigIndex = sigIndex;
    }

    public int getSigIndex() {
        return sigIndex;
    }

    @Override
    protected void read(DexDataInput input) {
        super.read(input);
        sigIndex = input.readUleb128p1();
    }

    @Override
    protected void write(DexDataOutput output) {
        super.write(output);
        output.writeUleb128p1(sigIndex);
    }

    @Override
    public void accept(DexFile dexFile, DebugInfo debugInfo, DebugSequenceVisitor visitor) {
        visitor.visitStartLocalExtended(dexFile, debugInfo, this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DebugStartLocalExtended other = (DebugStartLocalExtended) o;
        return sigIndex == other.sigIndex;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), sigIndex);
    }

    @Override
    public String toString() {
        return String.format("DebugStartLocalExtended[registerNum=%d,nameIndex=%d,typeIndex=%d,sigIndex=%d]",
                             registerNum,
                             nameIndex,
                             typeIndex,
                             sigIndex);
    }
}
