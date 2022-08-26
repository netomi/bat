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
import com.github.netomi.bat.classfile.attribute.DeprecatedAttribute
import com.github.netomi.bat.classfile.attribute.SignatureAttribute
import com.github.netomi.bat.classfile.attribute.annotations.RuntimeAnnotationsAttribute
import com.github.netomi.bat.classfile.attribute.annotations.RuntimeInvisibleAnnotationsAttribute
import com.github.netomi.bat.classfile.attribute.annotations.RuntimeVisibleAnnotationsAttribute

fun interface AttributeVisitor: ClassAttributeVisitor, MemberAttributeVisitor {
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

    fun visitAnyRuntimeVisibleAnnotationsAttribute(classFile: ClassFile, attribute: RuntimeVisibleAnnotationsAttribute) {
        visitAnyRuntimeAnnotationsAttribute(classFile, attribute)
    }

    override fun visitRuntimeVisibleAnnotationsAttribute(classFile: ClassFile, attribute: RuntimeVisibleAnnotationsAttribute) {
        visitAnyRuntimeVisibleAnnotationsAttribute(classFile, attribute)
    }

    override fun visitRuntimeVisibleAnnotationsAttribute(classFile: ClassFile, member: Member, attribute: RuntimeVisibleAnnotationsAttribute) {
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

    fun visitAnySignatureAttribute(classFile: ClassFile, attribute: SignatureAttribute) {
        visitAnyAttribute(classFile, attribute)
    }

    override fun visitSignatureAttribute(classFile: ClassFile, attribute: SignatureAttribute) {
        visitAnySignatureAttribute(classFile, attribute)
    }

    override fun visitSignatureAttribute(classFile: ClassFile, member: Member, attribute: SignatureAttribute) {
        visitAnySignatureAttribute(classFile, attribute)
    }
}