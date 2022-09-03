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

import com.github.netomi.bat.classfile.attribute.AttachedToClass
import com.github.netomi.bat.classfile.attribute.Attribute
import com.github.netomi.bat.classfile.constant.*
import com.github.netomi.bat.classfile.constant.ConstantPool
import com.github.netomi.bat.classfile.attribute.visitor.ClassAttributeVisitor
import com.github.netomi.bat.classfile.visitor.ClassFileVisitor
import com.github.netomi.bat.classfile.constant.visitor.ConstantPoolVisitor
import com.github.netomi.bat.classfile.visitor.MemberVisitor
import com.github.netomi.bat.util.JvmClassName
import com.github.netomi.bat.util.JvmType
import com.github.netomi.bat.util.asJvmType
import com.github.netomi.bat.util.mutableListOfCapacity
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

    private var _interfaces = mutableListOfCapacity<Int>(0)
    private var _fields     = mutableListOfCapacity<Field>(0)
    private var _methods    = mutableListOfCapacity<Method>(0)
    private var _attributes = mutableListOfCapacity<Attribute>(0)

    val className: JvmClassName
        get() = getClassName(thisClassIndex)

    val superClassName: JvmClassName
        get() = getClassName(superClassIndex)

    val interfaces: List<JvmClassName>
        get() {
            return if (_interfaces.isEmpty()) {
                emptyList()
            } else {
                _interfaces.map { getClassName(it) }
            }
        }

    val fields: List<Field>
        get() = _fields

    val methods: List<Method>
        get() = _methods

    val attributes: List<Attribute>
        get() = _attributes

    // helper methods to access constant pool entries

    fun getConstant(constantIndex: Int): Constant {
        return constantPool[constantIndex]
    }

    fun getFieldref(constantIndex: Int): FieldrefConstant {
        return (constantPool[constantIndex] as FieldrefConstant)
    }

    fun getMethodref(constantIndex: Int): MethodrefConstant {
        return (constantPool[constantIndex] as MethodrefConstant)
    }

    fun getInterfaceMethodref(constantIndex: Int): InterfaceMethodrefConstant {
        return (constantPool[constantIndex] as InterfaceMethodrefConstant)
    }

    fun getInteger(constantIndex: Int): Int {
        return (constantPool[constantIndex] as IntegerConstant).value
    }

    fun getBoolean(constantIndex: Int): Boolean {
        return (constantPool[constantIndex] as IntegerConstant).value == 1
    }

    fun getString(stringIndex: Int): String {
        return (constantPool[stringIndex] as Utf8Constant).value
    }

    fun getClassName(classIndex: Int): JvmClassName {
        return (constantPool[classIndex] as ClassConstant).getClassName(this)
    }

    fun getType(typeIndex: Int): JvmType {
        return getString(typeIndex).asJvmType()
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
        _interfaces = mutableListOfCapacity(interfacesCount)
        for (i in 0 until interfacesCount) {
            val idx = input.readUnsignedShort()
            _interfaces.add(idx)
        }

        val fieldCount = input.readUnsignedShort()
        _fields = mutableListOfCapacity(fieldCount)
        for (i in 0 until fieldCount) {
            _fields.add(Field.readField(input, this))
        }

        val methodCount = input.readUnsignedShort()
        _methods = mutableListOfCapacity(methodCount)
        for (i in 0 until methodCount) {
            _methods.add(Method.readMethod(input, this))
        }

        val attributeCount = input.readUnsignedShort()
        _attributes = mutableListOfCapacity(attributeCount)
        for (i in 0 until attributeCount) {
            _attributes.add(Attribute.readAttribute(input, this))
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
        for (attribute in attributes.filterIsInstance(AttachedToClass::class.java)) {
            attribute.accept(this, visitor)
        }
    }

    companion object {
        internal fun empty(): ClassFile {
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

