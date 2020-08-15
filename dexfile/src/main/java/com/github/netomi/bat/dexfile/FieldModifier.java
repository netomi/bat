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

import java.util.EnumSet;

import static com.github.netomi.bat.dexfile.DexConstants.*;

public enum FieldModifier
{
    FINAL    (ACC_FINAL),
    VOLATILE (ACC_VOLATILE),
    TRANSIENT(ACC_TRANSIENT),
    SYNTHETIC(ACC_SYNTHETIC),
    ENUM     (ACC_ENUM);

    private final int value;

    FieldModifier(int value) {
        this.value  = value;
    }

    public int getFlagValue() {
        return value;
    }

    public static EnumSet<FieldModifier> setOf(int accessFlags) {
        EnumSet<FieldModifier> set = EnumSet.noneOf(FieldModifier.class);

        for (FieldModifier modifier : values()) {
            if ((accessFlags & modifier.getFlagValue()) != 0) {
                set.add(modifier);
            }
        }

        return set;
    }
}
