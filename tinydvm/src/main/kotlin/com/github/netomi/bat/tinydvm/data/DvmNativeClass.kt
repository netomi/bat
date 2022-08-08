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

import com.github.netomi.bat.dexfile.util.DexClasses.externalClassNameFromInternalType
import com.github.netomi.bat.dexfile.util.DexClasses.internalClassNameFromExternalClassName
import com.github.netomi.bat.dexfile.util.DexClasses.internalTypeFromInternalClassName

class DvmNativeClass private constructor(private val clazz: Class<Any>): DvmClass() {
    override val type: String
        get() = internalTypeFromInternalClassName(clazz.name)

    override val className: String
        get() = internalClassNameFromExternalClassName(clazz.name)

    override fun getField(name: String, type: String): DvmField {
        return DvmNativeField.of(clazz.getField(name), type)
    }

    companion object {
        fun of(type: String): DvmNativeClass {
            val externalClassName = externalClassNameFromInternalType(type)
            return DvmNativeClass(Class.forName(externalClassName) as Class<Any>)
        }
    }
}