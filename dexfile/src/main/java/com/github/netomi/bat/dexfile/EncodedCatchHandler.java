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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A class representing an encoded catch handler inside a dex file.
 *
 * @see <a href="https://source.android.com/devices/tech/dalvik/dex-format#encoded-catch-handler">encoded catch handler @ dex format</a>
 *
 * @author Thomas Neidhart
 */
public class EncodedCatchHandler
extends      DexContent
{
    //public int                      size;       // sleb128, use handlers.size()
    private int                     catchAllAddr; // uleb128
    private ArrayList<TypeAddrPair> handlers = new ArrayList<>(0);

    public static EncodedCatchHandler of(int catchAllAddr, TypeAddrPair... handlers) {
        EncodedCatchHandler encodedCatchHandler = new EncodedCatchHandler(catchAllAddr);
        for (TypeAddrPair handler : handlers) {
            encodedCatchHandler.addHandler(handler);
        }
        return encodedCatchHandler;
    }

    public static EncodedCatchHandler readContent(DexDataInput input) {
        EncodedCatchHandler encodedCatchHandler = new EncodedCatchHandler();
        encodedCatchHandler.read(input);
        return encodedCatchHandler;
    }

    private EncodedCatchHandler() {
        this(-1);
    }

    private EncodedCatchHandler(int catchAllAddr) {
        this.catchAllAddr = catchAllAddr;
    }

    public int getCatchAllAddr() {
        return catchAllAddr;
    }

    public void addHandler(TypeAddrPair typeAddrPair) {
        handlers.add(typeAddrPair);
    }

    public int getHandlerCount() {
        return handlers.size();
    }

    public TypeAddrPair getHandler(int index) {
        return handlers.get(index);
    }

    @Override
    protected void read(DexDataInput input) {
        int readSize = input.readSleb128();
        int size     = Math.abs(readSize);

        handlers.ensureCapacity(size);
        for (int i = 0; i < size; i++) {
            TypeAddrPair typeAddrPair = TypeAddrPair.readContent(input);
            handlers.add(typeAddrPair);
        }

        if (readSize <= 0) {
            catchAllAddr = input.readUleb128();
        }
    }

    @Override
    protected void write(DexDataOutput output) {
        int writtenSize = handlers.size();
        if (catchAllAddr != -1) {
            writtenSize = -writtenSize;
        }

        output.writeSleb128(writtenSize);
        for (TypeAddrPair typeAddrPair : handlers) {
            typeAddrPair.write(output);
        }

        if (writtenSize <= 0) {
            output.writeUleb128(catchAllAddr);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EncodedCatchHandler other = (EncodedCatchHandler) o;
        return catchAllAddr == other.catchAllAddr &&
               Objects.equals(handlers, other.handlers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(catchAllAddr, handlers);
    }

    @Override
    public String toString() {
        return String.format("EncodedCatchHandler[handlers=%d,catchAllAddr=%4x]", handlers.size(), catchAllAddr);
    }
}
