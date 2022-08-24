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
import com.github.netomi.bat.tinydvm.Dvm

class DVMDexField private constructor(private val dvmDexClass: DvmDexClass,
                                      private val field:       EncodedField): DvmField() {

    private val dexFile = dvmDexClass.dexFile

    override val name = field.getName(dexFile)
    override val type = field.getType(dexFile).type

    override fun get(dvm: Dvm, obj: Any?): DvmValue {
        return if (field.isStatic) {
            dvmDexClass.getValueOfStaticField(field)
        } else {
            require(obj is DvmDexObject)
            return obj.getValue(field)
        }
    }

    override fun set(dvm: Dvm, obj: Any?, value: DvmValue) {
        if (field.isStatic) {
            dvmDexClass.setValueForStaticField(field, value)
        } else {
            require(obj is DvmDexObject)
            obj.setValue(field, value)
        }
    }

    companion object {
        internal fun of(dvmDexClass: DvmDexClass, field: EncodedField): DVMDexField {
            return DVMDexField(dvmDexClass, field)
        }
    }
}
