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
package com.github.netomi.bat.classfile

import java.util.*

fun accessFlagsOf(visibility: Visibility, modifiers: Set<MethodModifier>): Int {
    return visibility.flagValue or modifiers.fold(0) { acc, m -> acc or m.flagValue }
}

enum class MethodModifier(val flagValue: Int) {
    STATIC      (ACC_STATIC),
    FINAL       (ACC_FINAL),
    SYNCHRONIZED(ACC_SYNCHRONIZED),
    BRIDGE      (ACC_BRIDGE),
    VARARGS     (ACC_VARARGS),
    NATIVE      (ACC_NATIVE),
    ABSTRACT    (ACC_ABSTRACT),
    STRICTFP    (ACC_STRICT),
    SYNTHETIC   (ACC_SYNTHETIC);

    companion object {
        fun setOf(accessFlags: Int): EnumSet<MethodModifier> {
            val set = EnumSet.noneOf(MethodModifier::class.java)
            for (modifier in values()) {
                if (accessFlags and modifier.flagValue != 0) {
                    set.add(modifier)
                }
            }
            return set
        }
    }
}