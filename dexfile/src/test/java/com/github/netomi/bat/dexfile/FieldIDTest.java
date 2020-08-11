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

public class FieldIDTest
extends      DexContentTest<FieldID>
{
    @Override
    public FieldID[] getTestInstances() {
        return new FieldID[] {
            FieldID.of(1, 2, 3),
            FieldID.of(65535, 65535, 65535)
        };
    }

    @Override
    public Function<DexDataInput, FieldID> getFactoryMethod() {
        return FieldID::readContent;
    }

    @Test
    public void inputChecking() {
        assertThrows(IllegalArgumentException.class, () -> {
            FieldID.of(-1, 2, 3);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            FieldID.of(1, -2, 3);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            FieldID.of(1, 2, -3);
        });
    }

    @Test
    public void getter() {
        FieldID[] data = getTestInstances();

        assertEquals(1, data[0].getClassIndex());
        assertEquals(2, data[0].getTypeIndex());
        assertEquals(3, data[0].getNameIndex());
    }

    @Test
    public void equals() {
        FieldID f1 = FieldID.of(1, 2, 3);
        FieldID f2 = FieldID.of(1, 3, 4);
        FieldID f3 = FieldID.of(1, 2, 3);

        assertEquals(f1, f1);
        assertNotEquals(f1, f2);
        assertEquals(f1, f3);
    }
}
