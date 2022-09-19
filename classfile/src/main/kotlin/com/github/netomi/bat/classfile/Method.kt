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
import com.github.netomi.bat.classfile.attribute.visitor.*
import com.github.netomi.bat.classfile.io.ClassDataInput
import com.github.netomi.bat.classfile.visitor.MemberVisitor
import com.github.netomi.bat.classfile.visitor.MethodVisitor
import com.github.netomi.bat.util.JvmClassName
import com.github.netomi.bat.util.getArgumentSize
import com.github.netomi.bat.util.parseDescriptorToJvmTypes
import com.github.netomi.bat.util.toHexString
import java.io.IOException

/**
 * A class representing a method in a class file.
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se13/html/jvms-4.html#jvms-4.6">Method_info structure</a>
 */
class Method private constructor(nameIndex:       Int = -1,
                                 accessFlags:     Int =  0,
                                 descriptorIndex: Int = -1): Member(nameIndex, accessFlags, descriptorIndex) {

    override val accessFlagTarget: AccessFlagTarget
        get() = AccessFlagTarget.METHOD

    var modifiers: Set<MethodModifier> = MethodModifier.setOf(accessFlags)
        private set

    override fun updateModifiers(accessFlags: Int) {
        modifiers = MethodModifier.setOf(accessFlags)
    }

    override fun addAttribute(attribute: Attribute) {
        require(attribute is AttachedToMethod) { "trying to add an attribute of type '${attribute.type}' to a method"}
        attributeMap.addAttribute(attribute)
    }

    override val isStatic: Boolean
        get() = modifiers.contains(MethodModifier.STATIC)

    val hasCode: Boolean
        get() = attributeMap.get<CodeAttribute>(AttributeType.CODE) != null

    fun getExceptionClassNames(classFile: ClassFile): List<JvmClassName> {
        return attributeMap.get<ExceptionsAttribute>(AttributeType.EXCEPTIONS)?.getExceptionClassNames(classFile) ?: emptyList()
    }

    fun getArgumentSize(classFile: ClassFile): Int {
        var argumentSize = if (isStatic) 0 else 1
        val (parameters, _) = parseDescriptorToJvmTypes(getDescriptor(classFile))
        argumentSize += parameters.getArgumentSize()
        return argumentSize
    }

    fun getArgumentCount(classFile: ClassFile): Int {
        var argumentCount = if (isStatic) 0 else 1
        val (parameters, _) = parseDescriptorToJvmTypes(getDescriptor(classFile))
        argumentCount += parameters.size
        return argumentCount
    }

    fun accept(classFile: ClassFile, index: Int, visitor: MethodVisitor) {
        visitor.visitMethod(classFile, index, this)
    }

    override fun accept(classFile: ClassFile, index: Int, visitor: MemberVisitor) {
        visitor.visitMethod(classFile, index, this)
    }

    fun attributesAccept(classFile: ClassFile, visitor: MethodAttributeVisitor) {
        for (attribute in attributeMap.filterIsInstance(AttachedToMethod::class.java)) {
            attribute.accept(classFile, this, visitor)
        }
    }

    override fun attributesAccept(classFile: ClassFile, visitor: MemberAttributeVisitor) {
        attributesAccept(classFile, visitor as MethodAttributeVisitor)
    }

    override fun toString(): String {
        return "Method(nameIndex=%d,descriptorIndex=%d,accessFlags=%s)".format(nameIndex, descriptorIndex, toHexString(accessFlags, 4))
    }

    companion object {
        fun of(nameIndex: Int, accessFlags: Int, descriptorIndex: Int): Method {
            require(nameIndex >= 1)       { "nameIndex must be a positive number" }
            require(accessFlags >= 0)     { "accessFlags mut not be negative" }
            require(descriptorIndex >= 1) { "descriptorIndex must be a positive number" }
            return Method(nameIndex, accessFlags, descriptorIndex)
        }

        @Throws(IOException::class)
        internal fun readMethod(input: ClassDataInput): Method {
            val method = Method()
            method.read(input)
            return method
        }
    }
}