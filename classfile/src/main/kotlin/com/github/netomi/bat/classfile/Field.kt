/*
 *  Copyright (c) 2020 Thomas Neidhart.
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
package com.github.netomi.bat.classfile

import com.github.netomi.bat.classfile.attribute.AttachedToField
import com.github.netomi.bat.classfile.attribute.Attribute
import com.github.netomi.bat.classfile.attribute.AttributeType
import com.github.netomi.bat.classfile.attribute.ConstantValueAttribute
import com.github.netomi.bat.classfile.attribute.visitor.FieldAttributeVisitor
import com.github.netomi.bat.classfile.attribute.visitor.MemberAttributeVisitor
import com.github.netomi.bat.classfile.constant.visitor.ConstantVisitor
import com.github.netomi.bat.classfile.io.ClassDataInput
import com.github.netomi.bat.classfile.visitor.FieldVisitor
import com.github.netomi.bat.classfile.visitor.MemberVisitor
import com.github.netomi.bat.util.toHexString
import java.io.IOException

/**
 * A class representing a field in a class file.
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se13/html/jvms-4.html#jvms-4.5">Field_info structure</a>
 */
open class Field protected constructor(nameIndex:       Int = -1,
                                       accessFlags:     Int =  0,
                                       descriptorIndex: Int = -1): Member(nameIndex, accessFlags, descriptorIndex) {

    override val accessFlagTarget: AccessFlagTarget
        get() = AccessFlagTarget.FIELD

    var modifiers: Set<FieldModifier> = FieldModifier.setOf(accessFlags)
        private set

    override fun updateModifiers(accessFlags: Int) {
        modifiers = FieldModifier.setOf(accessFlags)
    }

    override fun addAttribute(attribute: Attribute) {
        require(attribute is AttachedToField) { "trying to add an attribute of type '${attribute.type}' to a field"}
        attributeMap.addAttribute(attribute)
    }

    override val isStatic: Boolean
        get() = modifiers.contains(FieldModifier.STATIC)

    fun accept(classFile: ClassFile, index: Int, visitor: FieldVisitor) {
        visitor.visitField(classFile, index, this)
    }

    override fun accept(classFile: ClassFile, index: Int, visitor: MemberVisitor) {
        visitor.visitField(classFile, index, this)
    }

    fun attributesAccept(classFile: ClassFile, visitor: FieldAttributeVisitor) {
        for (attribute in attributeMap.filterIsInstance(AttachedToField::class.java)) {
            attribute.accept(classFile, this, visitor)
        }
    }

    override fun attributesAccept(classFile: ClassFile, visitor: MemberAttributeVisitor) {
        attributesAccept(classFile, visitor as FieldAttributeVisitor)
    }

    fun constantValueAccept(classFile: ClassFile, visitor: ConstantVisitor) {
        attributeMap.get<ConstantValueAttribute>(AttributeType.CONSTANT_VALUE)?.constantValueAccept(classFile, visitor)
    }

    override fun toString(): String {
        return "Field(nameIndex=%d,descriptorIndex=%d,accessFlags=%s)".format(nameIndex, descriptorIndex, toHexString(accessFlags, 4))
    }

    companion object {
        fun of(nameIndex: Int, accessFlags: Int, descriptorIndex: Int): Field {
            require(nameIndex >= 1)       { "nameIndex must be a positive number" }
            require(accessFlags >= 0)     { "accessFlags mut not be negative" }
            require(descriptorIndex >= 1) { "descriptorIndex must be a positive number" }
            return Field(nameIndex, accessFlags, descriptorIndex)
        }

        @Throws(IOException::class)
        internal fun read(input: ClassDataInput): Field {
            val field = Field()
            field.read(input)
            return field
        }
    }
}