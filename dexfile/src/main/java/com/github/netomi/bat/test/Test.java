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

package com.github.netomi.bat.test;

import com.github.netomi.bat.dexfile.io.ByteBufferBackedDexDataOutput;
import com.github.netomi.bat.dexfile.io.DexDataInput;
import com.github.netomi.bat.dexfile.value.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class Test {

    public static void main(String[] args) throws IOException {
        ByteBufferBackedDexDataOutput output = new ByteBufferBackedDexDataOutput(100);
        //EncodedValue value = EncodedDoubleValue.of(-1);
        EncodedValue value = EncodedIntValue.of(-127);
        //EncodedValue value = EncodedCharValue.of((char)63);
        //EncodedValue value = EncodedShortValue.of((short)-1);
        value.write(output);

        DexDataInput input = new DexDataInput(new ByteArrayInputStream(output.toArray()));
        EncodedValue value2 = EncodedValue.read(input);

        System.out.println(value2);

    }
}
