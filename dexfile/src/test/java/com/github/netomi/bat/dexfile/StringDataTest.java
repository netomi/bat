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

public class StringDataTest
extends      DexContentTest<StringData>
{
    @Override
    public StringData[] getTestInstances() {
        return new StringData[] {
            StringData.of("Terence Hill"),
            StringData.of("Jean-Claude Van Damme"),
            StringData.of("Bud Spencer")
        };
    }

    @Override
    public Function<DexDataInput, StringData> getFactoryMethod() {
        return StringData::readContent;
    }

    @Test
    public void inputChecking() {
        assertThrows(NullPointerException.class, () -> {
            StringData.of(null);
        });
    }

    @Test
    public void getter() {
        StringData[] data = getTestInstances();

        assertEquals("Terence Hill", data[0].getString());
        assertEquals("Bud Spencer",  data[2].getString());
    }

    @Test
    public void equals() {
        StringData t1 = StringData.of("Terence Hill");
        StringData t2 = StringData.of("Bud Spencer");
        StringData t3 = StringData.of("Terence Hill");

        assertEquals(t1, t1);
        assertNotEquals(t1, t2);
        assertEquals(t1, t3);
    }
}
