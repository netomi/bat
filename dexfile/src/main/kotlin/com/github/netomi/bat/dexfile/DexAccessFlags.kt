/*
 *  Copyright (c) 2020 Thomas Neidhart.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law, agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES, CONDITIONS OF ANY KIND, either express, implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.github.netomi.bat.dexfile

import com.github.netomi.bat.dexfile.DexAccessFlagTarget.*
import java.util.*
import kotlin.Array
import kotlin.Int
import kotlin.String
import kotlin.arrayOf

fun formatAccessFlagsAsHumanReadable(accessFlags: Int, target: DexAccessFlagTarget): String {
    return DexAccessFlag.values().filter { it.target and target.value != 0 && it.value and accessFlags != 0 }
                                 .joinToString(separator = " ") { it.name }
}

enum class DexAccessFlagTarget(val value: Int) {
    CLASS (0x01),
    FIELD (0x02),
    METHOD(0x04)
}

enum class DexAccessFlag(val value: Int, val target: Int) {
    PUBLIC(ACC_PUBLIC,                               arrayOf(CLASS, FIELD, METHOD)),
    PRIVATE(ACC_PRIVATE,                             arrayOf(CLASS, FIELD, METHOD)),
    PROTECTED(ACC_PROTECTED,                         arrayOf(CLASS, FIELD, METHOD)),
    STATIC(ACC_STATIC,                               arrayOf(CLASS, FIELD, METHOD)),
    FINAL(ACC_FINAL,                                 arrayOf(CLASS, FIELD, METHOD)),
    SYNCHRONIZED(ACC_SYNCHRONIZED,                   arrayOf(METHOD)),
    VOLATILE(ACC_VOLATILE,                           arrayOf(FIELD)),
    BRIDGE(ACC_BRIDGE,                               arrayOf(METHOD)),
    TRANSIENT(ACC_TRANSIENT,                         arrayOf(FIELD)),
    VARARGS(ACC_VARARGS,                             arrayOf(METHOD)),
    NATIVE(ACC_NATIVE,                               arrayOf(METHOD)),
    INTERFACE(ACC_INTERFACE,                         arrayOf(CLASS)),
    ABSTRACT(ACC_ABSTRACT,                           arrayOf(CLASS, METHOD)),
    STRICTFP(ACC_STRICT,                             arrayOf(METHOD)),
    SYNTHETIC(ACC_SYNTHETIC,                         arrayOf(CLASS, FIELD, METHOD)),
    ANNOTATION(ACC_ANNOTATION,                       arrayOf(CLASS)),
    ENUM(ACC_ENUM,                                   arrayOf(CLASS, FIELD)),
    CONSTRUCTOR(ACC_CONSTRUCTOR,                     arrayOf(METHOD)),
    DECLARED_SYNCHRONIZED(ACC_DECLARED_SYNCHRONIZED, arrayOf(METHOD));

    constructor(value: Int, targets: Array<DexAccessFlagTarget>): this(value, targets.fold(0) { acc, t -> acc or t.value })

    companion object {
        fun of(input: String): DexAccessFlag {
            val cleanedInput = input.replace("-".toRegex(), "_").uppercase(Locale.getDefault())
            return valueOf(cleanedInput)
        }

        fun flagsByTarget(target: DexAccessFlagTarget): Collection<DexAccessFlag> {
            return values().filter { it.target and target.value != 0 }
        }
    }
}