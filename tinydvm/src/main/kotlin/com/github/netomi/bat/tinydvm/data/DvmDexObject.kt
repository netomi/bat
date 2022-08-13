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

class DvmDexObject constructor(private val clazz:  DvmDexClass,
                               private var status: ObjectInitializationStatus = ObjectInitializationStatus.UNINITIALIZED): DvmObject() {

    override val obj: Any
        get() = this

    override val type: String
        get() = clazz.type

    private val instanceFields = mutableMapOf<EncodedField, DvmValue?>()

    override val isInitialized
        get() = status == ObjectInitializationStatus.INITIALIZED

    fun getClass(): DvmDexClass {
        return clazz
    }

    fun getValue(field: EncodedField): DvmValue {
        require(!field.isStatic) { "trying to get a value from static field '$field' via a DvmDexObject" }
        return instanceFields[field] ?: throw RuntimeException("field $field has no value assigned")
    }

    fun setValue(field: EncodedField, value: DvmValue) {
        require(!field.isStatic) { "trying to set a value for a static field '$field' via a DvmDexObject" }
        instanceFields[field] = value
    }

    override fun toString(): String {
        return "DexObject[type=${type}]"
    }

    companion object {
        fun newInstanceOf(clazz: DvmDexClass): DvmDexObject {
            return DvmDexObject(clazz)
        }
    }
}