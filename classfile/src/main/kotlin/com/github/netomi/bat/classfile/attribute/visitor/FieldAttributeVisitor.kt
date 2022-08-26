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
import com.github.netomi.bat.classfile.attribute.*
import com.github.netomi.bat.classfile.attribute.annotations.RuntimeAnnotationsAttribute
import com.github.netomi.bat.classfile.attribute.annotations.RuntimeInvisibleAnnotationsAttribute
import com.github.netomi.bat.classfile.attribute.annotations.RuntimeVisibleAnnotationsAttribute

fun interface FieldAttributeVisitor: AnyAttributeVisitor {

    fun visitConstantValueAttribute(classFile: ClassFile, field: Field, attribute: ConstantValueAttribute) {
        visitAnyAttribute(classFile, attribute)
    }

    fun visitDeprecatedAttribute(classFile: ClassFile, field: Field, attribute: DeprecatedAttribute) {
        visitAnyAttribute(classFile, attribute)
    }

    fun visitRuntimeAnnotationsAttribute(classFile: ClassFile, field: Field, attribute: RuntimeAnnotationsAttribute) {
        visitAnyAttribute(classFile, attribute)
    }

    fun visitRuntimeVisibleAnnotationsAttribute(classFile: ClassFile, field: Field, attribute: RuntimeVisibleAnnotationsAttribute) {
        visitRuntimeAnnotationsAttribute(classFile, field, attribute)
    }

    fun visitRuntimeInvisibleAnnotationsAttribute(classFile: ClassFile, field: Field, attribute: RuntimeInvisibleAnnotationsAttribute) {
        visitRuntimeAnnotationsAttribute(classFile, field, attribute)
    }

    fun visitSignatureAttribute(classFile: ClassFile, field: Field, attribute: SignatureAttribute) {
        visitAnyAttribute(classFile, attribute)
    }

    fun visitSyntheticAttribute(classFile: ClassFile, field: Field, attribute: SyntheticAttribute) {
        visitAnyAttribute(classFile, attribute)
    }
}
