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

public class TryTest
extends      DexContentTest<Try>
{
    @Override
    public Try[] getTestInstances() {
        // can not use concrete EncodedCatchHandler instances for testing
        // special case that will is covered by testing the Code item.
        return new Try[] {
            Try.of(0, 10, null),
            Try.of(0, 65534, null),
        };
    }

    @Override
    public Function<DexDataInput, Try> getFactoryMethod() {
        return Try::readContent;
    }

    @Test
    public void inputChecking() {
        assertThrows(IllegalArgumentException.class, () -> {
            Try.of(-1, 100, null);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            Try.of(1, -1, null);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            Try.of(100000, 100, null);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            Try.of(1, 100000, null);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            Try.of(100, 10, null);
        });
    }

    @Test
    public void getter() {
        Try[] data = getTestInstances();

        assertEquals(0,  data[0].getStartAddr());
        assertEquals(11, data[0].getInsnCount());
        assertEquals(10, data[0].getEndAddr());
    }

    @Test
    public void equals() {
        Try t1 = Try.of(1, 10, EncodedCatchHandler.of(10, TypeAddrPair.of(1, 2)));
        Try t2 = Try.of(2, 10, EncodedCatchHandler.of(10, TypeAddrPair.of(1, 2)));
        Try t3 = Try.of(2, 11, EncodedCatchHandler.of(10, TypeAddrPair.of(1, 2)));
        Try t4 = Try.of(1, 10, EncodedCatchHandler.of(10, TypeAddrPair.of(1, 2)));

        assertEquals(t1, t1);
        assertNotEquals(t1, t2);
        assertNotEquals(t2, t3);
        assertEquals(t1, t4);
    }
}
