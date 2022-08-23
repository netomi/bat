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
import com.github.netomi.bat.classfile.attribute.*
import com.github.netomi.bat.classfile.attribute.annotations.RuntimeAnnotationsAttribute
import com.github.netomi.bat.classfile.attribute.annotations.RuntimeInvisibleAnnotationsAttribute
import com.github.netomi.bat.classfile.attribute.annotations.RuntimeVisibleAnnotationsAttribute

fun fieldAttributes(visitor: FieldAttributeVisitor): AttributeVisitor {
    return FieldAttributeAdapter.of(visitor)
}

fun interface FieldAttributeVisitor: AnyAttributeVisitor {

    fun visitConstantValueAttribute(classFile: ClassFile, attribute: ConstantValueAttribute) {
        visitAnyAttribute(classFile, attribute)
    }

    fun visitDeprecatedAttribute(classFile: ClassFile, attribute: DeprecatedAttribute) {
        visitAnyAttribute(classFile, attribute)
    }

    fun visitAnyRuntimeAnnotationsAttribute(classFile: ClassFile, attribute: RuntimeAnnotationsAttribute) {
        visitAnyAttribute(classFile, attribute)
    }

    fun visitRuntimeVisibleAnnotationsAttribute(classFile: ClassFile, attribute: RuntimeVisibleAnnotationsAttribute) {
        visitAnyRuntimeAnnotationsAttribute(classFile, attribute)
    }

    fun visitRuntimeInvisibleAnnotationsAttribute(classFile: ClassFile, attribute: RuntimeInvisibleAnnotationsAttribute) {
        visitAnyRuntimeAnnotationsAttribute(classFile, attribute)
    }

    fun visitSignatureAttribute(classFile: ClassFile, attribute: SignatureAttribute) {
        visitAnyAttribute(classFile, attribute)
    }

    fun visitSyntheticAttribute(classFile: ClassFile, attribute: SyntheticAttribute) {
        visitAnyAttribute(classFile, attribute)
    }
}

private class FieldAttributeAdapter private constructor(val visitor: FieldAttributeVisitor): AttributeVisitor {
    override fun visitAnyAttribute(classFile: ClassFile, attribute: Attribute) {}

    override fun visitConstantValueAttribute(classFile: ClassFile, attribute: ConstantValueAttribute) {
        visitor.visitConstantValueAttribute(classFile, attribute)
    }

    override fun visitDeprecatedAttribute(classFile: ClassFile, attribute: DeprecatedAttribute) {
        visitor.visitDeprecatedAttribute(classFile, attribute)
    }

    override fun visitAnyRuntimeAnnotationsAttribute(classFile: ClassFile, attribute: RuntimeAnnotationsAttribute) {
        visitor.visitAnyRuntimeAnnotationsAttribute(classFile, attribute)
    }

    override fun visitRuntimeVisibleAnnotationsAttribute(classFile: ClassFile, attribute: RuntimeVisibleAnnotationsAttribute) {
        visitor.visitRuntimeVisibleAnnotationsAttribute(classFile, attribute)
    }

    override fun visitRuntimeInvisibleAnnotationsAttribute(classFile: ClassFile, attribute: RuntimeInvisibleAnnotationsAttribute) {
        visitor.visitRuntimeInvisibleAnnotationsAttribute(classFile, attribute)
    }

    override fun visitSignatureAttribute(classFile: ClassFile, attribute: SignatureAttribute) {
        visitor.visitSignatureAttribute(classFile, attribute)
    }

    override fun visitSyntheticAttribute(classFile: ClassFile, attribute: SyntheticAttribute) {
        visitor.visitSyntheticAttribute(classFile, attribute)
    }

    override fun visitUnknownAttribute(classFile: ClassFile, attribute: UnknownAttribute) {
        visitor.visitUnknownAttribute(classFile, attribute)
    }

    companion object {
        fun of(visitor: FieldAttributeVisitor): FieldAttributeAdapter {
            return FieldAttributeAdapter(visitor)
        }
    }
}