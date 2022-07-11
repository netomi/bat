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
package com.github.netomi.bat.dexfile.util

import com.github.netomi.bat.dexfile.ClassDef
import com.github.netomi.bat.dexfile.DexFile
import com.github.netomi.bat.dexfile.EncodedMethod
import com.github.netomi.bat.dexfile.value.*
import com.github.netomi.bat.util.Classes.externalClassNameFromInternalName

object DexClasses {

    fun fullExternalMethodSignature(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod): String {
        return String.format(
            "%s.%s:%s",
            externalClassNameFromInternalName(classDef.getClassName(dexFile)),
            method.getName(dexFile),
            method.getDescriptor(dexFile)
        )
    }

    fun toShortyFormat(parameterTypes: List<String>, returnType: String): String {
        var result = toShortyFormat(returnType)

        for (parameter in parameterTypes) {
            result += toShortyFormat(parameter)
        }

        return result
    }

    fun toShortyFormat(type: String): String {
        if (type.startsWith("L") || type.startsWith("[")) {
            return "L"
        } else {
            return type
        }
    }

    @JvmStatic
    fun getDefaultEncodedValueForType(type: String): EncodedValue {
        when (type) {
            "B" -> return EncodedByteValue.of(0x00.toByte())
            "S" -> return EncodedShortValue.of(0x00.toShort())
            "C" -> return EncodedCharValue.of(0x00.toChar())
            "I" -> return EncodedIntValue.of(0)
            "J" -> return EncodedLongValue.of(0L)
            "F" -> return EncodedFloatValue.of(0.0f)
            "D" -> return EncodedDoubleValue.of(0.0)
            "Z" -> return EncodedBooleanValue.of(false)
        }
        return if (type.startsWith("L") && type.endsWith(";")) {
            EncodedNullValue
        } else {
            EncodedNullValue
        }
    }
}