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

class DvmNativeObject private constructor(private var _obj:  Any? = null,
                                                      _type: String): DvmObject() {

    override val obj: Any?
        get() = _obj

    override val type: String = _type

    override val isInitialized: Boolean
        get() = obj != null

    override fun toString(): String {
        return "NativeObject[obj=${obj}, type=${type}]"
    }

    companion object {
        fun newInstanceOf(clazz: DvmNativeClass): DvmNativeObject {
            return DvmNativeObject(_type = clazz.type)
        }

        fun of(obj: Any, type: String): DvmNativeObject {
            return DvmNativeObject(obj, type)
        }
    }
}