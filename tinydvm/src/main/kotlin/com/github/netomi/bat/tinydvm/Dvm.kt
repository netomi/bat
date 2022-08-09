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

package com.github.netomi.bat.tinydvm

import com.github.netomi.bat.dexfile.DexFile
import com.github.netomi.bat.dexfile.FieldID
import com.github.netomi.bat.tinydvm.data.*

class Dvm constructor(private val dexFile: DexFile) {

    private val dvmDexClassMap    = mutableMapOf<String, DvmDexClass>()
    private val dvmNativeClassMap = mutableMapOf<String, DvmNativeClass>()

    fun getClass(type: String): DvmClass {
        val classDef = dexFile.getClassDefByType(type)
        return if (classDef != null) {
            val clazz = dvmDexClassMap.computeIfAbsent(type) { DvmDexClass.of(dexFile, classDef) }
            if (!clazz.isInitialized) {
                clazz.initialize(this)
            }
            clazz
        } else {
            dvmNativeClassMap.computeIfAbsent(type) { DvmNativeClass.of(type) }
        }
    }

    internal fun getField(dexFile: DexFile, fieldID: FieldID): DvmField? {
        val name      = fieldID.getName(dexFile)
        val type      = fieldID.getType(dexFile)
        val classType = fieldID.getClassType(dexFile)

        val dvmClazz = getClass(classType)
        return dvmClazz.getField(name, type)
    }
}