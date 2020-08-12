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
import com.github.netomi.bat.dexfile.value.EncodedMethodHandleValue;
import com.github.netomi.bat.dexfile.value.EncodedMethodTypeValue;
import com.github.netomi.bat.dexfile.value.EncodedStringValue;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

public class CallSiteTest
extends      DexContentTest<CallSite>
{
    @Override
    public CallSite[] getTestInstances() {
        return new CallSite[] {
            CallSite.of(1, 2, 3),
            CallSite.of(65535, 65535, 65535)
        };
    }

    @Override
    public Function<DexDataInput, CallSite> getFactoryMethod() {
        return CallSite::readContent;
    }

    @Test
    public void inputChecking() {
        assertThrows(NullPointerException.class, () -> {
            CallSiteID.of(null);
        });
    }

    @Test
    public void getter() {
        CallSite[] data = getTestInstances();

        assertEquals(EncodedMethodHandleValue.of(1), data[0].getMethodHandle());
        assertEquals(EncodedStringValue.of(2), data[0].getMethodName());
        assertEquals(EncodedMethodTypeValue.of(3), data[0].getMethodType());
    }

    @Test
    public void equals() {
        CallSite c1 = CallSite.of(1, 2, 3);
        CallSite c2 = CallSite.of(2, 3, 4);
        CallSite c3 = CallSite.of(1, 2, 3);

        assertEquals(c1, c1);
        assertNotEquals(c1, c2);
        assertEquals(c1, c3);
    }
}
