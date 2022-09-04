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
import com.github.netomi.bat.classfile.attribute.RecordAttribute
import com.github.netomi.bat.classfile.attribute.RecordComponent
import com.github.netomi.bat.classfile.attribute.SignatureAttribute
import com.github.netomi.bat.classfile.attribute.annotation.*

fun interface RecordComponentAttributeVisitor: AnyAttributeVisitor {
    fun visitRuntimeAnnotationsAttribute(classFile: ClassFile, record: RecordAttribute, component: RecordComponent, attribute: RuntimeAnnotationsAttribute) {
        visitAnyAttribute(classFile, attribute)
    }

    fun visitRuntimeVisibleAnnotationsAttribute(classFile: ClassFile, record: RecordAttribute, component: RecordComponent, attribute: RuntimeVisibleAnnotationsAttribute) {
        visitRuntimeAnnotationsAttribute(classFile, record, component, attribute)
    }

    fun visitRuntimeInvisibleAnnotationsAttribute(classFile: ClassFile, record: RecordAttribute, component: RecordComponent, attribute: RuntimeInvisibleAnnotationsAttribute) {
        visitRuntimeAnnotationsAttribute(classFile, record, component, attribute)
    }

    fun visitRuntimeTypeAnnotationsAttribute(classFile: ClassFile, record: RecordAttribute, component: RecordComponent, attribute: RuntimeTypeAnnotationsAttribute) {
        visitAnyAttribute(classFile, attribute)
    }

    fun visitRuntimeVisibleTypeAnnotationsAttribute(classFile: ClassFile, record: RecordAttribute, component: RecordComponent, attribute: RuntimeVisibleTypeAnnotationsAttribute) {
        visitRuntimeTypeAnnotationsAttribute(classFile, record, component, attribute)
    }

    fun visitRuntimeInvisibleTypeAnnotationsAttribute(classFile: ClassFile, record: RecordAttribute, component: RecordComponent, attribute: RuntimeInvisibleTypeAnnotationsAttribute) {
        visitRuntimeTypeAnnotationsAttribute(classFile, record, component, attribute)
    }

    fun visitSignatureAttribute(classFile: ClassFile, record: RecordAttribute, component: RecordComponent, attribute: SignatureAttribute) {
        visitAnyAttribute(classFile, attribute)
    }
}
