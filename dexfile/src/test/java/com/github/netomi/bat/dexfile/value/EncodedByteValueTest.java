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
package com.github.netomi.bat.dexfile.value;

public class EncodedByteValueTest
extends      EncodedValueTest<EncodedByteValue>
{
    @Override
    protected EncodedByteValue[] getTestInstance() {
        return new EncodedByteValue[] {
            EncodedByteValue.of((byte)    0),
            EncodedByteValue.of((byte)    1),
            EncodedByteValue.of((byte)   -1),
            EncodedByteValue.of((byte)  127),
            EncodedByteValue.of((byte) -127),
            EncodedByteValue.of((byte)  128),
            EncodedByteValue.of((byte) -128),
            EncodedByteValue.of(Byte.MIN_VALUE),
            EncodedByteValue.of(Byte.MAX_VALUE)
        };
    }
}
