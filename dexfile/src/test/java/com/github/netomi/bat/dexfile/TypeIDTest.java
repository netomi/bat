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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

public class TypeIDTest
extends      DexContentTest<TypeID>
{
    @Override
    public TypeID[] getTestInstances() {
        return new TypeID[] {
            TypeID.of(10),
            TypeID.of(20)
        };
    }

    @Override
    public Function<DexDataInput, TypeID> getFactoryMethod() {
        return TypeID::readContent;
    }

    @Test
    public void inputChecking() {
        assertThrows(IllegalArgumentException.class, () -> {
            TypeID.of(-1);
        });
    }

    @Test
    public void equals() {
        TypeID t1 = TypeID.of(1);
        TypeID t2 = TypeID.of(2);
        TypeID t3 = TypeID.of(1);

        assertEquals(t1, t1);
        assertNotEquals(t1, t2);
        assertEquals(t1, t3);
    }
}
