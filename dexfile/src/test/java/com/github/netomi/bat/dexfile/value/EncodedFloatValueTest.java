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

public class EncodedFloatValueTest
extends      EncodedValueTest<EncodedFloatValue>
{
    @Override
    protected EncodedFloatValue[] getTestInstance() {
        return new EncodedFloatValue[] {
            EncodedFloatValue.of(0f),
            EncodedFloatValue.of(1f),
            EncodedFloatValue.of(-1f),
            EncodedFloatValue.of(0.1f),
            EncodedFloatValue.of(-0.1f),
            EncodedFloatValue.of(Float.MIN_VALUE),
            EncodedFloatValue.of(Float.MAX_VALUE)
        };
    }
}
