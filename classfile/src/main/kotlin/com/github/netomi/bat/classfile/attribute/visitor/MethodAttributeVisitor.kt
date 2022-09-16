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
import com.github.netomi.bat.classfile.attribute.*
import com.github.netomi.bat.classfile.attribute.annotation.*
import com.github.netomi.bat.classfile.instruction.visitor.InstructionVisitor

fun allInstructions(visitor: InstructionVisitor): MethodAttributeVisitor {
    return object: MethodAttributeVisitor {
        override fun visitAnyAttribute(classFile: ClassFile, attribute: Attribute) {}

        override fun visitCode(classFile: ClassFile, method: Method, attribute: CodeAttribute) {
            attribute.instructionsAccept(classFile, method, visitor)
        }
    }
}

fun interface MethodAttributeVisitor: AnyAttributeVisitor {

    fun visitAnnotationDefault(classFile: ClassFile, method: Method, attribute: AnnotationDefaultAttribute) {
        visitAnyAttribute(classFile, attribute)
    }

    fun visitCode(classFile: ClassFile, method: Method, attribute: CodeAttribute) {
        visitAnyAttribute(classFile, attribute)
    }

    fun visitDeprecated(classFile: ClassFile, method: Method, attribute: DeprecatedAttribute) {
        visitAnyAttribute(classFile, attribute)
    }

    fun visitExceptions(classFile: ClassFile, method: Method, attribute: ExceptionsAttribute) {
        visitAnyAttribute(classFile, attribute)
    }

    fun visitMethodParameters(classFile: ClassFile, method: Method, attribute: MethodParametersAttribute) {
        visitAnyAttribute(classFile, attribute)
    }

    fun visitRuntimeAnnotations(classFile: ClassFile, method: Method, attribute: RuntimeAnnotationsAttribute) {
        visitAnyAttribute(classFile, attribute)
    }

    fun visitRuntimeVisibleAnnotations(classFile: ClassFile, method: Method, attribute: RuntimeVisibleAnnotationsAttribute) {
        visitRuntimeAnnotations(classFile, method, attribute)
    }

    fun visitRuntimeInvisibleAnnotations(classFile: ClassFile, method: Method, attribute: RuntimeInvisibleAnnotationsAttribute) {
        visitRuntimeAnnotations(classFile, method, attribute)
    }

    fun visitRuntimeTypeAnnotations(classFile: ClassFile, method: Method, attribute: RuntimeTypeAnnotationsAttribute) {
        visitAnyAttribute(classFile, attribute)
    }

    fun visitRuntimeVisibleTypeAnnotations(classFile: ClassFile, method: Method, attribute: RuntimeVisibleTypeAnnotationsAttribute) {
        visitRuntimeTypeAnnotations(classFile, method, attribute)
    }

    fun visitRuntimeInvisibleTypeAnnotations(classFile: ClassFile, method: Method, attribute: RuntimeInvisibleTypeAnnotationsAttribute) {
        visitRuntimeTypeAnnotations(classFile, method, attribute)
    }

    fun visitRuntimeParameterAnnotations(classFile: ClassFile, method: Method, attribute: RuntimeParameterAnnotationsAttribute) {
        visitAnyAttribute(classFile, attribute)
    }

    fun visitRuntimeVisibleParameterAnnotations(classFile: ClassFile, method: Method, attribute: RuntimeVisibleParameterAnnotationsAttribute) {
        visitRuntimeParameterAnnotations(classFile, method, attribute)
    }

    fun visitRuntimeInvisibleParameterAnnotations(classFile: ClassFile, method: Method, attribute: RuntimeInvisibleParameterAnnotationsAttribute) {
        visitRuntimeParameterAnnotations(classFile, method, attribute)
    }

    fun visitSignature(classFile: ClassFile, method: Method, attribute: SignatureAttribute) {
        visitAnyAttribute(classFile, attribute)
    }

    fun visitSynthetic(classFile: ClassFile, method: Method, attribute: SyntheticAttribute) {
        visitAnyAttribute(classFile, attribute)
    }
}
