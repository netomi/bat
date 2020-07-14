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

public final class DexUtil
{
    private DexUtil() {}

    public static String internalClassNameFromType(String type) {
        if (type == null) {
            return null;
        }

        if (type.startsWith("L") && type.endsWith(";")) {
            return type.substring(1, type.length() - 1);
        }
        return type;
    }

    public static String internalClassNameFromExternalName(String className) {
        return className.replaceAll(".", "/");
    }

    public static String externalClassNameFromInternalName(String className) {
        return className.replaceAll("/", ".");
    }

    public static String fullExternalMethodSignature(DexFile dexFile, ClassDef classDef, EncodedMethod method) {
        return String.format("%s.%s:%s",
                externalClassNameFromInternalName(classDef.getClassName(dexFile)),
                method.getName(dexFile),
                method.getTypeSignature(dexFile));
    }
}
