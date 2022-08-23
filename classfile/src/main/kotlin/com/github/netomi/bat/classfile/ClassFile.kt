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

import com.github.netomi.bat.classfile.attribute.Attribute
import com.github.netomi.bat.classfile.attribute.visitor.AttributeVisitor
import com.github.netomi.bat.classfile.constant.*
import com.github.netomi.bat.classfile.constant.ConstantPool
import com.github.netomi.bat.classfile.attribute.visitor.ClassAttributeVisitor
import com.github.netomi.bat.classfile.attribute.visitor.classAttributes
import com.github.netomi.bat.classfile.visitor.ClassFileVisitor
import com.github.netomi.bat.classfile.constant.visitor.ConstantPoolVisitor
import com.github.netomi.bat.classfile.visitor.MemberVisitor
import com.github.netomi.bat.util.asInternalClassName
import java.io.DataInput
import java.io.IOException
import java.util.*

/**
 * https://docs.oracle.com/javase/specs/jvms/se13/html/jvms-4.html#jvms-4.1
 */
class ClassFile private constructor() {
    var minorVersion = 0
        private set

    var majorVersion = 0
        private set

    var accessFlags: Int = 0
        private set

    val visibility: Visibility
        get() = Visibility.of(accessFlags)

    val modifiers: EnumSet<AccessFlag>
        get() = accessFlagModifiers(accessFlags, AccessFlagTarget.CLASS)

    var thisClassIndex = -1
        private set

    var superClassIndex = -1
        private set

    private val constantPool: ConstantPool = ConstantPool.empty()

    private val interfaces = mutableListOf<Int>()
    private val fields     = mutableListOf<Field>()
    private val methods    = mutableListOf<Method>()
    private val attributes = mutableListOf<Attribute>()

    val className: String
        get() = getClassName(thisClassIndex)

    val externalClassName: String
        get() = className.asInternalClassName().toExternalClassName()

    val superClassName: String
        get() = getClassName(superClassIndex)

    fun interfaces(): Collection<String> {
        return if (interfaces.isEmpty()) {
            emptyList()
        } else {
            interfaces.map { getClassName(it) }
        }
    }

    fun fields(): Collection<Field> {
        return fields
    }

    fun methods(): Collection<Method> {
        return methods
    }

    fun attributes(): Collection<Attribute> {
        return attributes
    }

    // helper methods to access constant pool entries

    internal fun getConstant(constantIndex: Int): Constant {
        return constantPool[constantIndex]
    }

    fun getInteger(constantIndex: Int): Int {
        return (constantPool[constantIndex] as IntegerConstant).value
    }

    fun getString(constantIndex: Int): String {
        return (constantPool[constantIndex] as Utf8Constant).value
    }

    fun getClassName(classIndex: Int): String {
        return (constantPool[classIndex] as ClassConstant).getClassName(this)
    }

    fun getNameAndType(nameAndTypeIndex: Int): NameAndTypeConstant {
        return (constantPool[nameAndTypeIndex] as NameAndTypeConstant)
    }

    @Throws(IOException::class)
    private fun read(input: DataInput) {
        val magic = input.readInt()
        require(magic == MAGIC) { "invalid magic bytes when trying to read a class file." }

        minorVersion = input.readUnsignedShort()
        majorVersion = input.readUnsignedShort()
        constantPool.read(input)
        accessFlags     = input.readUnsignedShort()
        thisClassIndex  = input.readUnsignedShort()
        superClassIndex = input.readUnsignedShort()

        val interfacesCount = input.readUnsignedShort()
        for (i in 0 until interfacesCount) {
            val idx = input.readUnsignedShort()
            interfaces.add(idx)
        }

        val fieldCount = input.readUnsignedShort()
        for (i in 0 until fieldCount) {
            fields.add(Field.readField(input, this))
        }

        val methodCount = input.readUnsignedShort()
        for (i in 0 until methodCount) {
            methods.add(Method.readMethod(input, this))
        }

        val attributeCount = input.readUnsignedShort()
        for (i in 0 until attributeCount) {
            attributes.add(Attribute.readAttribute(input, this))
        }
    }

    fun accept(visitor: ClassFileVisitor) {
        visitor.visitClassFile(this)
    }

    fun constantPoolAccept(visitor: ConstantPoolVisitor) {
        constantPool.accept(this, visitor)
    }

    fun membersAccept(visitor: MemberVisitor) {
        fieldsAccept(visitor)
        membersAccept(visitor)
    }

    fun fieldsAccept(visitor: MemberVisitor) {
        fields.forEachIndexed { index, field -> visitor.visitField(this, index, field) }
    }

    fun methodsAccept(visitor: MemberVisitor) {
        methods.forEachIndexed { index, method -> visitor.visitMethod(this, index, method) }
    }

    fun attributesAccept(visitor: ClassAttributeVisitor) {
        val adapter = if (visitor is AttributeVisitor) visitor else classAttributes(visitor)
        for (attribute in attributes) {
            attribute.accept(this, adapter)
        }
    }

    companion object {
        fun empty(): ClassFile {
            return ClassFile()
        }

        @Throws(IOException::class)
        fun readClassFile(input: DataInput): ClassFile {
            val classFile = ClassFile()
            classFile.read(input)
            return classFile
        }
    }
}

