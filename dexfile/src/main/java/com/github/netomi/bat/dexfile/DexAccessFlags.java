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

import static com.github.netomi.bat.dexfile.DexAccessFlags.Target.*;
import static com.github.netomi.bat.dexfile.DexConstants.*;

public enum DexAccessFlags
{
    PUBLIC               (ACC_PUBLIC,                CLASS | FIELD | METHOD),
    PRIVATE              (ACC_PRIVATE,               CLASS | FIELD | METHOD),
    PROTECTED            (ACC_PROTECTED,             CLASS | FIELD | METHOD),
    STATIC               (ACC_STATIC,                CLASS | FIELD | METHOD),
    FINAL                (ACC_FINAL,                 CLASS | FIELD | METHOD),
    SYNCHRONIZED         (ACC_SYNCHRONIZED,          METHOD),
    VOLATILE             (ACC_VOLATILE,              FIELD),
    BRIDGE               (ACC_BRIDGE,                METHOD),
    TRANSIENT            (ACC_TRANSIENT,             FIELD),
    VARARGS              (ACC_VARARGS,               METHOD),
    NATIVE               (ACC_NATIVE,                METHOD),
    INTERFACE            (ACC_INTERFACE,             CLASS),
    ABSTRACT             (ACC_ABSTRACT,              CLASS | METHOD),
    STRICT               (ACC_STRICT,                METHOD),
    SYNTHETIC            (ACC_SYNTHETIC,             CLASS | FIELD | METHOD),
    ANNOTATION           (ACC_ANNOTATION,            CLASS),
    ENUM                 (ACC_ENUM,                  CLASS | FIELD),
    CONSTRUCTOR          (ACC_CONSTRUCTOR,           METHOD),
    DECLARED_SYNCHRONIZED(ACC_DECLARED_SYNCHRONIZED, METHOD);

    private final int value;
    private final int target;

    DexAccessFlags(int value, int target) {
        this.value  = value;
        this.target = target;
    }

    public static String formatAsHumanReadable(int accessFlags, int target) {
        StringBuilder sb = new StringBuilder();
        for (DexAccessFlags accessFlag : values())
        {
            if ((accessFlag.target & target)      != 0 &&
                (accessFlag.value  & accessFlags) != 0)
            {
                String name = accessFlag.name().replaceAll("_", "-");
                sb.append(name);
                sb.append(' ');
            }
        }
        if (sb.length() > 0)
        {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }

    public static class Target
    {
        public static final int CLASS  = 0x1;
        public static final int FIELD  = 0x2;
        public static final int METHOD = 0x4;
    }
}
