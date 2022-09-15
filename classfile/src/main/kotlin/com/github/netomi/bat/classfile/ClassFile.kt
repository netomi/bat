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
import com.github.netomi.bat.classfile.constant.*
import com.github.netomi.bat.classfile.constant.ConstantPool
import com.github.netomi.bat.classfile.attribute.visitor.ClassAttributeVisitor
import com.github.netomi.bat.classfile.visitor.ClassFileVisitor
import com.github.netomi.bat.classfile.constant.visitor.ConstantVisitor
import com.github.netomi.bat.classfile.constant.visitor.PropertyAccessor
import com.github.netomi.bat.classfile.constant.visitor.ReferencedConstantVisitor
import com.github.netomi.bat.classfile.io.ClassFileReader
import com.github.netomi.bat.classfile.visitor.FieldVisitor
import com.github.netomi.bat.classfile.visitor.MemberVisitor
import com.github.netomi.bat.classfile.visitor.MethodVisitor
import com.github.netomi.bat.util.JvmClassName
import com.github.netomi.bat.util.JvmType
import com.github.netomi.bat.util.asJvmType
import com.github.netomi.bat.util.mutableListOfCapacity
import java.io.InputStream

/**
 * https://docs.oracle.com/javase/specs/jvms/se13/html/jvms-4.html#jvms-4.1
 */
class ClassFile private constructor() {
    var minorVersion = 0
        internal set

    var majorVersion = 0
        internal set

    var accessFlags: Int = 0
        internal set(value) {
            field = value
            visibility = Visibility.of(value)
            modifiers  = ClassModifier.setOf(value)
        }

    val accessFlagsAsSet: Set<AccessFlag>
        get() = accessFlagsToSet(accessFlags, AccessFlagTarget.CLASS)

    var visibility: Visibility = Visibility.of(accessFlags)
        private set

    var modifiers: Set<ClassModifier> = ClassModifier.setOf(accessFlags)
        private set

    var thisClassIndex = -1
        internal set

    var superClassIndex = -1
        internal set

    val isInterface: Boolean
        get() = modifiers.contains(ClassModifier.INTERFACE)

    val isModule: Boolean
        get() = modifiers.contains(ClassModifier.MODULE)

    internal val constantPool: ConstantPool = ConstantPool.empty()

    val constantPoolSize: Int
        get() = constantPool.size

    internal var _interfaces = mutableListOfCapacity<Int>(0)
    internal var _fields     = mutableListOfCapacity<Field>(0)
    internal var _methods    = mutableListOfCapacity<Method>(0)
    internal var _attributes = AttributeMap.empty()

    val className: JvmClassName
        get() = getClassName(thisClassIndex)

    val superClassName: JvmClassName?
        get() = if (superClassIndex > 0) getClassName(superClassIndex) else null

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

    val attributes: Sequence<Attribute>
        get() = _attributes

    // convenience methods to access certain attributes

    val isDeprecated: Boolean
        get() = _attributes.get<DeprecatedAttribute>(AttributeType.DEPRECATED) != null

    val isSynthetic: Boolean
        get() = _attributes.get<SyntheticAttribute>(AttributeType.SYNTHETIC) != null

    val sourceFile: String?
        get() = _attributes.get<SourceFileAttribute>(AttributeType.SOURCE_FILE)?.getSourceFile(this)

    val signature: String?
        get() = _attributes.get<SignatureAttribute>(AttributeType.SIGNATURE)?.getSignature(this)

    // helper methods to access constant pool entries

    internal fun getConstant(constantIndex: Int): Constant {
        return constantPool[constantIndex]
    }

    fun getClass(constantIndex: Int): ClassConstant {
        return (constantPool[constantIndex] as ClassConstant)
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

    fun getDynamic(constantIndex: Int): DynamicConstant {
        return (constantPool[constantIndex] as DynamicConstant)
    }

    fun getModule(constantIndex: Int): ModuleConstant {
        return (constantPool[constantIndex] as ModuleConstant)
    }

    fun getPackage(constantIndex: Int): PackageConstant {
        return (constantPool[constantIndex] as PackageConstant)
    }

    fun getInteger(constantIndex: Int): Int {
        return (constantPool[constantIndex] as IntegerConstant).value
    }

    fun getBoolean(constantIndex: Int): Boolean {
        return (constantPool[constantIndex] as IntegerConstant).value == 1
    }

    fun getUtf8Constant(constantIndex: Int): Utf8Constant {
        return (constantPool[constantIndex] as Utf8Constant)
    }

    fun getString(stringIndex: Int): String {
        return (constantPool[stringIndex] as Utf8Constant).value
    }

    fun getStringOrNull(stringIndex: Int): String? {
        return if (stringIndex > 0) getString(stringIndex) else null
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

    fun accept(visitor: ClassFileVisitor) {
        visitor.visitClassFile(this)
    }

    fun constantsAccept(visitor: ConstantVisitor) {
        constantPool.accept(this, visitor)
    }

    fun constantAccept(constantIndex: Int, visitor: ConstantVisitor) {
        constantPool.constantAccept(this, constantIndex, visitor)
    }

    fun membersAccept(visitor: MemberVisitor) {
        fieldsAccept(visitor)
        membersAccept(visitor)
    }

    fun fieldsAccept(visitor: FieldVisitor) {
        fields.forEachIndexed { index, field -> visitor.visitField(this, index, field) }
    }

    fun methodsAccept(visitor: MethodVisitor) {
        methods.forEachIndexed { index, method -> visitor.visitMethod(this, index, method) }
    }

    fun attributesAccept(visitor: ClassAttributeVisitor) {
        for (attribute in _attributes.filterIsInstance(AttachedToClass::class.java)) {
            attribute.accept(this, visitor)
        }
    }

    fun referencedConstantVisitor(visitor: ReferencedConstantVisitor) {
        visitor.visitClassConstant(this, this, PropertyAccessor({ thisClassIndex }, { thisClassIndex = it }))
        if (superClassIndex > 0) {
            visitor.visitClassConstant(this, this, PropertyAccessor({ superClassIndex }, { superClassIndex = it }))
        }

        constantPool.referencedConstantVisitor(this, visitor)

        for (field in fields) {
            field.referencedConstantVisitor(this, visitor)
        }

        for (method in methods) {
            method.referencedConstantVisitor(this, visitor)
        }

        for (attribute in attributes) {
            attribute.referencedConstantVisitor(this, visitor)
        }
    }

    override fun toString(): String {
        return "ClassFile(name=$className)"
    }

    companion object {
        fun empty(): ClassFile {
            return ClassFile()
        }

        fun read(`is`: InputStream, skipAttributes: Boolean = false): ClassFile {
            val classFile = empty()
            val reader    = ClassFileReader(`is`, skipAttributes)
            reader.visitClassFile(classFile)
            return classFile
        }
    }
}

