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

import com.github.netomi.bat.dexfile.value.EncodedMethodHandleValue;
import com.github.netomi.bat.dexfile.value.EncodedMethodTypeValue;
import com.github.netomi.bat.dexfile.value.EncodedStringValue;

/**
 * @author Thomas Neidhart
 */
public class CallSite
extends      EncodedArray
{
    public EncodedMethodHandleValue getMethodHandle() {
        return (EncodedMethodHandleValue) encodedArrayValue.getValue(0);
    }

    public EncodedStringValue getMethodName() {
        return (EncodedStringValue) encodedArrayValue.getValue(1);
    }

    public EncodedMethodTypeValue getMethodType() {
        return (EncodedMethodTypeValue) encodedArrayValue.getValue(2);
    }
}
