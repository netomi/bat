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
    STATIC_PUT        (METHOD_HANDLE_TYPE_STATIC_PUT,         true, false,  "static-put"),
    STATIC_GET        (METHOD_HANDLE_TYPE_STATIC_GET,         true, false,  "static-get"),
    INSTANCE_PUT      (METHOD_HANDLE_TYPE_INSTANCE_PUT,       true, true,   "instance-put"),
    INSTANCE_GET      (METHOD_HANDLE_TYPE_INSTANCE_GET,       true, true,   "instance-get"),
    INVOKE_STATIC     (METHOD_HANDLE_TYPE_INVOKE_STATIC,      false, false, "invoke-static"),
    INVOKE_INSTANCE   (METHOD_HANDLE_TYPE_INVOKE_INSTANCE,    false, true,  "invoke-instance"),
    INVOKE_CONSTRUCTOR(METHOD_HANDLE_TYPE_INVOKE_CONSTRUCTOR, false, true,  "invoke-constructor"),
    INVOKE_DIRECT     (METHOD_HANDLE_TYPE_INVOKE_DIRECT,      false, true,  "invoke-direct"),
    INVOKE_INTERFACE  (METHOD_HANDLE_TYPE_INVOKE_INTERFACE,   false, true,  "invoke-interface");

    private final int     value;
    private final boolean targetsField;
    private final boolean targetsInstance;
    private final String  name;

    MethodHandleType(int value, boolean targetsField, boolean targetsInstance, String name) {
        this.value           = value;
        this.targetsField    = targetsField;
        this.targetsInstance = targetsInstance;
        this.name            = name;
    }

    public int getValue() {
        return value;
    }

    public boolean targetsField() {
        return targetsField;
    }

    public boolean targetsInstance() {
        return targetsInstance;
    }

    public String getName() {
        return name;
    }

    public static MethodHandleType of(int value) {
        try {
            return values()[value];
        } catch (Exception ex) {
            throw new IllegalArgumentException("unexpected method handle type value " + value);
        }
    }
}
