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

package com.github.netomi.bat.shrinker.visitor

import com.github.netomi.bat.shrinker.classfile.AnyClass
import com.github.netomi.bat.shrinker.classfile.AnyMethod
import com.github.netomi.bat.shrinker.classfile.LibraryMethod
import com.github.netomi.bat.shrinker.classfile.ProgramMethod

fun interface AnyMethodVisitor {
    fun visitAnyMethod(clazz: AnyClass, method: AnyMethod)

    fun visitProgramMethod(clazz: AnyClass, method: ProgramMethod) {
        visitAnyMethod(clazz, method)
    }

    fun visitLibraryMethod(clazz: AnyClass, method: LibraryMethod) {
        visitAnyMethod(clazz, method)
    }
}