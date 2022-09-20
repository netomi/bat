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
package com.github.netomi.bat.classfile.attribute

import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.Field
import com.github.netomi.bat.classfile.Method
import com.github.netomi.bat.classfile.attribute.annotation.*
import com.github.netomi.bat.classfile.attribute.module.ModuleAttribute
import com.github.netomi.bat.classfile.attribute.module.ModuleMainClassAttribute
import com.github.netomi.bat.classfile.attribute.module.ModulePackagesAttribute
import com.github.netomi.bat.classfile.attribute.module.ModuleHashesAttribute
import com.github.netomi.bat.classfile.attribute.module.ModuleTargetAttribute
import com.github.netomi.bat.classfile.attribute.preverification.StackMapTableAttribute
import com.github.netomi.bat.classfile.attribute.visitor.*
import com.github.netomi.bat.classfile.constant.editor.ConstantPoolEditor
import com.github.netomi.bat.classfile.constant.visitor.PropertyAccessor
import com.github.netomi.bat.classfile.constant.visitor.ReferencedConstantVisitor
import com.github.netomi.bat.classfile.io.ClassDataInput
import com.github.netomi.bat.classfile.io.ClassDataOutput
import com.github.netomi.bat.classfile.io.ClassFileContent
import java.io.IOException

/**
 * Base class for attributes as contained in a class file.
 */
abstract class Attribute protected constructor(protected open var attributeNameIndex: Int): ClassFileContent() {

    abstract val type: AttributeType

    protected abstract val dataSize: Int

    override val contentSize: Int
        get() = 6 + dataSize

    fun getAttributeName(classFile: ClassFile): String {
        return classFile.getString(attributeNameIndex)
    }

    @Throws(IOException::class)
    internal abstract fun readAttributeData(input: ClassDataInput, length: Int)

    @Throws(IOException::class)
    internal abstract fun writeAttributeData(output: ClassDataOutput)

    internal fun read(input: ClassDataInput) {
        val length = input.readInt()
        readAttributeData(input, length)
    }

    @Throws(IOException::class)
    override fun write(output: ClassDataOutput) {
        output.writeShort(attributeNameIndex)
        output.writeInt(dataSize)
        writeAttributeData(output)
    }

    open fun referencedConstantsAccept(classFile: ClassFile, visitor: ReferencedConstantVisitor) {
        visitor.visitUtf8Constant(classFile, this, PropertyAccessor(::attributeNameIndex))
    }

    companion object {
        internal fun skipAttribute(input: ClassDataInput) {
            @Suppress("UNUSED_VARIABLE")
            val attributeNameIndex = input.readUnsignedShort()
            val attributeLength    = input.readInt()
            input.skipBytes(attributeLength)
        }

        internal fun readAttribute(input : ClassDataInput, classFile: ClassFile): Attribute {
            val attributeNameIndex = input.readUnsignedShort()
            val attributeName      = classFile.getString(attributeNameIndex)

            val attribute = AttributeType.of(attributeName).createAttribute(attributeNameIndex)
            attribute.read(input)

            return attribute
        }
    }
}

interface AttachedToClass {
    fun accept(classFile: ClassFile, visitor: ClassAttributeVisitor)
}

interface AttachedToField {
    fun accept(classFile: ClassFile, field: Field, visitor: FieldAttributeVisitor)
}

interface AttachedToMethod {
    fun accept(classFile: ClassFile, method: Method, visitor: MethodAttributeVisitor)
}

interface AttachedToCodeAttribute {
    fun accept(classFile: ClassFile, method: Method, code: CodeAttribute, visitor: CodeAttributeVisitor)
}

interface AttachedToRecordComponent {
    fun accept(classFile: ClassFile, record: RecordAttribute, component: RecordComponent, visitor: RecordComponentAttributeVisitor)
}

/**
 * Known constant types as contained in a java class file.
 */
enum class AttributeType constructor(val attributeName: String, private val supplier: (Int) -> Attribute) {

    // Predefined attributes:
    // https://docs.oracle.com/javase/specs/jvms/se17/html/jvms-4.html#jvms-4.7-300

    CONSTANT_VALUE                         ("ConstantValue",                        { ConstantValueAttribute.empty(it) }),
    CODE                                   ("Code",                                 { CodeAttribute.empty(it) }),
    STACK_MAP_TABLE                        ("StackMapTable",                        { StackMapTableAttribute.empty(it) }),
    EXCEPTIONS                             ("Exceptions",                           { ExceptionsAttribute.empty(it) }),
    INNER_CLASSES                          ("InnerClasses",                         { InnerClassesAttribute.empty(it) }),
    ENCLOSING_METHOD                       ("EnclosingMethod",                      { EnclosingMethodAttribute.empty(it) }),
    SYNTHETIC                              ("Synthetic",                            { SyntheticAttribute.empty(it) }),
    SIGNATURE                              ("Signature",                            { SignatureAttribute.empty(it) }),
    SOURCE_FILE                            ("SourceFile",                           { SourceFileAttribute.empty(it) }),
    SOURCE_DEBUG_EXTENSION                 ("SourceDebugExtension",                 { SourceDebugExtensionAttribute.empty(it) }),
    LINE_NUMBER_TABLE                      ("LineNumberTable",                      { LineNumberTableAttribute.empty(it) }),
    LOCAL_VARIABLE_TABLE                   ("LocalVariableTable",                   { LocalVariableTableAttribute.empty(it) }),
    LOCAL_VARIABLE_TYPE_TABLE              ("LocalVariableTypeTable",               { LocalVariableTypeTableAttribute.empty(it) }),
    DEPRECATED                             ("Deprecated",                           { DeprecatedAttribute.empty(it) }),
    RUNTIME_VISIBLE_ANNOTATIONS            ("RuntimeVisibleAnnotations",            { RuntimeVisibleAnnotationsAttribute.empty(it) }),
    RUNTIME_INVISIBLE_ANNOTATIONS          ("RuntimeInvisibleAnnotations",          { RuntimeInvisibleAnnotationsAttribute.empty(it) }),
    RUNTIME_VISIBLE_PARAMETER_ANNOTATIONS  ("RuntimeVisibleParameterAnnotations",   { RuntimeVisibleParameterAnnotationsAttribute.empty(it) }),
    RUNTIME_INVISIBLE_PARAMETER_ANNOTATIONS("RuntimeInvisibleParameterAnnotations", { RuntimeInvisibleParameterAnnotationsAttribute.empty(it) }),
    RUNTIME_VISIBLE_TYPE_ANNOTATIONS       ("RuntimeVisibleTypeAnnotations",        { RuntimeVisibleTypeAnnotationsAttribute.empty(it) }),
    RUNTIME_INVISIBLE_TYPE_ANNOTATIONS     ("RuntimeInvisibleTypeAnnotations",      { RuntimeInvisibleTypeAnnotationsAttribute.empty(it) }),
    ANNOTATION_DEFAULT                     ("AnnotationDefault",                    { AnnotationDefaultAttribute.empty(it) }),
    BOOTSTRAP_METHOD                       ("BootstrapMethods",                     { BootstrapMethodsAttribute.empty(it) }),
    METHOD_PARAMETERS                      ("MethodParameters",                     { MethodParametersAttribute.empty(it) }),
    MODULE                                 ("Module",                               { ModuleAttribute.empty(it) }),
    MODULE_PACKAGES                        ("ModulePackages",                       { ModulePackagesAttribute.empty(it) }),
    MODULE_MAIN_CLASS                      ("ModuleMainClass",                      { ModuleMainClassAttribute.empty(it) }),
    MODULE_HASHES                          ("ModuleHashes",                         { ModuleHashesAttribute.empty(it) }),
    MODULE_TARGET                          ("ModuleTarget",                         { ModuleTargetAttribute.empty(it) }),
    NEST_HOST                              ("NestHost",                             { NestHostAttribute.empty(it) }),
    NEST_MEMBERS                           ("NestMembers",                          { NestMembersAttribute.empty(it) }),
    RECORD                                 ("Record",                               { RecordAttribute.empty(it) }),
    PERMITTED_SUBCLASSES                   ("PermittedSubclasses",                  { PermittedSubclassesAttribute.empty(it) }),
    UNKNOWN                                ("Unknown",                              { UnknownAttribute.empty(it) });

    companion object {
        private val nameToAttributeMap: Map<String, AttributeType> by lazy {
            values().associateBy { it.attributeName }
        }

        fun of(name: String) : AttributeType {
            return nameToAttributeMap[name] ?: UNKNOWN
        }
    }

    internal fun <T : Attribute> createAttribute(constantPoolEditor: ConstantPoolEditor): T {
        val attributeNameIndex = constantPoolEditor.addOrGetUtf8ConstantIndex(attributeName)
        @Suppress("UNCHECKED_CAST")
        return supplier.invoke(attributeNameIndex) as T
    }

    internal fun createAttribute(attributeNameIndex: Int): Attribute {
        return supplier.invoke(attributeNameIndex)
    }
}
