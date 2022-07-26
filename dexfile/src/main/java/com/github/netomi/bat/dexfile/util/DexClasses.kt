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
        return "%s.%s:%s".format(externalClassNameFromInternalName(classDef.getClassName(dexFile)),
                                 method.getName(dexFile),
                                 method.getDescriptor(dexFile)
        )
    }

    fun parseParameters(parameters: String): List<String> {
        val result = mutableListOf<String>()

        var index = 0
        while (index < parameters.length) {
            val char = parameters[index]

            when (char) {
                'L' -> {
                    val colon = parameters.indexOf(';', index)
                    result.add(parameters.substring(index, colon + 1))
                    index = colon + 1
                }

                '[' -> {
                    var j = index + 1
                    while (parameters[j] == '[') j++
                    index = when (parameters[j]) {
                        'L' -> {
                            val colon = parameters.indexOf(';', j)
                            result.add(parameters.substring(index, colon + 1))
                            colon + 1
                        }

                        else -> {
                            result.add(parameters.substring(index, j + 1))
                            j + 1
                        }
                    }
                }

                else -> {
                    result.add(char.toString())
                    index++
                }
            }
        }

        return result
    }

    fun toShortyFormat(parameterTypes: List<String>, returnType: String): String {
        var result = toShortyFormat(returnType)

        for (parameter in parameterTypes) {
            result += toShortyFormat(parameter)
        }

        return result
    }

    fun toShortyFormat(type: String): String {
        return if (type.startsWith("L") || type.startsWith("[")) {
            "L"
        } else {
            type
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

    fun getArgumentSize(parameterTypes: Iterable<String>): Int {
        return parameterTypes.fold(0) { size, type -> size + getArgumentSizeForType(type) }
    }

    fun getArgumentSizeForType(type: String): Int {
        return when (type) {
            "B",
            "S",
            "C",
            "I",
            "Z",
            "F"  -> 1

            "J",
            "D"  -> 2

            else -> 1
        }
    }
}