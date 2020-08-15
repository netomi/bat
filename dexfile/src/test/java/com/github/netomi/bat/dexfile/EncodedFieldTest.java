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
import com.github.netomi.bat.dexfile.io.DexDataOutput;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

public class EncodedFieldTest
extends      DexContentTest<EncodedField>
{
    @Override
    public EncodedField[] getTestInstances() {
        return new EncodedField[] {
            EncodedField.of(1, Visibility.PUBLIC, FieldModifier.FINAL),
            EncodedField.of(2, Visibility.PRIVATE),
            EncodedField.of(65535, Visibility.PACKAGE_PRIVATE, FieldModifier.VOLATILE, FieldModifier.SYNTHETIC),
        };
    }

    @Override
    public Function<DexDataInput, EncodedField> getFactoryMethod() {
        return (input) -> EncodedField.readContent(input, 0);
    }

    @Override
    public Consumer<DexDataOutput> getWriteMethod(EncodedField data) {
        return (output) -> data.write(output, 0);
    }

    @Test
    public void inputChecking() {
        assertThrows(IllegalArgumentException.class, () -> {
            EncodedField.of(-1, Visibility.PUBLIC, FieldModifier.FINAL);
        });
    }

    @Test
    public void getter() {
        EncodedField[] data = getTestInstances();

        assertEquals(1, data[0].getFieldIndex());
        assertEquals(Visibility.PUBLIC, data[0].getVisibility());
        assertEquals(EnumSet.of(FieldModifier.FINAL), data[0].getModifiers());
    }

    @Test
    public void equals() {
        EncodedField e1 = EncodedField.of(1, Visibility.PUBLIC, FieldModifier.FINAL);
        EncodedField e2 = EncodedField.of(2, Visibility.PRIVATE, FieldModifier.STATIC);
        EncodedField e3 = EncodedField.of(1, Visibility.PUBLIC, FieldModifier.FINAL);

        assertEquals(e1, e1);
        assertNotEquals(e1, e2);
        assertEquals(e1, e3);
    }
}
