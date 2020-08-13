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
import com.github.netomi.bat.util.Preconditions;

import java.util.Map;
import java.util.Objects;

/**
 * A class representing a try item inside a dex file.
 *
 * @see <a href="https://source.android.com/devices/tech/dalvik/dex-format#type-item">try item @ dex format</a>
 *
 * @author Thomas Neidhart
 */
public class Try
extends      DexContent
{
    private int startAddr;     // uint
    private int insnCount;     // ushort

    // package-private as these fields are set from the Code item.
    int handlerOffset;         // ushort
    EncodedCatchHandler catchHandler;

    public static Try of(int startAddr, int endAddr, EncodedCatchHandler catchHandler) {
        Preconditions.checkArgument(startAddr >= 0,     "startAddr must not be negative");
        Preconditions.checkArgument(startAddr <= 65535, "startAddr must be <= 65535");

        Preconditions.checkArgument(endAddr >= 0,        "endAddr must not be negative");
        Preconditions.checkArgument(endAddr <= 65534,    "endAddr must be <= 65534");
        Preconditions.checkArgument(endAddr > startAddr, "endAddr must be > startAddr");

        int insnCount = endAddr - startAddr + 1;
        return new Try(startAddr, insnCount, catchHandler);
    }

    public static Try readContent(DexDataInput input) {
        Try tryItem = new Try();
        tryItem.read(input);
        return tryItem;
    }

    private Try() {
        this(0, 0, null);
    }

    private Try(int startAddr, int insnCount, EncodedCatchHandler catchHandler) {
        this.startAddr    = startAddr;
        this.insnCount    = insnCount;
        this.catchHandler = catchHandler;
    }

    public int getStartAddr() {
        return startAddr;
    }

    public int getEndAddr() {
        return startAddr + insnCount - 1;
    }

    public int getInsnCount() {
        return insnCount;
    }

    public int getHandlerOffset() {
        return handlerOffset;
    }

    public EncodedCatchHandler getCatchHandler() {
        return catchHandler;
    }

    @Override
    protected void read(DexDataInput input) {
        startAddr     = input.readInt();
        insnCount     = input.readUnsignedShort();
        handlerOffset = input.readUnsignedShort();
    }

    @Override
    protected void write(DexDataOutput output) {
        output.writeInt(startAddr);
        output.writeUnsignedShort(insnCount);
        output.writeUnsignedShort(handlerOffset);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Try other = (Try) o;
        return startAddr == other.startAddr &&
               insnCount == other.insnCount &&
               Objects.equals(catchHandler, other.catchHandler);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startAddr, insnCount, catchHandler);
    }

    @Override
    public String toString() {
        return String.format("Try[startAddr=%04x,insnCount=%d,handler=%s]", startAddr, insnCount, catchHandler);
    }
}
