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

import com.github.netomi.bat.dexfile.EncodedField

class DvmDexObject constructor(private val clazz: DvmDexClass): DvmObject() {

    override val obj: Any
        get() = this

    override val type: String
        get() = clazz.type

    private val instanceFields = mutableMapOf<EncodedField, DvmValue?>()

    private var status = InitializationStatus.NOT_INITIALIZED

    val isInitialized = status == InitializationStatus.INITIALIZED

    fun getClass(): DvmDexClass {
        return clazz
    }

    fun getValue(field: EncodedField): DvmValue {
        return instanceFields[field] ?: throw RuntimeException("field $field has no value assigned")
    }

    fun setValue(field: EncodedField, value: DvmValue) {
        instanceFields[field] = value
    }

    companion object {
        fun newInstanceOf(clazz: DvmDexClass): DvmDexObject {
            return DvmDexObject(clazz)
        }
    }
}