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
package com.github.netomi.bat.dexfile.annotation

import com.github.netomi.bat.dexfile.VISIBILITY_BUILD
import com.github.netomi.bat.dexfile.VISIBILITY_RUNTIME
import com.github.netomi.bat.dexfile.VISIBILITY_SYSTEM

enum class AnnotationVisibility(val value: Short, val simpleName: String) {
    BUILD  (VISIBILITY_BUILD,   "build"),
    RUNTIME(VISIBILITY_RUNTIME, "runtime"),
    SYSTEM (VISIBILITY_SYSTEM,  "system");

    companion object {
        fun of(simpleName: String): AnnotationVisibility {
            for (visibility in values()) {
                if (visibility.simpleName == simpleName) return visibility
            }
            throw IllegalArgumentException("unexpected annotation visibility name $simpleName")
        }

        fun of(value: Int): AnnotationVisibility {
            require(value in 0 until values().size) { "unexpected annotation visibility value '$value'" }
            // the values correspond to the enum ordinals, thus we can lookup by index.
            return values()[value]
        }
    }
}