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
import com.github.netomi.bat.classfile.Method
import com.github.netomi.bat.classfile.attribute.Attribute
import com.github.netomi.bat.classfile.attribute.AttributeMap
import com.github.netomi.bat.classfile.constant.editor.ConstantPoolEditor

class ClassEditor private constructor(private val classFile: ClassFile): AttributeEditor() {

    override val constantPoolEditor = ConstantPoolEditor.of(classFile)

    override val attributeMap: AttributeMap
        get() = classFile.attributeMap

    override fun addAttribute(attribute: Attribute) {
        classFile.addAttribute(attribute)
    }

    fun addField(name: String, accessFlags: Int, descriptor: String): FieldEditor {
        val nameIndex       = constantPoolEditor.addOrGetUtf8ConstantIndex(name)
        val descriptorIndex = constantPoolEditor.addOrGetUtf8ConstantIndex(descriptor)

        val field = Field.of(nameIndex, accessFlags, descriptorIndex)
        classFile.addField(field)

        return FieldEditor.of(constantPoolEditor, field)
    }

    fun addMethod(name: String, accessFlags: Int, parameterTypes: List<String>, returnType: String): MethodEditor {
        val descriptor = buildString {
            append(parameterTypes.joinToString(separator = "", prefix = "(", postfix = ")"))
            append(returnType)
        }
        return addMethod(name, accessFlags, descriptor)
    }

    fun addMethod(name: String, accessFlags: Int, descriptor: String): MethodEditor {
        val nameIndex       = constantPoolEditor.addOrGetUtf8ConstantIndex(name)
        val descriptorIndex = constantPoolEditor.addOrGetUtf8ConstantIndex(descriptor)

        val method = Method.of(nameIndex, accessFlags, descriptorIndex)
        classFile.addMethod(method)

        return MethodEditor.of(constantPoolEditor, method)
    }

    companion object {
        fun of(classFile: ClassFile): ClassEditor {
            return ClassEditor(classFile)
        }
    }
}