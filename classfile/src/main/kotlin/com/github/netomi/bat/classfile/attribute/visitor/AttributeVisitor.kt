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
    fun visitAnyDeprecatedAttribute(classFile: ClassFile, attribute: DeprecatedAttribute) {
        visitAnyAttribute(classFile, attribute)
    }

    override fun visitDeprecatedAttribute(classFile: ClassFile, attribute: DeprecatedAttribute) {
        visitAnyDeprecatedAttribute(classFile, attribute)
    }

    override fun visitDeprecatedAttribute(classFile: ClassFile, member: Member, attribute: DeprecatedAttribute) {
        visitAnyDeprecatedAttribute(classFile, attribute)
    }

    fun visitAnyRuntimeAnnotationsAttribute(classFile: ClassFile, attribute: RuntimeAnnotationsAttribute) {
        visitAnyAttribute(classFile, attribute)
    }

    override fun visitRuntimeAnnotationsAttribute(classFile: ClassFile, attribute: RuntimeAnnotationsAttribute) {
        visitAnyRuntimeAnnotationsAttribute(classFile, attribute)
    }

    override fun visitRuntimeAnnotationsAttribute(classFile: ClassFile, member: Member, attribute: RuntimeAnnotationsAttribute) {
        visitAnyRuntimeAnnotationsAttribute(classFile, attribute)
    }

    override fun visitRuntimeAnnotationsAttribute(classFile: ClassFile, record: RecordAttribute, component: RecordComponent, attribute: RuntimeAnnotationsAttribute) {
        visitAnyRuntimeAnnotationsAttribute(classFile, attribute)
    }

    fun visitAnyRuntimeVisibleAnnotationsAttribute(classFile: ClassFile, attribute: RuntimeVisibleAnnotationsAttribute) {
        visitAnyRuntimeAnnotationsAttribute(classFile, attribute)
    }

    override fun visitRuntimeVisibleAnnotationsAttribute(classFile: ClassFile, attribute: RuntimeVisibleAnnotationsAttribute) {
        visitAnyRuntimeVisibleAnnotationsAttribute(classFile, attribute)
    }

    override fun visitRuntimeVisibleAnnotationsAttribute(classFile: ClassFile, member: Member, attribute: RuntimeVisibleAnnotationsAttribute) {
        visitAnyRuntimeVisibleAnnotationsAttribute(classFile, attribute)
    }

    override fun visitRuntimeVisibleAnnotationsAttribute(classFile: ClassFile, record: RecordAttribute, component: RecordComponent, attribute: RuntimeVisibleAnnotationsAttribute) {
        visitAnyRuntimeVisibleAnnotationsAttribute(classFile, attribute)
    }

    fun visitAnyRuntimeInvisibleAnnotationsAttribute(classFile: ClassFile, attribute: RuntimeInvisibleAnnotationsAttribute) {
        visitAnyRuntimeAnnotationsAttribute(classFile, attribute)
    }

    override fun visitRuntimeInvisibleAnnotationsAttribute(classFile: ClassFile, attribute: RuntimeInvisibleAnnotationsAttribute) {
        visitAnyRuntimeInvisibleAnnotationsAttribute(classFile, attribute)
    }

    override fun visitRuntimeInvisibleAnnotationsAttribute(classFile: ClassFile, member: Member, attribute: RuntimeInvisibleAnnotationsAttribute) {
        visitAnyRuntimeInvisibleAnnotationsAttribute(classFile, attribute)
    }

    override fun visitRuntimeInvisibleAnnotationsAttribute(classFile: ClassFile, record: RecordAttribute, component: RecordComponent, attribute: RuntimeInvisibleAnnotationsAttribute) {
        visitAnyRuntimeInvisibleAnnotationsAttribute(classFile, attribute)
    }

    fun visitAnyRuntimeTypeAnnotationsAttribute(classFile: ClassFile, attribute: RuntimeTypeAnnotationsAttribute) {
        visitAnyAttribute(classFile, attribute)
    }

    override fun visitRuntimeTypeAnnotationsAttribute(classFile: ClassFile, attribute: RuntimeTypeAnnotationsAttribute) {
        visitAnyRuntimeTypeAnnotationsAttribute(classFile, attribute)
    }

    override fun visitRuntimeTypeAnnotationsAttribute(classFile: ClassFile, member: Member, attribute: RuntimeTypeAnnotationsAttribute) {
        visitAnyRuntimeTypeAnnotationsAttribute(classFile, attribute)
    }

    override fun visitRuntimeTypeAnnotationsAttribute(classFile: ClassFile, method: Method, code: CodeAttribute, attribute: RuntimeTypeAnnotationsAttribute) {
        visitAnyRuntimeTypeAnnotationsAttribute(classFile, attribute)
    }

    override fun visitRuntimeTypeAnnotationsAttribute(classFile: ClassFile, record: RecordAttribute, component: RecordComponent, attribute: RuntimeTypeAnnotationsAttribute) {
        visitAnyRuntimeTypeAnnotationsAttribute(classFile, attribute)
    }

    fun visitAnyRuntimeVisibleTypeAnnotationsAttribute(classFile: ClassFile, attribute: RuntimeVisibleTypeAnnotationsAttribute) {
        visitAnyRuntimeTypeAnnotationsAttribute(classFile, attribute)
    }

    override fun visitRuntimeVisibleTypeAnnotationsAttribute(classFile: ClassFile, attribute: RuntimeVisibleTypeAnnotationsAttribute) {
        visitAnyRuntimeVisibleTypeAnnotationsAttribute(classFile, attribute)
    }

    override fun visitRuntimeVisibleTypeAnnotationsAttribute(classFile: ClassFile, member: Member, attribute: RuntimeVisibleTypeAnnotationsAttribute) {
        visitAnyRuntimeVisibleTypeAnnotationsAttribute(classFile, attribute)
    }

    override fun visitRuntimeVisibleTypeAnnotationsAttribute(classFile: ClassFile, method: Method, code: CodeAttribute, attribute: RuntimeVisibleTypeAnnotationsAttribute) {
        visitAnyRuntimeVisibleTypeAnnotationsAttribute(classFile, attribute)
    }

    override fun visitRuntimeVisibleTypeAnnotationsAttribute(classFile: ClassFile, record: RecordAttribute, component: RecordComponent, attribute: RuntimeVisibleTypeAnnotationsAttribute) {
        visitAnyRuntimeVisibleTypeAnnotationsAttribute(classFile, attribute)
    }

    fun visitAnyRuntimeInvisibleTypeAnnotationsAttribute(classFile: ClassFile, attribute: RuntimeInvisibleTypeAnnotationsAttribute) {
        visitAnyRuntimeTypeAnnotationsAttribute(classFile, attribute)
    }

    override fun visitRuntimeInvisibleTypeAnnotationsAttribute(classFile: ClassFile, attribute: RuntimeInvisibleTypeAnnotationsAttribute) {
        visitAnyRuntimeInvisibleTypeAnnotationsAttribute(classFile, attribute)
    }

    override fun visitRuntimeInvisibleTypeAnnotationsAttribute(classFile: ClassFile, member: Member, attribute: RuntimeInvisibleTypeAnnotationsAttribute) {
        visitAnyRuntimeInvisibleTypeAnnotationsAttribute(classFile, attribute)
    }

    override fun visitRuntimeInvisibleTypeAnnotationsAttribute(classFile: ClassFile, method: Method, code: CodeAttribute, attribute: RuntimeInvisibleTypeAnnotationsAttribute) {
        visitAnyRuntimeInvisibleTypeAnnotationsAttribute(classFile, attribute)
    }

    override fun visitRuntimeInvisibleTypeAnnotationsAttribute(classFile: ClassFile, record: RecordAttribute, component: RecordComponent, attribute: RuntimeInvisibleTypeAnnotationsAttribute) {
        visitAnyRuntimeInvisibleTypeAnnotationsAttribute(classFile, attribute)
    }

    fun visitAnySignatureAttribute(classFile: ClassFile, attribute: SignatureAttribute) {
        visitAnyAttribute(classFile, attribute)
    }

    override fun visitSignatureAttribute(classFile: ClassFile, attribute: SignatureAttribute) {
        visitAnySignatureAttribute(classFile, attribute)
    }

    override fun visitSignatureAttribute(classFile: ClassFile, member: Member, attribute: SignatureAttribute) {
        visitAnySignatureAttribute(classFile, attribute)
    }

    override fun visitSignatureAttribute(classFile: ClassFile, record: RecordAttribute, component: RecordComponent, attribute: SignatureAttribute) {
        visitAnySignatureAttribute(classFile, attribute)
    }
}