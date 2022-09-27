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
import com.github.netomi.bat.shrinker.classfile.AnyField
import com.github.netomi.bat.shrinker.classfile.LibraryField
import com.github.netomi.bat.shrinker.classfile.ProgramField

fun interface AnyFieldVisitor {
    fun visitAnyField(clazz: AnyClass, field: AnyField)

    fun visitProgramField(clazz: AnyClass, field: ProgramField) {
        visitAnyField(clazz, field)
    }

    fun visitLibraryField(clazz: AnyClass, field: LibraryField) {
        visitAnyField(clazz, field)
    }
}