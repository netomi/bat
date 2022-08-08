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

package com.github.netomi.bat.tinydvm.data

import com.github.netomi.bat.dexfile.util.DexClasses.isArrayType

class DvmReferenceValue private constructor(private val reference: Any?, override val type: String): DvmValue() {

    override val value: Any?
        get() = reference

    val isArrayValue = isArrayType(type)

    override fun valueOfType(type: String): Any? {
        return value
    }

    companion object {
        fun of(obj: Any?, type: String): DvmReferenceValue {
            return DvmReferenceValue(obj, type)
        }
    }
}