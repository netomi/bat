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
import com.github.netomi.bat.classfile.Member
import com.github.netomi.bat.classfile.Method
import com.github.netomi.bat.classfile.attribute.*
import com.github.netomi.bat.classfile.attribute.annotation.*

fun interface AttributeVisitor: ClassAttributeVisitor, MemberAttributeVisitor, CodeAttributeVisitor, RecordComponentAttributeVisitor {
    fun visitAnyDeprecated(classFile: ClassFile, attribute: DeprecatedAttribute) {
        visitAnyAttribute(classFile, attribute)
    }

    override fun visitDeprecated(classFile: ClassFile, attribute: DeprecatedAttribute) {
        visitAnyDeprecated(classFile, attribute)
    }

    override fun visitDeprecated(classFile: ClassFile, member: Member, attribute: DeprecatedAttribute) {
        visitAnyDeprecated(classFile, attribute)
    }

    fun visitAnyRuntimeAnnotations(classFile: ClassFile, attribute: RuntimeAnnotationsAttribute) {
        visitAnyAttribute(classFile, attribute)
    }

    override fun visitRuntimeAnnotations(classFile: ClassFile, attribute: RuntimeAnnotationsAttribute) {
        visitAnyRuntimeAnnotations(classFile, attribute)
    }

    override fun visitRuntimeAnnotations(classFile: ClassFile, member: Member, attribute: RuntimeAnnotationsAttribute) {
        visitAnyRuntimeAnnotations(classFile, attribute)
    }

    override fun visitRuntimeAnnotations(classFile: ClassFile, record: RecordAttribute, component: RecordComponent, attribute: RuntimeAnnotationsAttribute) {
        visitAnyRuntimeAnnotations(classFile, attribute)
    }

    fun visitAnyRuntimeVisibleAnnotations(classFile: ClassFile, attribute: RuntimeVisibleAnnotationsAttribute) {
        visitAnyRuntimeAnnotations(classFile, attribute)
    }

    override fun visitRuntimeVisibleAnnotations(classFile: ClassFile, attribute: RuntimeVisibleAnnotationsAttribute) {
        visitAnyRuntimeVisibleAnnotations(classFile, attribute)
    }

    override fun visitRuntimeVisibleAnnotations(classFile: ClassFile, member: Member, attribute: RuntimeVisibleAnnotationsAttribute) {
        visitAnyRuntimeVisibleAnnotations(classFile, attribute)
    }

    override fun visitRuntimeVisibleAnnotations(classFile: ClassFile, record: RecordAttribute, component: RecordComponent, attribute: RuntimeVisibleAnnotationsAttribute) {
        visitAnyRuntimeVisibleAnnotations(classFile, attribute)
    }

    fun visitAnyRuntimeInvisibleAnnotations(classFile: ClassFile, attribute: RuntimeInvisibleAnnotationsAttribute) {
        visitAnyRuntimeAnnotations(classFile, attribute)
    }

    override fun visitRuntimeInvisibleAnnotations(classFile: ClassFile, attribute: RuntimeInvisibleAnnotationsAttribute) {
        visitAnyRuntimeInvisibleAnnotations(classFile, attribute)
    }

    override fun visitRuntimeInvisibleAnnotations(classFile: ClassFile, member: Member, attribute: RuntimeInvisibleAnnotationsAttribute) {
        visitAnyRuntimeInvisibleAnnotations(classFile, attribute)
    }

    override fun visitRuntimeInvisibleAnnotations(classFile: ClassFile, record: RecordAttribute, component: RecordComponent, attribute: RuntimeInvisibleAnnotationsAttribute) {
        visitAnyRuntimeInvisibleAnnotations(classFile, attribute)
    }

    fun visitAnyRuntimeTypeAnnotations(classFile: ClassFile, attribute: RuntimeTypeAnnotationsAttribute) {
        visitAnyAttribute(classFile, attribute)
    }

    override fun visitRuntimeTypeAnnotations(classFile: ClassFile, attribute: RuntimeTypeAnnotationsAttribute) {
        visitAnyRuntimeTypeAnnotations(classFile, attribute)
    }

    override fun visitRuntimeTypeAnnotations(classFile: ClassFile, member: Member, attribute: RuntimeTypeAnnotationsAttribute) {
        visitAnyRuntimeTypeAnnotations(classFile, attribute)
    }

    override fun visitRuntimeTypeAnnotations(classFile: ClassFile, method: Method, code: CodeAttribute, attribute: RuntimeTypeAnnotationsAttribute) {
        visitAnyRuntimeTypeAnnotations(classFile, attribute)
    }

    override fun visitRuntimeTypeAnnotations(classFile: ClassFile, record: RecordAttribute, component: RecordComponent, attribute: RuntimeTypeAnnotationsAttribute) {
        visitAnyRuntimeTypeAnnotations(classFile, attribute)
    }

    fun visitAnyRuntimeVisibleTypeAnnotations(classFile: ClassFile, attribute: RuntimeVisibleTypeAnnotationsAttribute) {
        visitAnyRuntimeTypeAnnotations(classFile, attribute)
    }

    override fun visitRuntimeVisibleTypeAnnotations(classFile: ClassFile, attribute: RuntimeVisibleTypeAnnotationsAttribute) {
        visitAnyRuntimeVisibleTypeAnnotations(classFile, attribute)
    }

    override fun visitRuntimeVisibleTypeAnnotations(classFile: ClassFile, member: Member, attribute: RuntimeVisibleTypeAnnotationsAttribute) {
        visitAnyRuntimeVisibleTypeAnnotations(classFile, attribute)
    }

    override fun visitRuntimeVisibleTypeAnnotations(classFile: ClassFile, method: Method, code: CodeAttribute, attribute: RuntimeVisibleTypeAnnotationsAttribute) {
        visitAnyRuntimeVisibleTypeAnnotations(classFile, attribute)
    }

    override fun visitRuntimeVisibleTypeAnnotations(classFile: ClassFile, record: RecordAttribute, component: RecordComponent, attribute: RuntimeVisibleTypeAnnotationsAttribute) {
        visitAnyRuntimeVisibleTypeAnnotations(classFile, attribute)
    }

    fun visitAnyRuntimeInvisibleTypeAnnotations(classFile: ClassFile, attribute: RuntimeInvisibleTypeAnnotationsAttribute) {
        visitAnyRuntimeTypeAnnotations(classFile, attribute)
    }

    override fun visitRuntimeInvisibleTypeAnnotations(classFile: ClassFile, attribute: RuntimeInvisibleTypeAnnotationsAttribute) {
        visitAnyRuntimeInvisibleTypeAnnotations(classFile, attribute)
    }

    override fun visitRuntimeInvisibleTypeAnnotations(classFile: ClassFile, member: Member, attribute: RuntimeInvisibleTypeAnnotationsAttribute) {
        visitAnyRuntimeInvisibleTypeAnnotations(classFile, attribute)
    }

    override fun visitRuntimeInvisibleTypeAnnotations(classFile: ClassFile, method: Method, code: CodeAttribute, attribute: RuntimeInvisibleTypeAnnotationsAttribute) {
        visitAnyRuntimeInvisibleTypeAnnotations(classFile, attribute)
    }

    override fun visitRuntimeInvisibleTypeAnnotations(classFile: ClassFile, record: RecordAttribute, component: RecordComponent, attribute: RuntimeInvisibleTypeAnnotationsAttribute) {
        visitAnyRuntimeInvisibleTypeAnnotations(classFile, attribute)
    }

    fun visitAnySignature(classFile: ClassFile, attribute: SignatureAttribute) {
        visitAnyAttribute(classFile, attribute)
    }

    override fun visitSignature(classFile: ClassFile, attribute: SignatureAttribute) {
        visitAnySignature(classFile, attribute)
    }

    override fun visitSignature(classFile: ClassFile, member: Member, attribute: SignatureAttribute) {
        visitAnySignature(classFile, attribute)
    }

    override fun visitSignature(classFile: ClassFile, record: RecordAttribute, component: RecordComponent, attribute: SignatureAttribute) {
        visitAnySignature(classFile, attribute)
    }

    fun visitAnySynthetic(classFile: ClassFile, attribute: SyntheticAttribute) {
        visitAnyAttribute(classFile, attribute)
    }

    override fun visitSynthetic(classFile: ClassFile, attribute: SyntheticAttribute) {
        visitAnySynthetic(classFile, attribute)
    }

    override fun visitSynthetic(classFile: ClassFile, member: Member, attribute: SyntheticAttribute) {
        visitAnySynthetic(classFile, attribute)
    }
}