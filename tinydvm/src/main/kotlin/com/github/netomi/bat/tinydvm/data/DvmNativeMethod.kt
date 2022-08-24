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

import com.github.netomi.bat.dexfile.DexFile
import com.github.netomi.bat.dexfile.ProtoID
import com.github.netomi.bat.tinydvm.Dvm
import java.lang.reflect.Method

class DvmNativeMethod private constructor(private val  method:  Method,
                                          private val  dexFile: DexFile,
                                          override val protoID: ProtoID): DvmMethod() {

    override val name: String = method.name

    override fun invoke(dvm: Dvm, vararg parameters: DvmValue): DvmValue {
        val obj = parameters[0]
        // FIXME: just a hack to override testing
        val value = obj.value
        method.invoke(null, value)
        return DvmValue.ofUnitValue()
    }

    companion object {
        internal fun of(method: Method, dexFile: DexFile, protoID: ProtoID): DvmNativeMethod {
            return DvmNativeMethod(method, dexFile, protoID)
        }
    }
}