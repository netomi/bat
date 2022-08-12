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

package com.github.netomi.bat.util

// common class type constants
const val JAVA_LANG_OBJECT_TYPE = "Ljava/lang/Object;"
const val JAVA_LANG_STRING_TYPE = "Ljava/lang/String;"

// primitive types
const val INT_TYPE     = "I"
const val LONG_TYPE    = "J"
const val SHORT_TYPE   = "S"
const val BYTE_TYPE    = "B"
const val BOOLEAN_TYPE = "Z"
const val CHAR_TYPE    = "C"
const val FLOAT_TYPE   = "F"
const val DOUBLE_TYPE  = "D"
const val VOID_TYPE    = "V"

// utility functions for java types

fun String.asJavaType(): JavaType {
    return JavaType.of(this)
}

fun String.asInternalJavaClassName(): JavaClassName {
    return JavaClassName.ofInternal(this)
}

fun String.asExternalJavaClassName(): JavaClassName {
    return JavaClassName.ofExternal(this)
}

open class JavaType protected constructor(val type: String) {

    val isClassType: Boolean
        get() = type.startsWith("L") && type.endsWith(";")

    val isArrayType: Boolean
        get() = type.startsWith("[")

    val isReferenceType: Boolean
        get() = isClassType || isArrayType

    val isPrimitiveType: Boolean
        get() {
            return when (type) {
                INT_TYPE,
                LONG_TYPE,
                SHORT_TYPE,
                BYTE_TYPE,
                CHAR_TYPE,
                FLOAT_TYPE,
                DOUBLE_TYPE,
                BOOLEAN_TYPE -> true
                else         -> false
            }
        }

    fun toInternalClassName(): String {
        require(isClassType)
        return type.removeSurrounding("L", ";")
    }

    fun toExternalClassName(): String {
        return toInternalClassName().replace("/", ".")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is JavaType) return false

        if (type != other.type) return false

        return true
    }

    override fun hashCode(): Int {
        return type.hashCode()
    }

    override fun toString(): String {
        return type
    }

    companion object {
        fun of(type: String): JavaType {
            return JavaType(type)
        }
    }
}

class JavaClassName private constructor(val className: String, val internal: Boolean) {

    fun toExternalClassName(): String {
        return if (internal) className.replace('/', '.') else className
    }

    fun toInternalClassName(): String {
        return if (internal) className else className.replace('.', '/')
    }

    fun toInternalType(): String {
        return "L${toInternalClassName()};"
    }

    companion object {
        fun ofInternal(className: String): JavaClassName {
            return JavaClassName(className, true)
        }

        fun ofExternal(className: String): JavaClassName {
            return JavaClassName(className, false)
        }
    }
}