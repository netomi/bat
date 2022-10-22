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

import com.github.netomi.bat.classfile.attribute.*
import com.github.netomi.bat.classfile.attribute.AttributeType
import com.github.netomi.bat.classfile.attribute.visitor.MemberAttributeVisitor
import com.github.netomi.bat.classfile.constant.visitor.ConstantVisitor
import com.github.netomi.bat.classfile.constant.visitor.PropertyAccessor
import com.github.netomi.bat.classfile.constant.visitor.ReferencedConstantVisitor
import com.github.netomi.bat.classfile.io.ClassDataInput
import com.github.netomi.bat.classfile.io.ClassDataOutput
import com.github.netomi.bat.classfile.visitor.MemberVisitor
import java.io.IOException
import java.util.*

abstract class Member protected constructor(nameIndex:                 Int          = -1,
                                            accessFlags:               Int          =  0,
                                            descriptorIndex:           Int          = -1,
                                            internal var attributeMap: AttributeMap = AttributeMap.empty()) {

    var accessFlags: Int = accessFlags
        private set(value) {
            field = value
            visibility = Visibility.of(value)
            updateModifiers(value)
        }

    val accessFlagsAsSet: Set<AccessFlag>
        get() = accessFlagsToSet(accessFlags, accessFlagTarget)

    var nameIndex: Int = nameIndex
        protected set

    var descriptorIndex: Int = descriptorIndex
        protected set

    var visibility: Visibility = Visibility.of(accessFlags)
        private set

    val attributes: Sequence<Attribute>
        get() = attributeMap

    protected abstract fun updateModifiers(accessFlags: Int)
    protected abstract val accessFlagTarget: AccessFlagTarget

    abstract val isStatic: Boolean

    val isPrivate: Boolean
        get() = visibility == Visibility.PRIVATE

    val isDeprecated: Boolean
        get() = attributeMap.get<DeprecatedAttribute>(AttributeType.DEPRECATED) != null

    // TODO: also check the modifier
    val isSynthetic: Boolean
        get() = attributeMap.get<SyntheticAttribute>(AttributeType.SYNTHETIC) != null

    fun getName(classFile: ClassFile): String {
        return classFile.getString(nameIndex)
    }

    fun getDescriptor(classFile: ClassFile): String {
        return classFile.getString(descriptorIndex)
    }

    fun getSignature(classFile: ClassFile): String? {
        return attributeMap.get<SignatureAttribute>(AttributeType.SIGNATURE)?.getSignature(classFile)
    }

    internal inline fun <reified T: Attribute> getAttribute(type: AttributeType): T? {
        return attributeMap[type]
    }

    internal abstract fun addAttribute(attribute: Attribute)

    abstract fun accept(classFile: ClassFile, index: Int, visitor: MemberVisitor)
    abstract fun attributesAccept(classFile: ClassFile, visitor: MemberAttributeVisitor)

    fun descriptorConstantAccept(classFile: ClassFile, visitor: ConstantVisitor) {
        classFile.constantAccept(descriptorIndex, visitor)
    }

    @Throws(IOException::class)
    fun read(input: ClassDataInput) {
        accessFlags     = input.readUnsignedShort()
        nameIndex       = input.readUnsignedShort()
        descriptorIndex = input.readUnsignedShort()
        attributeMap    = input.readAttributes()
    }

    fun write(output: ClassDataOutput) {
        output.writeShort(accessFlags)
        output.writeShort(nameIndex)
        output.writeShort(descriptorIndex)
        attributeMap.write(output)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Member) return false

        return accessFlags     == other.accessFlags     &&
               nameIndex       == other.nameIndex       &&
               descriptorIndex == other.descriptorIndex &&
               attributeMap    == other.attributeMap
    }

    override fun hashCode(): Int {
        return Objects.hash(accessFlags, nameIndex, descriptorIndex, attributeMap)
    }

    fun referencedConstantsAccept(classFile: ClassFile, visitor: ReferencedConstantVisitor) {
        visitor.visitUtf8Constant(classFile, this, PropertyAccessor(::nameIndex))
        visitor.visitUtf8Constant(classFile, this, PropertyAccessor(::descriptorIndex))

        for (attribute in attributeMap) {
            attribute.referencedConstantsAccept(classFile, visitor)
        }
    }
}