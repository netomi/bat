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

public enum DexAccessFlags {
    PUBLIC               (ACC_PUBLIC),
    PRIVATE              (ACC_PRIVATE),
    PROTECTED            (ACC_PROTECTED),
    STATIC               (ACC_STATIC),
    FINAL                (ACC_FINAL),
    SYNCHRONIZED         (ACC_SYNCHRONIZED),
    VOLATILE             (ACC_VOLATILE),
    BRIDGE               (ACC_BRIDGE),
    TRANSIENT            (ACC_TRANSIENT),
    VARARGS              (ACC_VARARGS),
    NATIVE               (ACC_NATIVE),
    INTERFACE            (ACC_INTERFACE),
    ABSTRACT             (ACC_ABSTRACT),
    STRICT               (ACC_STRICT),
    SYNTHETIC            (ACC_SYNTHETIC),
    ANNOTATION           (ACC_ANNOTATION),
    ENUM                 (ACC_ENUM),
    CONSTRUCTOR          (ACC_CONSTRUCTOR),
    DECLARED_SYNCHRONIZED(ACC_DECLARED_SYNCHRONIZED);

    private final int value;

    DexAccessFlags(int value) {
        this.value = value;
    }

    public static String formatAsHumanReadable(int accessFlags) {
        StringBuilder sb = new StringBuilder();
        for (DexAccessFlags accessFlag : values()) {
            if ((accessFlag.value & accessFlags) != 0) {
                sb.append(accessFlag.name());
                sb.append(' ');
            }
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }
}
