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
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

public class TypeAddrPairTest
extends      DexContentTest<TypeAddrPair>
{
    @Override
    public TypeAddrPair[] getTestInstances() {
        return new TypeAddrPair[] {
            TypeAddrPair.of(1, 20),
            TypeAddrPair.of(2, 0),
            TypeAddrPair.of(65535, 65535)
        };
    }

    @Override
    public Function<DexDataInput, TypeAddrPair> getFactoryMethod() {
        return TypeAddrPair::readContent;
    }

    @Test
    public void inputChecking() {
        assertThrows(IllegalArgumentException.class, () -> {
            TypeAddrPair.of(-1, 0);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            TypeAddrPair.of(10, -5);
        });
    }

    @Test
    public void getter() {
        TypeAddrPair[] data = getTestInstances();

        assertEquals(1,  data[0].getTypeIndex());
        assertEquals(20, data[0].getAddress());
    }

    @Test
    public void equals() {
        TypeAddrPair p1 = TypeAddrPair.of(1, 0);
        TypeAddrPair p2 = TypeAddrPair.of(2, 0);
        TypeAddrPair p3 = TypeAddrPair.of(2, 10);
        TypeAddrPair p4 = TypeAddrPair.of(1, 0);

        assertEquals(p1, p1);
        assertNotEquals(p1, p2);
        assertNotEquals(p2, p3);
        assertEquals(p1, p4);
    }
}
