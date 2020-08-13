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

import static com.github.netomi.bat.dexfile.DexConstants.*;

public enum MethodHandleType
{
    STATIC_PUT        (METHOD_HANDLE_TYPE_STATIC_PUT),
    STATIC_GET        (METHOD_HANDLE_TYPE_STATIC_GET),
    INSTANCE_PUT      (METHOD_HANDLE_TYPE_INSTANCE_PUT),
    INSTANCE_GET      (METHOD_HANDLE_TYPE_INSTANCE_GET),
    INVOKE_STATIC     (METHOD_HANDLE_TYPE_INVOKE_STATIC),
    INVOKE_INSTANCE   (METHOD_HANDLE_TYPE_INVOKE_INSTANCE),
    INVOKE_CONSTRUCTOR(METHOD_HANDLE_TYPE_INVOKE_CONSTRUCTOR),
    INVOKE_DIRECT     (METHOD_HANDLE_TYPE_INVOKE_DIRECT),
    INVOKE_INTERFACE  (METHOD_HANDLE_TYPE_INVOKE_INTERFACE);

    private final int value;

    MethodHandleType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static MethodHandleType of(int value) {
        for (MethodHandleType type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        throw new IllegalArgumentException("unexpected method handle type value " + value);
    }
}
