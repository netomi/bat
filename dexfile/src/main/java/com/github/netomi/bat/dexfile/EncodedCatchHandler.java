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

/**
 * @author Thomas Neidhart
 */
public class EncodedCatchHandler
extends      DexContent
{
    //public int              size;         // sleb128, use handlers.size()
    public List<TypeAddrPair> handlers;
    public int                catchAllAddr; // uleb128

    public EncodedCatchHandler() {
        handlers     = Collections.emptyList();
        catchAllAddr = -1;
    }

    @Override
    protected void read(DexDataInput input) {
        int readSize = input.readSleb128();
        int size     = Math.abs(readSize);

        handlers = new ArrayList<>(size);
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
}
