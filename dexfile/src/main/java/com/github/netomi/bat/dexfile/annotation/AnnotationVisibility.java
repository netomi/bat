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
package com.github.netomi.bat.dexfile.annotation;

import com.github.netomi.bat.dexfile.DexConstants;

public enum AnnotationVisibility
{
    VISIBILITY_BUILD  (DexConstants.VISIBILITY_BUILD,   "build"),
    VISIBILITY_RUNTIME(DexConstants.VISIBILITY_RUNTIME, "runtime"),
    VISIBILITY_SYSTEM (DexConstants.VISIBILITY_SYSTEM,  "system");

    private final short  value;
    private final String name;

    AnnotationVisibility(int value, String name) {
        this.value = (short) value;
        this.name  = name;
    }

    public short getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    public static AnnotationVisibility of(int value) {
        try {
            return values()[value];
        } catch (Exception ex) {
            throw new IllegalArgumentException("unexpected annotation visibility value " + value);
        }
    }
}
