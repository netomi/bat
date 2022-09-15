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

/**
 * A class representing a ModuleTarget attribute in a class file.
 */
data class ModuleTargetAttribute
    private constructor(override var attributeNameIndex: Int,
                         private var _platformIndex:     Int = -1): Attribute(attributeNameIndex), AttachedToClass {

    override val type: AttributeType
        get() = AttributeType.MODULE_MAIN_CLASS

    override val dataSize: Int
        get() = 2

    val platformIndex: Int
        get() = _platformIndex

    fun getPlatform(classFile: ClassFile): String {
        return classFile.getString(platformIndex)
    }

    override fun readAttributeData(input: ClassDataInput, length: Int) {
        _platformIndex = input.readUnsignedShort()
    }

    override fun writeAttributeData(output: ClassDataOutput) {
        output.writeShort(_platformIndex)
    }

    override fun accept(classFile: ClassFile, visitor: ClassAttributeVisitor) {
        visitor.visitModuleTarget(classFile, this)
    }

    override fun referencedConstantsAccept(classFile: ClassFile, visitor: ReferencedConstantVisitor) {
        super.referencedConstantsAccept(classFile, visitor)
        visitor.visitUtf8Constant(classFile, this, PropertyAccessor(::_platformIndex))
    }

    companion object {
        internal fun empty(attributeNameIndex: Int): ModuleTargetAttribute {
            return ModuleTargetAttribute(attributeNameIndex)
        }
    }
}