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

package com.github.netomi.bat.classfile.attribute

import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.attribute.visitor.ClassAttributeVisitor
import com.github.netomi.bat.util.JvmClassName
import java.io.DataInput
import java.io.DataOutput

/**
 * A class representing a ModuleMainClass attribute in a class file.
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se18/html/jvms-4.html#jvms-4.7.27">ModuleMainClass Attribute</a>
 */
data class ModuleMainClassAttribute
    private constructor(override val attributeNameIndex: Int,
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

    override fun readAttributeData(input: DataInput, classFile: ClassFile) {
        @Suppress("UNUSED_VARIABLE")
        val length = input.readInt()
        _mainClassIndex = input.readUnsignedShort()
    }

    override fun writeAttributeData(output: DataOutput) {
        output.writeInt(dataSize)
        output.writeShort(mainClassIndex)
    }

    override fun accept(classFile: ClassFile, visitor: ClassAttributeVisitor) {
        visitor.visitModuleMainClassAttribute(classFile, this)
    }

    companion object {
        internal fun empty(attributeNameIndex: Int): ModuleMainClassAttribute {
            return ModuleMainClassAttribute(attributeNameIndex)
        }
    }
}