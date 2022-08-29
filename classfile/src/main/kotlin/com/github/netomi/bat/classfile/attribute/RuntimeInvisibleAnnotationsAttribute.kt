/*
 *  Copyright (c) 2020-2022 Thomas Neidhart.
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
import com.github.netomi.bat.classfile.annotation.Annotation
import com.github.netomi.bat.classfile.attribute.visitor.ClassAttributeVisitor
import com.github.netomi.bat.classfile.attribute.visitor.FieldAttributeVisitor
import com.github.netomi.bat.classfile.attribute.visitor.MethodAttributeVisitor
import com.github.netomi.bat.util.mutableListOfCapacity

/**
 * A class representing a RuntimeInvisibleAnnotations attribute in a class file.
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se13/html/jvms-4.html#jvms-4.7.17">RuntimeInvisibleAnnotations Attribute</a>
 */
data class RuntimeInvisibleAnnotationsAttribute
    private constructor(override val attributeNameIndex: Int,
                        override var annotations:        MutableList<Annotation> = mutableListOfCapacity(0))
    : RuntimeAnnotationsAttribute(attributeNameIndex, annotations), AttachedToClass, AttachedToField, AttachedToMethod {

    override val type: AttributeType
        get() = AttributeType.RUNTIME_INVISIBLE_ANNOTATIONS

    override fun accept(classFile: ClassFile, visitor: ClassAttributeVisitor) {
        visitor.visitRuntimeInvisibleAnnotationsAttribute(classFile, this)
    }

    override fun accept(classFile: ClassFile, field: Field, visitor: FieldAttributeVisitor) {
        visitor.visitRuntimeInvisibleAnnotationsAttribute(classFile, field, this)
    }

    override fun accept(classFile: ClassFile, method: Method, visitor: MethodAttributeVisitor) {
        visitor.visitRuntimeInvisibleAnnotationsAttribute(classFile, method, this)
    }

    companion object {
        internal fun empty(attributeNameIndex: Int): RuntimeInvisibleAnnotationsAttribute {
            return RuntimeInvisibleAnnotationsAttribute(attributeNameIndex)
        }
    }
}