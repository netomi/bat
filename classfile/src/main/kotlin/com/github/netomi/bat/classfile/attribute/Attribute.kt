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
import com.github.netomi.bat.classfile.attribute.preverification.StackMapTableAttribute
import com.github.netomi.bat.classfile.attribute.visitor.*
import com.github.netomi.bat.classfile.io.ClassDataInput
import com.github.netomi.bat.classfile.io.ClassDataOutput
import com.github.netomi.bat.classfile.io.ClassFileContent
import java.io.IOException

/**
 * Base class for attributes as contained in a class file.
 */
abstract class Attribute protected constructor(open val attributeNameIndex: Int): ClassFileContent() {

    internal abstract val type: AttributeType

    fun getAttributeName(classFile: ClassFile): String {
        return classFile.getString(attributeNameIndex)
    }

    @Throws(IOException::class)
    internal abstract fun readAttributeData(input: ClassDataInput)

    @Throws(IOException::class)
    internal abstract fun writeAttributeData(output: ClassDataOutput)

    @Throws(IOException::class)
    override fun write(output: ClassDataOutput) {
        output.writeByte(attributeNameIndex)
        writeAttributeData(output)
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
            attribute.readAttributeData(input)

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
internal enum class AttributeType constructor(val attributeName: String, private val supplier: ((Int) -> Attribute)?) {

    // Predefined attributes:
    // https://docs.oracle.com/javase/specs/jvms/se17/html/jvms-4.html#jvms-4.7-300

    CONSTANT_VALUE                         ("ConstantValue",                        ConstantValueAttribute.Companion::empty),
    CODE                                   ("Code",                                 CodeAttribute.Companion::empty),
    STACK_MAP_TABLE                        ("StackMapTable",                        StackMapTableAttribute.Companion::empty),
    EXCEPTIONS                             ("Exceptions",                           ExceptionsAttribute.Companion::empty),
    INNER_CLASSES                          ("InnerClasses",                         InnerClassesAttribute.Companion::empty),
    ENCLOSING_METHOD                       ("EnclosingMethod",                      EnclosingMethodAttribute.Companion::empty),
    SYNTHETIC                              ("Synthetic",                            SyntheticAttribute.Companion::empty),
    SIGNATURE                              ("Signature",                            SignatureAttribute.Companion::empty),
    SOURCE_FILE                            ("SourceFile",                           SourceFileAttribute.Companion::empty),
    SOURCE_DEBUG_EXTENSION                 ("SourceDebugExtension",                 SourceDebugExtensionAttribute.Companion::empty),
    LINE_NUMBER_TABLE                      ("LineNumberTable",                      LineNumberTableAttribute.Companion::empty),
    LOCAL_VARIABLE_TABLE                   ("LocalVariableTable",                   LocalVariableTableAttribute.Companion::empty),
    LOCAL_VARIABLE_TYPE_TABLE              ("LocalVariableTypeTable",               LocalVariableTypeTableAttribute.Companion::empty),
    DEPRECATED                             ("Deprecated",                           DeprecatedAttribute.Companion::empty),
    RUNTIME_VISIBLE_ANNOTATIONS            ("RuntimeVisibleAnnotations",            RuntimeVisibleAnnotationsAttribute.Companion::empty),
    RUNTIME_INVISIBLE_ANNOTATIONS          ("RuntimeInvisibleAnnotations",          RuntimeInvisibleAnnotationsAttribute.Companion::empty),
    RUNTIME_VISIBLE_PARAMETER_ANNOTATIONS  ("RuntimeVisibleParameterAnnotations",   RuntimeVisibleParameterAnnotationsAttribute.Companion::empty),
    RUNTIME_INVISIBLE_PARAMETER_ANNOTATIONS("RuntimeInvisibleParameterAnnotations", RuntimeInvisibleParameterAnnotationsAttribute.Companion::empty),
    RUNTIME_VISIBLE_TYPE_ANNOTATIONS       ("RuntimeVisibleTypeAnnotations",        RuntimeVisibleTypeAnnotationsAttribute.Companion::empty),
    RUNTIME_INVISIBLE_TYPE_ANNOTATIONS     ("RuntimeInvisibleTypeAnnotations",      RuntimeInvisibleTypeAnnotationsAttribute.Companion::empty),
    ANNOTATION_DEFAULT                     ("AnnotationDefault",                    AnnotationDefaultAttribute.Companion::empty),
    BOOTSTRAP_METHOD                       ("BootstrapMethods",                     BootstrapMethodsAttribute.Companion::empty),
    METHOD_PARAMETERS                      ("MethodParameters",                     MethodParametersAttribute.Companion::empty),
    MODULE                                 ("Module",                               ModuleAttribute.Companion::empty),
    MODULE_PACKAGES                        ("ModulePackages",                       ModulePackagesAttribute.Companion::empty),
    MODULE_MAIN_CLASS                      ("ModuleMainClass",                      ModuleMainClassAttribute.Companion::empty),
    NEST_HOST                              ("NestHost",                             NestHostAttribute.Companion::empty),
    NEST_MEMBERS                           ("NestMembers",                          NestMembersAttribute.Companion::empty),
    RECORD                                 ("Record",                               RecordAttribute.Companion::empty),
    PERMITTED_SUBCLASSES                   ("PermittedSubclasses",                  PermittedSubclassesAttribute.Companion::empty),
    UNKNOWN                                ("Unknown",                              UnknownAttribute.Companion::empty);

    companion object {
        private val nameToAttributeMap: Map<String, AttributeType> by lazy {
            values().associateBy { it.attributeName }
        }

        fun of(name: String) : AttributeType {
            return nameToAttributeMap[name] ?: UNKNOWN
        }
    }

    fun createAttribute(attributeNameIndex: Int): Attribute {
        return supplier?.invoke(attributeNameIndex) ?: UNKNOWN.supplier!!.invoke(attributeNameIndex)
    }
}
