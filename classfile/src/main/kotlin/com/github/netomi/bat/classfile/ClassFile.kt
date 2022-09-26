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
import com.github.netomi.bat.classfile.constant.editor.ConstantPoolEditor
import com.github.netomi.bat.classfile.visitor.ClassFileVisitor
import com.github.netomi.bat.classfile.constant.visitor.ConstantVisitor
import com.github.netomi.bat.classfile.constant.visitor.ListElementAccessor
import com.github.netomi.bat.classfile.constant.visitor.PropertyAccessor
import com.github.netomi.bat.classfile.constant.visitor.ReferencedConstantVisitor
import com.github.netomi.bat.classfile.io.ClassDataInput
import com.github.netomi.bat.classfile.io.ClassDataOutput
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
open class ClassFile protected constructor(version:     Version = Version.JAVA_8,
                                           accessFlags: Int     = 0) {

    var majorVersion = version.majorVersion
        internal set

    var minorVersion = version.minorVersion
        internal set

    var accessFlags: Int = accessFlags
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

    internal var constantPool = ConstantPool.empty()

    val isInterface: Boolean
        get() = modifiers.contains(ClassModifier.INTERFACE)

    val isModule: Boolean
        get() = modifiers.contains(ClassModifier.MODULE)

    val constantPoolSize: Int
        get() = constantPool.size

    val className: JvmClassName
        get() = getClassName(thisClassIndex)

    val superClassName: JvmClassName?
        get() = if (superClassIndex > 0) getClassName(superClassIndex) else null

    // interfaces

    private var _interfaces: MutableList<Int> = mutableListOfCapacity(0)

    val interfaceCount: Int
        get() = _interfaces.size

    val interfaces: List<JvmClassName>
        get() {
            return if (_interfaces.isEmpty()) {
                emptyList()
            } else {
                _interfaces.map { getClassName(it) }
            }
        }

    // fields

    private var _fields: MutableList<Field>  = mutableListOfCapacity(0)

    val fields: List<Field>
        get() = _fields

    internal fun addField(field: Field) {
        _fields.add(field)
    }

    // methods

    private var _methods: MutableList<Method> = mutableListOfCapacity(0)

    val methods: List<Method>
        get() = _methods

    internal fun addMethod(method: Method) {
        _methods.add(method)
    }

    // attributes

    internal var attributeMap = AttributeMap.empty()

    val attributes: Sequence<Attribute>
        get() = attributeMap

    internal fun addAttribute(attribute: Attribute) {
        require(attribute is AttachedToClass) { "trying to add an attribute of type '${attribute.type}' to a class"}
        attributeMap.addAttribute(attribute)
    }

    // convenience methods to access certain attributes

    val isDeprecated: Boolean
        get() = attributeMap.get<DeprecatedAttribute>(AttributeType.DEPRECATED) != null

    val isSynthetic: Boolean
        get() = attributeMap.get<SyntheticAttribute>(AttributeType.SYNTHETIC) != null

    val sourceFile: String?
        get() = attributeMap.get<SourceFileAttribute>(AttributeType.SOURCE_FILE)?.getSourceFile(this)

    val signature: String?
        get() = attributeMap.get<SignatureAttribute>(AttributeType.SIGNATURE)?.getSignature(this)

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

    fun getInvokeDynamic(constantIndex: Int): InvokeDynamicConstant {
        return (constantPool[constantIndex] as InvokeDynamicConstant)
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
        for (attribute in attributeMap.filterIsInstance(AttachedToClass::class.java)) {
            attribute.accept(this, visitor)
        }
    }

    fun referencedConstantsAccept(visitConstantPool: Boolean = true, visitor: ReferencedConstantVisitor) {
        visitor.visitClassConstant(this, this, PropertyAccessor(::thisClassIndex))
        if (superClassIndex > 0) {
            visitor.visitClassConstant(this, this, PropertyAccessor(::superClassIndex))
        }

        if (visitConstantPool) {
            constantPool.referencedConstantsAccept(this, visitor)
        }

        for ((index, _) in _interfaces.withIndex()) {
            visitor.visitClassConstant(this, this, ListElementAccessor(_interfaces, index))
        }

        for (field in fields) {
            field.referencedConstantsAccept(this, visitor)
        }

        for (method in methods) {
            method.referencedConstantsAccept(this, visitor)
        }

        for (attribute in attributes) {
            attribute.referencedConstantsAccept(this, visitor)
        }
    }

    internal fun read(input: ClassDataInput) {
        val magic = input.readInt()
        require(magic == MAGIC) { "invalid magic bytes when trying to read a class file." }

        minorVersion    = input.readUnsignedShort()
        majorVersion    = input.readUnsignedShort()
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
            addField(Field.readField(input))
        }

        val methodCount = input.readUnsignedShort()
        _methods = mutableListOfCapacity(methodCount)
        for (i in 0 until methodCount) {
            _methods.add(Method.readMethod(input))
        }

        attributeMap = input.readAttributes()
    }

    internal fun write(output: ClassDataOutput) {
        output.writeInt(MAGIC)

        output.writeShort(minorVersion)
        output.writeShort(majorVersion)
        constantPool.write(output)
        output.writeShort(accessFlags)
        output.writeShort(thisClassIndex)
        output.writeShort(superClassIndex)

        output.writeShort(_interfaces.size)
        for (index in _interfaces) {
            output.writeShort(index)
        }

        output.writeShort(_fields.size)
        for (field in _fields) {
            field.write(output)
        }

        output.writeShort(_methods.size)
        for (method in _methods) {
            method.write(output)
        }

        attributeMap.write(output)
    }

    override fun toString(): String {
        return if (constantPoolSize == 1) {
            "ClassFile(empty)"
        } else {
            "ClassFile(name=$className)"
        }
    }

    companion object {
        fun empty(): ClassFile {
            return ClassFile()
        }

        fun of(className: String, accessFlags: Int, superClassName: String? = null, version: Version = Version.JAVA_8): ClassFile {
            val classFile            = ClassFile(version, accessFlags)
            val constantPoolEditor   = ConstantPoolEditor.of(classFile)
            classFile.thisClassIndex = constantPoolEditor.addOrGetClassConstantIndex(className)

            if (superClassName != null) {
                classFile.superClassIndex = constantPoolEditor.addOrGetClassConstantIndex(superClassName)
            }

            return classFile
        }

        fun read(`is`: InputStream, skipAttributes: Boolean = false): ClassFile {
            val classFile = empty()
            val reader    = ClassFileReader(`is`, skipAttributes)
            reader.visitClassFile(classFile)
            return classFile
        }
    }
}

