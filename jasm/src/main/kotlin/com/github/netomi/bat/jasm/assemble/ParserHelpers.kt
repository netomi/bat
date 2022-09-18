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

package com.github.netomi.bat.jasm.assemble

import com.github.netomi.bat.classfile.AccessFlag
import com.github.netomi.bat.jasm.parser.JasmParser

internal fun parseAccessFlags(sAccListContext: JasmParser.SAccListContext): Int {
    var accessFlags = 0
    sAccListContext.ACC().forEach {
        val flag = AccessFlag.of(it.text)
        accessFlags = accessFlags or flag.value
    }
    return accessFlags
}
