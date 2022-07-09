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
package com.github.netomi.bat.dexfile.util;

import com.github.netomi.bat.dexfile.ClassDef;
import com.github.netomi.bat.dexfile.DexFile;
import com.github.netomi.bat.dexfile.EncodedMethod;
import com.github.netomi.bat.dexfile.value.*;
import com.github.netomi.bat.util.Classes;

public class DexClasses
extends      Classes
{
    private DexClasses() {}

    public static String fullExternalMethodSignature(DexFile dexFile, ClassDef classDef, EncodedMethod method) {
        return String.format("%s.%s:%s",
            externalClassNameFromInternalName(classDef.getClassName(dexFile)),
            method.getName(dexFile),
            method.getDescriptor(dexFile));
    }

    public static EncodedValue getDefaultEncodedValueForType(String type) {
        switch (type) {
            case "B": return EncodedByteValue.of((byte) 0x00);
            case "S": return EncodedShortValue.of((short) 0x00);
            case "C": return EncodedCharValue.of((char) 0x00);
            case "I": return EncodedIntValue.of(0);
            case "J": return EncodedLongValue.of(0l);
            case "F": return EncodedFloatValue.of(0.0f);
            case "D": return EncodedDoubleValue.of(0.0);
            case "Z": return EncodedBooleanValue.of(false);
        }

        if (type.startsWith("L") && type.endsWith(";")) {
            return EncodedNullValue.INSTANCE;
        } else {
            return EncodedNullValue.INSTANCE;
        }
    }

}
