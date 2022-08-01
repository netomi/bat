/*
 *  Copyright (c) 2020-2022 Thomas Neidhart.
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

package com.github.netomi.bat.classfile.util

import java.util.*

fun isClassType(type: String): Boolean {
    return type.startsWith("L") && type.endsWith(";")
}

fun isArrayType(type: String): Boolean {
    return type.startsWith("[")
}

fun internalClassNameFromInternalType(type: String): String {
    Objects.requireNonNull(type)
    return if (isClassType(type)) {
        type.substring(1, type.length - 1)
    } else type
}

fun internalTypeFromInternalClassName(internalClassName: String): String {
    Objects.requireNonNull(internalClassName)
    return "L$internalClassName;"
}

fun internalClassNameFromExternalClassName(externalClassName: String): String {
    Objects.requireNonNull(externalClassName)
    return externalClassName.replace(".".toRegex(), "/")
}

fun externalClassNameFromInternalClassName(internalClassName: String): String {
    Objects.requireNonNull(internalClassName)
    return internalClassName.replace("/".toRegex(), ".")
}

fun simpleClassNameFromInternalClassName(internalClassName: String): String {
    val idx = internalClassName.lastIndexOf('/')
    return if (idx == -1) internalClassName else internalClassName.substring(idx + 1)
}

fun internalPackageNameFromInternalClassName(internalClassName: String): String {
    val idx = internalClassName.lastIndexOf('/')
    return if (idx == -1) "" else internalClassName.substring(0, idx)
}

fun externalTypeFromInternalType(internalType: String): String {
    Objects.requireNonNull(internalType)
    return if (isClassType(internalType)) {
        val className = internalClassNameFromInternalType(internalType)
        externalClassNameFromInternalClassName(className)
    } else if (isArrayType(internalType)) {
        val dimension = internalType.chars().filter { ch: Int -> ch == '['.code }.count().toInt()
        val type = internalType.substring(dimension)
        var result = externalTypeFromInternalType(type)
        result += "[]".repeat(dimension)
        result
    } else {
        when (internalType) {
            "B" -> "byte"
            "C" -> "char"
            "D" -> "double"
            "F" -> "float"
            "I" -> "int"
            "J" -> "long"
            "S" -> "short"
            "Z" -> "boolean"
            else -> internalType
        }
    }
}
