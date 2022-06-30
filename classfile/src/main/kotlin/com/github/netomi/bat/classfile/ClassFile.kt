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
import com.github.netomi.bat.classfile.visitor.ClassFileVisitor
import com.github.netomi.bat.classfile.visitor.ConstantPoolVisitor
import com.github.netomi.bat.classfile.visitor.MemberVisitor
import com.github.netomi.bat.util.Classes
import java.io.DataInput
import java.io.IOException

/**
 * https://docs.oracle.com/javase/specs/jvms/se13/html/jvms-4.html#jvms-4.1
 */
class ClassFile internal constructor() {
    var minorVersion = 0
        private set
    var majorVersion = 0
        private set
    var accessFlags: AccessFlags = AccessFlags(0, AccessFlagTarget.CLASS)
        private set
    var thisClassIndex = 0
        private set
    var superClassIndex = 0
        private set
    val constantPool: ConstantPool = ConstantPool()
    private val interfaces = mutableListOf<Int>()
    private val fields     = mutableListOf<Field>()
    private val methods    = mutableListOf<Method>()
    private val attributes = mutableListOf<Attribute>()

    val className: String
        get() = constantPool.getClassName(thisClassIndex)

    val externalClassName: String
        get() = Classes.externalClassNameFromInternalName(className)

    val superClassName: String
        get() = constantPool.getClassName(superClassIndex)

    fun interfaces(): Collection<String> {
        return if (interfaces.isEmpty()) {
            emptyList()
        } else {
            interfaces.map { constantPool.getClassName(it) }
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

    @Throws(IOException::class)
    private fun read(input: DataInput) {
        val magic = input.readInt()
        require(magic == MAGIC) { "invalid magic bytes when trying to read a class file." }

        minorVersion = input.readUnsignedShort()
        majorVersion = input.readUnsignedShort()
        constantPool.read(input)
        accessFlags = AccessFlags(input.readUnsignedShort(), AccessFlagTarget.CLASS)
        thisClassIndex = input.readUnsignedShort()
        superClassIndex = input.readUnsignedShort()
        val interfacesCount = input.readUnsignedShort()
        for (i in 0 until interfacesCount) {
            val idx = input.readUnsignedShort()
            interfaces.add(idx)
        }

        val fieldCount = input.readUnsignedShort()
        for (i in 0 until fieldCount) {
            fields.add(Field.readField(input, constantPool))
        }

        val methodCount = input.readUnsignedShort()
        for (i in 0 until methodCount) {
            methods.add(Method.readMethod(input, constantPool))
        }

        val attributeCount = input.readUnsignedShort()
        for (i in 0 until attributeCount) {
            attributes.add(Attribute.readAttribute(input, constantPool))
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

    companion object {
        @Throws(IOException::class)
        fun readClassFile(input: DataInput): ClassFile {
            val classFile = ClassFile()
            classFile.read(input)
            return classFile
        }
    }
}

