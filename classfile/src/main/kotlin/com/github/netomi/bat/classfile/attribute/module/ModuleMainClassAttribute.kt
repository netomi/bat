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

package com.github.netomi.bat.classfile.attribute.module

import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.attribute.AttachedToClass
import com.github.netomi.bat.classfile.attribute.Attribute
import com.github.netomi.bat.classfile.attribute.AttributeType
import com.github.netomi.bat.classfile.attribute.visitor.ClassAttributeVisitor
import com.github.netomi.bat.classfile.constant.visitor.PropertyAccessor
import com.github.netomi.bat.classfile.constant.visitor.ReferencedConstantVisitor
import com.github.netomi.bat.classfile.io.ClassDataInput
import com.github.netomi.bat.classfile.io.ClassDataOutput
import com.github.netomi.bat.util.JvmClassName

/**
 * A class representing a ModuleMainClass attribute in a class file.
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se18/html/jvms-4.html#jvms-4.7.27">ModuleMainClass Attribute</a>
 */
data class ModuleMainClassAttribute
    private constructor(override var attributeNameIndex: Int,
                         private var _mainClassIndex:    Int = -1): Attribute(attributeNameIndex), AttachedToClass {

    override val type: AttributeType
        get() = AttributeType.MODULE_MAIN_CLASS

    override val dataSize: Int
        get() = 2

    val mainClassIndex: Int
        get() = _mainClassIndex

    fun getMainClass(classFile: ClassFile): JvmClassName {
        return classFile.getClassName(mainClassIndex)
    }

    override fun readAttributeData(input: ClassDataInput, length: Int) {
        _mainClassIndex = input.readUnsignedShort()
    }

    override fun writeAttributeData(output: ClassDataOutput) {
        output.writeShort(mainClassIndex)
    }

    override fun accept(classFile: ClassFile, visitor: ClassAttributeVisitor) {
        visitor.visitModuleMainClass(classFile, this)
    }

    override fun referencedConstantVisitor(classFile: ClassFile, visitor: ReferencedConstantVisitor) {
        super.referencedConstantVisitor(classFile, visitor)
        visitor.visitClassConstant(classFile, this, PropertyAccessor({ _mainClassIndex }, { _mainClassIndex = it }))
    }

    companion object {
        internal fun empty(attributeNameIndex: Int): ModuleMainClassAttribute {
            return ModuleMainClassAttribute(attributeNameIndex)
        }
    }
}