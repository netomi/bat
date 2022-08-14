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

import com.github.netomi.bat.dexfile.ClassDef
import com.github.netomi.bat.dexfile.DexFile
import com.github.netomi.bat.dexfile.EncodedField
import com.github.netomi.bat.dexfile.visitor.fieldCollector
import com.github.netomi.bat.tinydvm.Dvm

class DvmDexClass private constructor(        val dexFile:  DexFile,
                                      private val classDef: ClassDef,
                                      private var status:   ClassInitializationStatus = ClassInitializationStatus.UNINITIALIZED): DvmClass() {

    override val type: String
        get() = classDef.getType(dexFile)

    override val className: String
        get() = classDef.getClassName(dexFile)

    val isInitialized = status == ClassInitializationStatus.INITIALIZED

    private val staticFields = mutableMapOf<EncodedField, DvmValue>()

    private val fieldCache = mutableMapOf<Pair<String, String>, DVMDexField?>()

    override fun getField(name: String, type: String): DvmField? {
        return fieldCache.computeIfAbsent(Pair(name, type)) {
            val fieldCollector = fieldCollector()
            classDef.fieldsAccept(dexFile, name, type, fieldCollector)
            val field = fieldCollector.items().singleOrNull()
            if (field != null) DVMDexField.of(this, field) else null
        }
    }

    fun getValueOfStaticField(field: EncodedField): DvmValue {
        require(field.isStatic) { "trying to get a value from non-static field '$field'" }
        return staticFields[field]!!
    }

    fun setValueForStaticField(field: EncodedField, value: DvmValue) {
        require(field.isStatic) { "trying to set a value for non-static field '$field'" }
        staticFields[field] = value
    }

    internal fun initialize(dvm: Dvm) {
        if (status != ClassInitializationStatus.UNINITIALIZED) {
            return
        }

        status = ClassInitializationStatus.INITIALIZING

        val superType = classDef.getSuperClassType(dexFile)
        if (superType != null) {
            dvm.getClass(superType)
        }

        classDef.staticFields.forEach { field ->
            val initialValue = field.staticValue(dexFile) ?: field.getDexType(dexFile).getDefaultEncodedValueForType()
            staticFields[field] = initialValue.toDVMValue(dvm, dexFile, field.getType(dexFile))
        }


        // TODO: execute <clinit> method to complete initialization

        status = ClassInitializationStatus.INITIALIZED
    }

    companion object {
        internal fun of(dexFile: DexFile, classDef: ClassDef): DvmDexClass {
            return DvmDexClass(dexFile, classDef)
        }
    }
}
