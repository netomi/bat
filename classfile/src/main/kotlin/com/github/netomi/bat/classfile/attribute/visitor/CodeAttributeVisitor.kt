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

package com.github.netomi.bat.classfile.attribute.visitor

import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.Method
import com.github.netomi.bat.classfile.attribute.CodeAttribute
import com.github.netomi.bat.classfile.attribute.LineNumberTableAttribute
import com.github.netomi.bat.classfile.attribute.LocalVariableTableAttribute
import com.github.netomi.bat.classfile.attribute.LocalVariableTypeTableAttribute
import com.github.netomi.bat.classfile.attribute.annotation.RuntimeInvisibleTypeAnnotationsAttribute
import com.github.netomi.bat.classfile.attribute.annotation.RuntimeTypeAnnotationsAttribute
import com.github.netomi.bat.classfile.attribute.annotation.RuntimeVisibleTypeAnnotationsAttribute
import com.github.netomi.bat.classfile.attribute.preverification.StackMapTableAttribute

fun interface CodeAttributeVisitor: AnyAttributeVisitor {
    fun visitLineNumberTable(classFile: ClassFile, method: Method, code: CodeAttribute, attribute: LineNumberTableAttribute) {
        visitAnyAttribute(classFile, attribute)
    }

    fun visitLocalVariableTable(classFile: ClassFile, method: Method, code: CodeAttribute, attribute: LocalVariableTableAttribute) {
        visitAnyAttribute(classFile, attribute)
    }

    fun visitLocalVariableTypeTable(classFile: ClassFile, method: Method, code: CodeAttribute, attribute: LocalVariableTypeTableAttribute) {
        visitAnyAttribute(classFile, attribute)
    }

    fun visitRuntimeTypeAnnotations(classFile: ClassFile, method: Method, code: CodeAttribute, attribute: RuntimeTypeAnnotationsAttribute) {
        visitAnyAttribute(classFile, attribute)
    }

    fun visitRuntimeVisibleTypeAnnotations(classFile: ClassFile, method: Method, code: CodeAttribute, attribute: RuntimeVisibleTypeAnnotationsAttribute) {
        visitRuntimeTypeAnnotations(classFile, method, code, attribute)
    }

    fun visitRuntimeInvisibleTypeAnnotations(classFile: ClassFile, method: Method, code: CodeAttribute, attribute: RuntimeInvisibleTypeAnnotationsAttribute) {
        visitRuntimeTypeAnnotations(classFile, method, code, attribute)
    }

    fun visitStackMapTable(classFile: ClassFile, method: Method, code: CodeAttribute, attribute: StackMapTableAttribute) {
        visitAnyAttribute(classFile, attribute)
    }
}