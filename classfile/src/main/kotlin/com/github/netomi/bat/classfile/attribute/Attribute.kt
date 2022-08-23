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
import com.github.netomi.bat.classfile.attribute.annotations.RuntimeInvisibleAnnotationsAttribute
import com.github.netomi.bat.classfile.attribute.annotations.RuntimeVisibleAnnotationsAttribute
import com.github.netomi.bat.classfile.attribute.visitor.AttributeVisitor
import java.io.DataInput
import java.io.DataOutput
import java.io.IOException

/**
 * Base class for attributes as contained in a class file.
 */
abstract class Attribute protected constructor(open val attributeNameIndex: Int) {

    internal abstract val type: AttributeType

    fun getAttributeName(classFile: ClassFile): String {
        return classFile.getString(attributeNameIndex)
    }

    @Throws(IOException::class)
    protected abstract fun readAttributeData(input: DataInput)

    @Throws(IOException::class)
    protected abstract fun writeAttributeData(output: DataOutput)

    @Throws(IOException::class)
    fun writeAttribute(output: DataOutput) {
        output.writeByte(attributeNameIndex)
        writeAttributeData(output)
    }

    abstract fun accept(classFile: ClassFile, visitor: AttributeVisitor)

    companion object {
        internal fun readAttribute(input : DataInput, classFile: ClassFile): Attribute {
            val attributeNameIndex = input.readUnsignedShort()
            val attributeName      = classFile.getString(attributeNameIndex)

            val attribute = AttributeType.of(attributeName).createAttribute(attributeNameIndex)
            attribute.readAttributeData(input)

            return attribute
        }
    }
}

/**
 * Known constant types as contained in a java class file.
 */
internal enum class AttributeType constructor(val attributeName: String, private val supplier: ((Int) -> Attribute)?) {

    // Predefined attributes:
    // https://docs.oracle.com/javase/specs/jvms/se13/html/jvms-4.html#jvms-4.7-300

    CONSTANT_VALUE("ConstantValue", ConstantValueAttribute.Companion::empty),
    CODE("Code", null),
    STACK_MAP_TABLE("StackMapTable", null),
    EXCEPTIONS("Exceptions", null),
    INNER_CLASSES("InnerClasses", null),
    ENCLOSING_METHOD("EnclosingMethod", EnclosingMethodAttribute.Companion::empty),
    SYNTHETIC("Synthetic", SyntheticAttribute.Companion::empty),
    SIGNATURE("Signature", SignatureAttribute.Companion::empty),
    SOURCE_FILE("SourceFile", SourceFileAttribute.Companion::empty),
    SOURCE_DEBUG_EXTENSION("SourceDebugExtension", null),
    LINE_NUMBER_TABLE("LineNumberTable", null),
    LOCAL_VARIABLE_TABLE("LocalVariableTable", null),
    LOCAL_VARIABLE_TYPE_TABLE("LocalVariableTypeTable", null),
    DEPRECATED("Deprecated", DeprecatedAttribute.Companion::empty),
    RUNTIME_VISIBLE_ANNOTATIONS("RuntimeVisibleAnnotations", RuntimeVisibleAnnotationsAttribute.Companion::empty),
    RUNTIME_INVISIBLE_ANNOTATIONS("RuntimeInvisibleAnnotations", RuntimeInvisibleAnnotationsAttribute.Companion::empty),
    RUNTIME_VISIBLE_PARAMETER_ANNOTATIONS("RuntimeVisibleParameterAnnotations", null),
    RUNTIME_INVISIBLE_PARAMETER_ANNOTATIONS("RuntimeInvisibleParameterAnnotations", null),
    RUNTIME_VISIBLE_TYPE_ANNOTATIONS("RuntimeVisibleTypeAnnotations", null),
    RUNTIME_INVISIBLE_TYPE_ANNOTATIONS("RuntimeInvisibleTypeAnnotations", null),
    ANNOTATION_DEFAULT("AnnotationDefault", null),
    BOOTSTRAP_METHOD("BootstrapMethod", null),
    METHOD_PARAMETERS("MethodParameters", null),
    MODULE("Module", null),
    MODULE_PACKAGES("ModulePackages", null),
    MODULE_MAIN_CLASS("ModuleMainClass", null),
    NEST_HOST("NestHost", null),
    NEST_MEMBERS("NestMembers", null),
    UNKNOWN("Unknown", UnknownAttribute.Companion::empty);

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
