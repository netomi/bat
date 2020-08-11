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

public class ProtoIDTest
extends      DexContentTest<ProtoID>
{
    @Override
    public ProtoID[] getTestInstances() {
        return new ProtoID[] {
            ProtoID.of(1, 2, 3, 4, 5),
            ProtoID.of(1, 2),
            ProtoID.of(65535, 65535, 65535)
        };
    }

    @Override
    public Function<DexDataInput, ProtoID> getFactoryMethod() {
        return ProtoID::readContent;
    }

    @Test
    public void inputChecking() {
        assertThrows(IllegalArgumentException.class, () -> {
            ProtoID.of(-1, 2, 3);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            ProtoID.of(1, -2, 3);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            ProtoID.of(1, 2, -3);
        });
    }

    @Test
    public void getter() {
        ProtoID[] data = getTestInstances();

        assertEquals(1, data[0].getShortyIndex());
        assertEquals(2, data[0].getReturnTypeIndex());

        TypeList parameters = data[0].getParameters();
        assertEquals(3, parameters.getTypeCount());
        assertEquals(3, parameters.getTypeIndex(0));
        assertEquals(4, parameters.getTypeIndex(1));
        assertEquals(5, parameters.getTypeIndex(2));
    }

    @Test
    public void equals() {
        ProtoID p1 = ProtoID.of(1, 2, 3);
        ProtoID p2 = ProtoID.of(1, 3, 4);
        ProtoID p3 = ProtoID.of(1, 2, 3);

        assertEquals(p1, p1);
        assertNotEquals(p1, p2);
        assertEquals(p1, p3);
    }
}
