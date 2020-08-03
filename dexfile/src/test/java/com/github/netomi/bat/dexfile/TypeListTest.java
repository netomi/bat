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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class TypeListTest
{
    @Test
    public void equals() {
        TypeList l1 = TypeList.empty();
        TypeList l2 = TypeList.empty();

        assertEquals(l1, l2);

        l1.addType(1);
        assertNotEquals(l1, l2);

        l2.addType(1);
        assertEquals(l1, l2);

        l1.addType(2);
        assertNotEquals(l1, l2);
    }
}
