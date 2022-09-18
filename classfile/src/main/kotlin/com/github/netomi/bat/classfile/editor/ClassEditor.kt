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

package com.github.netomi.bat.classfile.editor

import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.Field
import com.github.netomi.bat.classfile.constant.editor.ConstantPoolEditor

class ClassEditor private constructor(private val classFile: ClassFile) {

    private val constantPoolEditor = ConstantPoolEditor.of(classFile)

    fun addField(name: String, accessFlags: Int, descriptor: String): FieldEditor {
        val nameIndex       = constantPoolEditor.addOrGetUtf8ConstantIndex(name)
        val descriptorIndex = constantPoolEditor.addOrGetUtf8ConstantIndex(descriptor)

        val field = Field.of(nameIndex, accessFlags, descriptorIndex)
        classFile.addField(field)

        return FieldEditor.of(this, field)
    }

    companion object {
        fun of(classFile: ClassFile): ClassEditor {
            return ClassEditor(classFile)
        }
    }
}