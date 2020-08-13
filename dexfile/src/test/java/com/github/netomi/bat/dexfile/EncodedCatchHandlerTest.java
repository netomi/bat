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

public class EncodedCatchHandlerTest
extends      DexContentTest<EncodedCatchHandler>
{
    @Override
    public EncodedCatchHandler[] getTestInstances() {
        return new EncodedCatchHandler[] {
            EncodedCatchHandler.of(1),
            EncodedCatchHandler.of(0, TypeAddrPair.of(1, 2), TypeAddrPair.of(3, 4))
        };
    }

    @Override
    public Function<DexDataInput, EncodedCatchHandler> getFactoryMethod() {
        return EncodedCatchHandler::readContent;
    }

    @Test
    public void getter() {
        EncodedCatchHandler[] data = getTestInstances();

        assertEquals(1, data[0].getCatchAllAddr());
        assertEquals(TypeAddrPair.of(1, 2), data[1].getHandler(0));
    }

    @Test
    public void equals() {
        EncodedCatchHandler c1 = EncodedCatchHandler.of(1, TypeAddrPair.of(1, 2));
        EncodedCatchHandler c2 = EncodedCatchHandler.of(1, TypeAddrPair.of(3, 4));
        EncodedCatchHandler c3 = EncodedCatchHandler.of(1, TypeAddrPair.of(1, 2));

        assertEquals(c1, c1);
        assertNotEquals(c1, c2);
        assertEquals(c1, c3);
    }
}
