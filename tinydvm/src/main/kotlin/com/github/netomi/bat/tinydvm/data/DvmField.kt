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

import com.github.netomi.bat.tinydvm.Dvm
import com.github.netomi.bat.util.JvmType

abstract class DvmField {
    abstract val name: String
    abstract val type: JvmType

    abstract fun get(dvm: Dvm, obj: Any?): DvmValue
    abstract fun set(dvm: Dvm, obj: Any?, value: DvmValue)
}
