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

public class MethodIDTest
extends      DexContentTest<MethodID>
{
    @Override
    public MethodID[] getTestInstances() {
        return new MethodID[] {
            MethodID.of(1, 2, 3),
            MethodID.of(65535, 65535, 65535)
        };
    }

    @Override
    public Function<DexDataInput, MethodID> getFactoryMethod() {
        return MethodID::readContent;
    }

    @Test
    public void inputChecking() {
        assertThrows(IllegalArgumentException.class, () -> {
            MethodID.of(-1, 2, 3);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            MethodID.of(1, -2, 3);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            MethodID.of(1, 2, -3);
        });
    }

    @Test
    public void getter() {
        MethodID[] data = getTestInstances();

        assertEquals(1, data[0].getClassIndex());
        assertEquals(2, data[0].getProtoIndex());
        assertEquals(3, data[0].getNameIndex());
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
