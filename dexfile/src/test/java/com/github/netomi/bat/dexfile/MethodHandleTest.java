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

public class MethodHandleTest
extends      DexContentTest<MethodHandle>
{
    @Override
    public MethodHandle[] getTestInstances() {
        return new MethodHandle[] {
            MethodHandle.of(MethodHandleType.INSTANCE_GET, 1),
            MethodHandle.of(MethodHandleType.INSTANCE_PUT, 65535),
        };
    }

    @Override
    public Function<DexDataInput, MethodHandle> getFactoryMethod() {
        return MethodHandle::readContent;
    }

    @Test
    public void inputChecking() {
        assertThrows(IllegalArgumentException.class, () -> {
            MethodHandle.of(MethodHandleType.INSTANCE_PUT, -1);
        });

        assertThrows(NullPointerException.class, () -> {
            MethodHandle.of(null, 1);
        });
    }

    @Test
    public void getter() {
        MethodHandle[] data = getTestInstances();

        assertEquals(MethodHandleType.INSTANCE_GET, data[0].getMethodHandleType());
        assertEquals(3, data[0].getMethodHandleTypeValue());
        assertEquals(1, data[0].getFieldOrMethodId());
    }

    @Test
    public void equals() {
        MethodID m1 = MethodID.of(1, 2, 3);
        MethodID m2 = MethodID.of(1, 3, 4);
        MethodID m3 = MethodID.of(1, 2, 3);

        assertEquals(m1, m1);
        assertNotEquals(m1, m2);
        assertEquals(m1, m3);
    }
}
