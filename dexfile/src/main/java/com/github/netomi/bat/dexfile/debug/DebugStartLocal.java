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
 * Represents a debug instruction that starts a local variable at the current address.
 *
 * @author Thomas Neidhart
 */
public class DebugStartLocal
extends      DebugInstruction
{
    protected int registerNum;
    protected int nameIndex;
    protected int typeIndex;

    public static DebugStartLocal of(int registerNum, int nameIndex, int typeIndex) {
        return new DebugStartLocal(DBG_START_LOCAL, registerNum, nameIndex, typeIndex);
    }

    DebugStartLocal() {
        this(DBG_START_LOCAL);
    }

    protected DebugStartLocal(byte opCode) {
        super(opCode);
    }

    protected DebugStartLocal(byte opCode, int registerNum, int nameIndex, int typeIndex) {
        super(opCode);
        this.registerNum = registerNum;
        this.nameIndex   = nameIndex;
        this.typeIndex   = typeIndex;
    }

    public int getRegisterNum() {
        return registerNum;
    }

    public int getNameIndex() {
        return nameIndex;
    }

    public int getTypeIndex() {
        return typeIndex;
    }

    public String getName(DexFile dexFile) {
        return dexFile.getStringID(nameIndex).getStringValue();
    }

    public String getType(DexFile dexFile) {
        return dexFile.getTypeID(typeIndex).getType(dexFile);
    }

    @Override
    protected void read(DexDataInput input) {
        registerNum = input.readUleb128();
        nameIndex   = input.readUleb128p1();
        typeIndex   = input.readUleb128p1();
    }

    @Override
    protected void write(DexDataOutput output) {
        output.writeByte(getOpcode());
        output.writeUleb128(registerNum);
        output.writeUleb128p1(nameIndex);
        output.writeUleb128p1(typeIndex);
    }

    @Override
    public void accept(DexFile dexFile, DebugInfo debugInfo, DebugSequenceVisitor visitor) {
        visitor.visitStartLocal(dexFile, debugInfo, this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DebugStartLocal other = (DebugStartLocal) o;
        return registerNum == other.registerNum &&
               nameIndex   == other.nameIndex   &&
               typeIndex   == other.typeIndex;
    }

    @Override
    public int hashCode() {
        return Objects.hash(registerNum, nameIndex, typeIndex);
    }

    @Override
    public String toString() {
        return String.format("DebugStartLocal[registerNum=%d,nameIndex=%d,typeIndex=%d]",
                             registerNum,
                             nameIndex,
                             typeIndex);
    }
}
