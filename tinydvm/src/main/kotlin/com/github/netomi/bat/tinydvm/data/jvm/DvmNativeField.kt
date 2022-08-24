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

package com.github.netomi.bat.tinydvm.data.jvm

import com.github.netomi.bat.tinydvm.Dvm
import com.github.netomi.bat.tinydvm.data.DvmField
import com.github.netomi.bat.tinydvm.data.DvmValue
import com.github.netomi.bat.util.JvmType
import java.lang.reflect.Field

class DvmNativeField private constructor(private val  field: Field,
                                         override val type:  JvmType): DvmField() {

    override val name: String = field.name

    override fun get(dvm: Dvm, obj: Any?): DvmValue {
        return DvmValue.ofNativeValue(dvm, field.get(obj), type)
    }

    override fun set(dvm: Dvm, obj: Any?, value: DvmValue) {
        field.set(obj, value.value)
    }

    companion object {
        internal fun of(field: Field, type: JvmType): DvmNativeField {
            return DvmNativeField(field, type)
        }
    }
}