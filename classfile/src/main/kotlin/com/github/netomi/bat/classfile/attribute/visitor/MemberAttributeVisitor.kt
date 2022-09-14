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
import com.github.netomi.bat.classfile.Field
import com.github.netomi.bat.classfile.Member
import com.github.netomi.bat.classfile.Method
import com.github.netomi.bat.classfile.attribute.DeprecatedAttribute
import com.github.netomi.bat.classfile.attribute.SignatureAttribute
import com.github.netomi.bat.classfile.attribute.SyntheticAttribute
import com.github.netomi.bat.classfile.attribute.annotation.*

fun interface MemberAttributeVisitor: FieldAttributeVisitor, MethodAttributeVisitor {
    fun visitDeprecated(classFile: ClassFile, member: Member, attribute: DeprecatedAttribute) {
        visitAnyAttribute(classFile, attribute)
    }

    override fun visitDeprecated(classFile: ClassFile, field: Field, attribute: DeprecatedAttribute) {
        visitDeprecated(classFile, field as Member, attribute)
    }

    override fun visitDeprecated(classFile: ClassFile, method: Method, attribute: DeprecatedAttribute) {
        visitDeprecated(classFile, method as Member, attribute)
    }

    fun visitRuntimeAnnotations(classFile: ClassFile, member: Member, attribute: RuntimeAnnotationsAttribute) {
        visitAnyAttribute(classFile, attribute)
    }

    override fun visitRuntimeAnnotations(classFile: ClassFile, field: Field, attribute: RuntimeAnnotationsAttribute) {
        visitRuntimeAnnotations(classFile, field as Member, attribute)
    }

    override fun visitRuntimeAnnotations(classFile: ClassFile, method: Method, attribute: RuntimeAnnotationsAttribute) {
        visitRuntimeAnnotations(classFile, method as Member, attribute)
    }

    fun visitRuntimeVisibleAnnotations(classFile: ClassFile, member: Member, attribute: RuntimeVisibleAnnotationsAttribute) {
        visitRuntimeAnnotations(classFile, member, attribute)
    }

    override fun visitRuntimeVisibleAnnotations(classFile: ClassFile, field: Field, attribute: RuntimeVisibleAnnotationsAttribute) {
        visitRuntimeVisibleAnnotations(classFile, field as Member, attribute)
    }

    override fun visitRuntimeVisibleAnnotations(classFile: ClassFile, method: Method, attribute: RuntimeVisibleAnnotationsAttribute) {
        visitRuntimeVisibleAnnotations(classFile, method as Member, attribute)
    }

    fun visitRuntimeInvisibleAnnotations(classFile: ClassFile, member: Member, attribute: RuntimeInvisibleAnnotationsAttribute) {
        visitRuntimeAnnotations(classFile, member, attribute)
    }

    override fun visitRuntimeInvisibleAnnotations(classFile: ClassFile, field: Field, attribute: RuntimeInvisibleAnnotationsAttribute) {
        visitRuntimeInvisibleAnnotations(classFile, field as Member, attribute)
    }

    override fun visitRuntimeInvisibleAnnotations(classFile: ClassFile, method: Method, attribute: RuntimeInvisibleAnnotationsAttribute) {
        visitRuntimeInvisibleAnnotations(classFile, method as Member, attribute)
    }

    fun visitRuntimeTypeAnnotations(classFile: ClassFile, member: Member, attribute: RuntimeTypeAnnotationsAttribute) {
        visitAnyAttribute(classFile, attribute)
    }

    override fun visitRuntimeTypeAnnotations(classFile: ClassFile, field: Field, attribute: RuntimeTypeAnnotationsAttribute) {
        visitRuntimeTypeAnnotations(classFile, field as Member, attribute)
    }

    override fun visitRuntimeTypeAnnotations(classFile: ClassFile, method: Method, attribute: RuntimeTypeAnnotationsAttribute) {
        visitRuntimeTypeAnnotations(classFile, method as Member, attribute)
    }

    fun visitRuntimeVisibleTypeAnnotations(classFile: ClassFile, member: Member, attribute: RuntimeVisibleTypeAnnotationsAttribute) {
        visitRuntimeTypeAnnotations(classFile, member, attribute)
    }

    override fun visitRuntimeVisibleTypeAnnotations(classFile: ClassFile, field: Field, attribute: RuntimeVisibleTypeAnnotationsAttribute) {
        visitRuntimeVisibleTypeAnnotations(classFile, field as Member, attribute)
    }

    override fun visitRuntimeVisibleTypeAnnotations(classFile: ClassFile, method: Method, attribute: RuntimeVisibleTypeAnnotationsAttribute) {
        visitRuntimeVisibleTypeAnnotations(classFile, method as Member, attribute)
    }

    fun visitRuntimeInvisibleTypeAnnotations(classFile: ClassFile, member: Member, attribute: RuntimeInvisibleTypeAnnotationsAttribute) {
        visitRuntimeTypeAnnotations(classFile, member, attribute)
    }

    override fun visitRuntimeInvisibleTypeAnnotations(classFile: ClassFile, field: Field, attribute: RuntimeInvisibleTypeAnnotationsAttribute) {
        visitRuntimeInvisibleTypeAnnotations(classFile, field as Member, attribute)
    }

    override fun visitRuntimeInvisibleTypeAnnotations(classFile: ClassFile, method: Method, attribute: RuntimeInvisibleTypeAnnotationsAttribute) {
        visitRuntimeInvisibleTypeAnnotations(classFile, method as Member, attribute)
    }

    fun visitSignature(classFile: ClassFile, member: Member, attribute: SignatureAttribute) {
        visitAnyAttribute(classFile, attribute)
    }

    override fun visitSignature(classFile: ClassFile, field: Field, attribute: SignatureAttribute) {
        visitSignature(classFile, field as Member, attribute)
    }

    override fun visitSignature(classFile: ClassFile, method: Method, attribute: SignatureAttribute) {
        visitSignature(classFile, method as Member, attribute)
    }

    fun visitSynthetic(classFile: ClassFile, member: Member, attribute: SyntheticAttribute) {
        visitAnyAttribute(classFile, attribute)
    }

    override fun visitSynthetic(classFile: ClassFile, field: Field, attribute: SyntheticAttribute) {
        visitSynthetic(classFile, field as Member, attribute)
    }

    override fun visitSynthetic(classFile: ClassFile, method: Method, attribute: SyntheticAttribute) {
        visitSynthetic(classFile, method as Member, attribute)
    }
}