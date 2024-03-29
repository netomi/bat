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

import com.github.netomi.bat.tinydvm.data.DvmClass
import com.github.netomi.bat.tinydvm.data.DvmObject
import com.github.netomi.bat.util.JvmType

class DvmNativeObject private constructor(private var _obj:  Any? = null,
                                          private val clazz: DvmNativeClass): DvmObject() {

    override val obj: Any
        get() = _obj!!

    override val type: JvmType
        get() = clazz.type

    override val isInitialized: Boolean
        get() = _obj != null

    override fun toString(): String {
        return "NativeObject[obj=${obj}, type=${type}]"
    }

    companion object {
        fun newInstanceOf(clazz: DvmNativeClass): DvmNativeObject {
            return DvmNativeObject(clazz = clazz)
        }

        fun of(obj: Any, clazz: DvmClass): DvmNativeObject {
            require(clazz is DvmNativeClass)
            return DvmNativeObject(obj, clazz)
        }
    }
}