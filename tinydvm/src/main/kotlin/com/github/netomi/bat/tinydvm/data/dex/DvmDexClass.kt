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

package com.github.netomi.bat.tinydvm.data.dex

import com.github.netomi.bat.dexfile.ClassDef
import com.github.netomi.bat.dexfile.DexFile
import com.github.netomi.bat.dexfile.EncodedField
import com.github.netomi.bat.dexfile.ProtoID
import com.github.netomi.bat.dexfile.util.DexType
import com.github.netomi.bat.dexfile.visitor.fieldCollector
import com.github.netomi.bat.dexfile.visitor.filterMethodsByNameAndProtoID
import com.github.netomi.bat.dexfile.visitor.methodCollector
import com.github.netomi.bat.tinydvm.Dvm
import com.github.netomi.bat.tinydvm.data.*
import com.github.netomi.bat.util.JvmType

class DvmDexClass private constructor(        val dexFile:  DexFile,
                                      private val classDef: ClassDef,
                                      private var status:   ClassInitializationStatus = ClassInitializationStatus.UNINITIALIZED): DvmClass() {

    override val type: DexType
        get() = classDef.getType(dexFile)

    override val className: String
        get() = classDef.getClassName(dexFile)

    val isInitialized = status == ClassInitializationStatus.INITIALIZED

    private val staticFields = mutableMapOf<EncodedField, DvmValue>()

    private val fieldCache = mutableMapOf<Pair<String, String>, DVMDexField?>()

    override fun getField(name: String, type: JvmType): DvmField? {
        return fieldCache.computeIfAbsent(Pair(name, type.type)) {
            val fieldCollector = fieldCollector()
            classDef.fieldsAccept(dexFile, name, type.type, fieldCollector)
            val field = fieldCollector.items().singleOrNull()
            if (field != null) DVMDexField.of(this, field) else null
        }
    }

    override fun getDirectMethod(dexFile: DexFile, name: String, protoID: ProtoID): DvmMethod? {
        val methodCollector = methodCollector()
        classDef.directMethodsAccept(dexFile, filterMethodsByNameAndProtoID(name, protoID, methodCollector))
        val method = methodCollector.items().singleOrNull()
        return if (method != null) DvmDexMethod.of(this, classDef, method) else null
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
            val initialValue = field.staticValue(dexFile) ?: field.getType(dexFile).getDefaultEncodedValueForType()
            staticFields[field] = initialValue.toDVMValue(dvm, dexFile, field.getType(dexFile))
        }


        // TODO: execute <clinit> method to complete initialization

        status = ClassInitializationStatus.INITIALIZED
    }

    override fun toString(): String {
        return "DvmDexClass[type=$type]"
    }

    companion object {
        internal fun of(dexFile: DexFile, classDef: ClassDef): DvmDexClass {
            return DvmDexClass(dexFile, classDef)
        }
    }
}
