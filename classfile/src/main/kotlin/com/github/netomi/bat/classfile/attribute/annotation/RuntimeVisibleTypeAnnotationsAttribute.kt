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
package com.github.netomi.bat.classfile.attribute.annotation

import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.Field
import com.github.netomi.bat.classfile.Method
import com.github.netomi.bat.classfile.attribute.*
import com.github.netomi.bat.classfile.attribute.AttributeType
import com.github.netomi.bat.classfile.attribute.visitor.*
import com.github.netomi.bat.util.mutableListOfCapacity

/**
 * A class representing a RuntimeVisibleTypeAnnotations attribute in a class file.
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se13/html/jvms-4.html#jvms-4.7.20">RuntimeVisibleTypeAnnotations Attribute</a>
 */
data class RuntimeVisibleTypeAnnotationsAttribute
    private constructor(override var attributeNameIndex: Int,
                        override var typeAnnotations:    MutableList<TypeAnnotation> = mutableListOfCapacity(0))
    : RuntimeTypeAnnotationsAttribute(attributeNameIndex, typeAnnotations),
      AttachedToClass, AttachedToField, AttachedToMethod, AttachedToCodeAttribute, AttachedToRecordComponent {

    override val type: AttributeType
        get() = AttributeType.RUNTIME_INVISIBLE_TYPE_ANNOTATIONS

    override fun accept(classFile: ClassFile, visitor: ClassAttributeVisitor) {
        visitor.visitRuntimeVisibleTypeAnnotations(classFile, this)
    }

    override fun accept(classFile: ClassFile, field: Field, visitor: FieldAttributeVisitor) {
        visitor.visitRuntimeVisibleTypeAnnotations(classFile, field, this)
    }

    override fun accept(classFile: ClassFile, method: Method, visitor: MethodAttributeVisitor) {
        visitor.visitRuntimeVisibleTypeAnnotations(classFile, method, this)
    }

    override fun accept(classFile: ClassFile, method: Method, code: CodeAttribute, visitor: CodeAttributeVisitor) {
        visitor.visitRuntimeVisibleTypeAnnotations(classFile, method, code, this)
    }

    override fun accept(classFile: ClassFile, record: RecordAttribute, component: RecordComponent, visitor: RecordComponentAttributeVisitor) {
        visitor.visitRuntimeVisibleTypeAnnotations(classFile, record, component, this)
    }

    companion object {
        internal fun empty(attributeNameIndex: Int): RuntimeVisibleTypeAnnotationsAttribute {
            return RuntimeVisibleTypeAnnotationsAttribute(attributeNameIndex)
        }
    }
}