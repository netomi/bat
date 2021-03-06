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

import com.github.netomi.bat.classfile.ConstantPool
import java.io.DataInput
import java.io.DataOutput
import java.io.IOException

abstract class Attribute protected constructor(open val attributeNameIndex: Int) {

    abstract val type: Type

    @Throws(IOException::class)
    abstract fun readAttributeData(input: DataInput)

    @Throws(IOException::class)
    abstract fun writeAttributeData(output: DataOutput)

    @Throws(IOException::class)
    fun writeAttribute(output: DataOutput) {
        output.writeByte(attributeNameIndex)
        writeAttributeData(output)
    }

    companion object {
        @JvmStatic
        fun readAttribute(input : DataInput, constantPool: ConstantPool): Attribute {
            val attributeNameIndex = input.readUnsignedShort()
            val attributeName      = constantPool.getString(attributeNameIndex)

            val attribute = Type.of(attributeName).createAttribute(attributeNameIndex)
            attribute.readAttributeData(input)

            return attribute
        }
    }

    /**
     * Known constant types as contained in a java class file.
     */
    enum class Type constructor(val attributeName: String, val supplier: ((Int) -> Attribute)?) {

        // Predefined attributes:
        // https://docs.oracle.com/javase/specs/jvms/se13/html/jvms-4.html#jvms-4.7-300

        CONSTANT_VALUE("ConstantValue", ConstantValueAttribute.Companion::create),
        CODE("Code", null),
        STACK_MAP_TABLE("StackMapTable", null),
        EXCEPTIONS("Exceptions", null),
        INNER_CLASSES("InnerClasses", null),
        ENCLOSING_METHOD("EnclosingMethod", EnclosingMethodAttribute.Companion::create),
        SYNTHETIC("Synthetic", SyntheticAttribute.Companion::create),
        SIGNATURE("Signature", null),
        SOURCE_FILE("SourceFile", SourceFileAttribute.Companion::create),
        SOURCE_DEBUG_EXTENSION("SourceDebugExtension", null),
        LINE_NUMBER_TABLE("LineNumberTable", null),
        LOCAL_VARIABLE_TABLE("LocalVariableTable", null),
        LOCAL_VARIABLE_TYPE_TABLE("LocalVariableTypeTable", null),
        DEPRECATED("Deprecated", DeprecatedAttribute.Companion::create),
        RUNTIME_VISIBLE_ANNOTATIONS("RuntimeVisibleAnnotations", null),
        RUNTIME_INVISIBLE_ANNOTATIONS("RuntimeInvisibleAnnotations", null),
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
        UNKNOWN("Unknown", UnknownAttribute.Companion::create);

        companion object {
            private val nameToAttributeMap: Map<String, Type> by lazy {
                values().map { it.attributeName to it }.toMap()
            }

            fun of(name: String) : Type {
                return nameToAttributeMap[name] ?: UNKNOWN
            }
        }

        fun createAttribute(attributeNameIndex: Int): Attribute {
            return supplier?.invoke(attributeNameIndex) ?: TODO("not implemented yet")
        }
    }

}