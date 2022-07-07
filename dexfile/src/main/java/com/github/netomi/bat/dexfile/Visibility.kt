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

import com.github.netomi.bat.util.Primitives

enum class Visibility(val flagValue: Int) {
    PUBLIC         (DexConstants.ACC_PUBLIC),
    PRIVATE        (DexConstants.ACC_PRIVATE),
    PROTECTED      (DexConstants.ACC_PROTECTED),
    PACKAGE_PRIVATE(0x0);

    companion object {
        @JvmStatic
        fun of(accessFlags: Int): Visibility {
            val value = accessFlags and 0xf
            for (visibility in values()) {
                if (value == visibility.flagValue) {
                    return visibility
                }
            }
            throw IllegalStateException("unexpected accessFlags ${Primitives.toHexString(accessFlags)}")
        }
    }
}