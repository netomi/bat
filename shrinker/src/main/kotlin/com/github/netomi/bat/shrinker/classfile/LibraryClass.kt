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

import com.github.netomi.bat.classfile.Field
import com.github.netomi.bat.classfile.Method
import com.github.netomi.bat.classfile.io.ClassDataInput
import com.github.netomi.bat.shrinker.visitor.AnyClassVisitor

class LibraryClass: AnyClass() {

    @Suppress("UNCHECKED_CAST")
    override val fields: List<LibraryField>
        get() = super.fields as List<LibraryField>

    @Suppress("UNCHECKED_CAST")
    override val methods: List<LibraryMethod>
        get() = super.methods as List<LibraryMethod>

    override fun accept(visitor: AnyClassVisitor) {
        visitor.visitLibraryClass(this)
    }

    override fun readField(input: ClassDataInput): Field {
        return LibraryField.read(input)
    }

    override fun readMethod(input: ClassDataInput): Method {
        return LibraryMethod.read(input)
    }
}