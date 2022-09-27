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

package com.github.netomi.bat.shrinker.classfile

import com.github.netomi.bat.classfile.io.ClassDataInput
import com.github.netomi.bat.shrinker.visitor.AnyFieldVisitor

class LibraryField: AnyField() {

    override fun accept(clazz: AnyClass, visitor: AnyFieldVisitor) {
        visitor.visitLibraryField(clazz, this)
    }

    companion object {
        internal fun read(input: ClassDataInput): LibraryField {
            val field = LibraryField()
            field.read(input)
            return field
        }
    }
}