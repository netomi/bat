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
package com.github.netomi.bat.dexfile

import java.util.*

enum class FieldModifier(val flagValue: Int) {
    STATIC   (ACC_STATIC),
    FINAL    (ACC_FINAL),
    VOLATILE (ACC_VOLATILE),
    TRANSIENT(ACC_TRANSIENT),
    SYNTHETIC(ACC_SYNTHETIC),
    ENUM     (ACC_ENUM);

    companion object {
        @JvmStatic
        fun setOf(accessFlags: Int): EnumSet<FieldModifier> {
            val set = EnumSet.noneOf(FieldModifier::class.java)
            for (modifier in values()) {
                if (accessFlags and modifier.flagValue != 0) {
                    set.add(modifier)
                }
            }
            return set
        }
    }
}