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

import com.github.netomi.bat.util.asExternalJavaClassName
import com.github.netomi.bat.util.asJvmType

class DvmNativeClass private constructor(private val clazz: Class<Any>): DvmClass() {

    override val type: String
        get() = clazz.name.asExternalJavaClassName().toInternalType()

    override val className: String
        get() = clazz.name.asExternalJavaClassName().toInternalClassName()

    private val fieldCache = mutableMapOf<String, DvmNativeField?>()

    override fun getField(name: String, type: String): DvmField? {
        return fieldCache.computeIfAbsent(name) { fieldName ->
            try {
                DvmNativeField.of(clazz.getField(fieldName), type)
            } catch (exception: NoSuchFieldException) {
                null
            }
        }
    }

    companion object {
        fun of(type: String): DvmNativeClass {
            val externalClassName = type.asJvmType().toExternalClassName()
            return DvmNativeClass(Class.forName(externalClassName) as Class<Any>)
        }
    }
}