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

import com.github.netomi.bat.dexfile.io.ByteBufferBackedDexDataOutput;
import com.github.netomi.bat.dexfile.io.DexDataInput;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public abstract class DexContentTest<T extends DexContent>
{
    public abstract T[] getTestInstances();

    public abstract Function<DexDataInput, T> getFactoryMethod();

    @Test
    public void readWrite() {
        T[] testData = getTestInstances();

        for (T data : testData) {
            serializeAndDeserialize(data);
        }
    }

    private void serializeAndDeserialize(T data) {
        try {
            ByteBufferBackedDexDataOutput output = new ByteBufferBackedDexDataOutput(8096);
            data.write(output);
            byte[] buffer = output.toArray();

            DexDataInput input = new DexDataInput(new ByteArrayInputStream(buffer));

            T result = getFactoryMethod().apply(input);

            assertEquals(data, result);
        } catch (IOException ioe) {
            fail(ioe);
        }
    }
}
