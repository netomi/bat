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

public class CallSiteIDTest
extends      DexContentTest<CallSiteID>
{
    @Override
    public CallSiteID[] getTestInstances() {
        return new CallSiteID[] {
            CallSiteID.of(CallSite.of(1, 2, 3)),
            CallSiteID.of(CallSite.of(65535, 65535, 65535))
        };
    }

    @Override
    public Function<DexDataInput, CallSiteID> getFactoryMethod() {
        return CallSiteID::readContent;
    }

    @Test
    public void inputChecking() {
        assertThrows(NullPointerException.class, () -> {
            CallSiteID.of(null);
        });
    }

    @Test
    public void getter() {
        CallSiteID[] data = getTestInstances();

        assertEquals(CallSite.of(1, 2, 3), data[0].getCallSite());
    }

    @Test
    public void equals() {
        CallSiteID c1 = CallSiteID.of(CallSite.of(1, 2, 3));
        CallSiteID c2 = CallSiteID.of(CallSite.of(2, 3, 4));
        CallSiteID c3 = CallSiteID.of(CallSite.of(1, 2, 3));

        assertEquals(c1, c1);
        assertNotEquals(c1, c2);
        assertEquals(c1, c3);
    }
}
