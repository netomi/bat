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
import com.github.netomi.bat.classfile.attribute.annotations.RuntimeAnnotationsAttribute
import com.github.netomi.bat.classfile.attribute.annotations.RuntimeInvisibleAnnotationsAttribute
import com.github.netomi.bat.classfile.attribute.annotations.RuntimeVisibleAnnotationsAttribute

fun interface MemberAttributeVisitor: FieldAttributeVisitor, MethodAttributeVisitor {
    fun visitDeprecatedAttribute(classFile: ClassFile, member: Member, attribute: DeprecatedAttribute) {
        visitAnyAttribute(classFile, attribute)
    }

    override fun visitDeprecatedAttribute(classFile: ClassFile, field: Field, attribute: DeprecatedAttribute) {
        visitDeprecatedAttribute(classFile, field as Member, attribute)
    }

    override fun visitDeprecatedAttribute(classFile: ClassFile, method: Method, attribute: DeprecatedAttribute) {
        visitDeprecatedAttribute(classFile, method as Member, attribute)
    }

    fun visitRuntimeAnnotationsAttribute(classFile: ClassFile, member: Member, attribute: RuntimeAnnotationsAttribute) {
        visitAnyAttribute(classFile, attribute)
    }

    override fun visitRuntimeAnnotationsAttribute(classFile: ClassFile, field: Field, attribute: RuntimeAnnotationsAttribute) {
        visitRuntimeAnnotationsAttribute(classFile, field as Member, attribute)
    }

    override fun visitRuntimeAnnotationsAttribute(classFile: ClassFile, method: Method, attribute: RuntimeAnnotationsAttribute) {
        visitRuntimeAnnotationsAttribute(classFile, method as Member, attribute)
    }

    fun visitRuntimeVisibleAnnotationsAttribute(classFile: ClassFile, member: Member, attribute: RuntimeVisibleAnnotationsAttribute) {
        visitRuntimeAnnotationsAttribute(classFile, member, attribute)
    }

    override fun visitRuntimeVisibleAnnotationsAttribute(classFile: ClassFile, field: Field, attribute: RuntimeVisibleAnnotationsAttribute) {
        visitRuntimeVisibleAnnotationsAttribute(classFile, field as Member, attribute)
    }

    override fun visitRuntimeVisibleAnnotationsAttribute(classFile: ClassFile, method: Method, attribute: RuntimeVisibleAnnotationsAttribute) {
        visitRuntimeVisibleAnnotationsAttribute(classFile, method as Member, attribute)
    }

    fun visitRuntimeInvisibleAnnotationsAttribute(classFile: ClassFile, member: Member, attribute: RuntimeInvisibleAnnotationsAttribute) {
        visitRuntimeAnnotationsAttribute(classFile, member, attribute)
    }

    override fun visitRuntimeInvisibleAnnotationsAttribute(classFile: ClassFile, field: Field, attribute: RuntimeInvisibleAnnotationsAttribute) {
        visitRuntimeInvisibleAnnotationsAttribute(classFile, field as Member, attribute)
    }

    override fun visitRuntimeInvisibleAnnotationsAttribute(classFile: ClassFile, method: Method, attribute: RuntimeInvisibleAnnotationsAttribute) {
        visitRuntimeInvisibleAnnotationsAttribute(classFile, method as Member, attribute)
    }

    fun visitSignatureAttribute(classFile: ClassFile, member: Member, attribute: SignatureAttribute) {
        visitAnyAttribute(classFile, attribute)
    }

    override fun visitSignatureAttribute(classFile: ClassFile, field: Field, attribute: SignatureAttribute) {
        visitSignatureAttribute(classFile, field as Member, attribute)
    }

    override fun visitSignatureAttribute(classFile: ClassFile, method: Method, attribute: SignatureAttribute) {
        visitSignatureAttribute(classFile, method as Member, attribute)
    }

    fun visitSyntheticAttribute(classFile: ClassFile, member: Member, attribute: SyntheticAttribute) {
        visitAnyAttribute(classFile, attribute)
    }

    override fun visitSyntheticAttribute(classFile: ClassFile, field: Field, attribute: SyntheticAttribute) {
        visitSyntheticAttribute(classFile, field as Member, attribute)
    }

    override fun visitSyntheticAttribute(classFile: ClassFile, method: Method, attribute: SyntheticAttribute) {
        visitSyntheticAttribute(classFile, method as Member, attribute)
    }
}