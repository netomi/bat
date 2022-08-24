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
import com.github.netomi.bat.dexfile.EncodedMethod
import com.github.netomi.bat.tinydvm.Dvm
import com.github.netomi.bat.tinydvm.Interpreter
import com.github.netomi.bat.tinydvm.data.DvmMethod
import com.github.netomi.bat.tinydvm.data.DvmValue

class DvmDexMethod private constructor(private val dvmDexClazz: DvmDexClass,
                                       private val classDef:    ClassDef,
                                       private val method:      EncodedMethod): DvmMethod() {

    private val dexFile = dvmDexClazz.dexFile

    override val name    = method.getName(dexFile)
    override val protoID = method.getProtoID(dexFile)

    override fun invoke(dvm: Dvm, vararg parameters: DvmValue): DvmValue {
        val interpreter = Interpreter.of(dvm, dexFile, classDef, method)
        return interpreter.invoke(*parameters)
    }

    companion object {
        fun of(dvmDexClazz: DvmDexClass, classDef: ClassDef, method: EncodedMethod): DvmDexMethod {
            return DvmDexMethod(dvmDexClazz, classDef, method)
        }
    }
}