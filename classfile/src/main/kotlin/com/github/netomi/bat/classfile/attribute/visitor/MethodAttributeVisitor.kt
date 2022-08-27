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
import com.github.netomi.bat.classfile.annotation.RuntimeAnnotationsAttribute
import com.github.netomi.bat.classfile.annotation.RuntimeInvisibleAnnotationsAttribute
import com.github.netomi.bat.classfile.annotation.RuntimeVisibleAnnotationsAttribute

fun interface MethodAttributeVisitor: AnyAttributeVisitor {

    fun visitCodeAttribute(classFile: ClassFile, method: Method, attribute: CodeAttribute) {
        visitAnyAttribute(classFile, attribute)
    }

    fun visitDeprecatedAttribute(classFile: ClassFile, method: Method, attribute: DeprecatedAttribute) {
        visitAnyAttribute(classFile, attribute)
    }

    fun visitExceptionsAttribute(classFile: ClassFile, method: Method, attribute: ExceptionsAttribute) {
        visitAnyAttribute(classFile, attribute)
    }

    fun visitMethodParametersAttribute(classFile: ClassFile, method: Method, attribute: MethodParametersAttribute) {
        visitAnyAttribute(classFile, attribute)
    }

    fun visitRuntimeAnnotationsAttribute(classFile: ClassFile, method: Method, attribute: RuntimeAnnotationsAttribute) {
        visitAnyAttribute(classFile, attribute)
    }

    fun visitRuntimeVisibleAnnotationsAttribute(classFile: ClassFile, method: Method, attribute: RuntimeVisibleAnnotationsAttribute) {
        visitRuntimeAnnotationsAttribute(classFile, method, attribute)
    }

    fun visitRuntimeInvisibleAnnotationsAttribute(classFile: ClassFile, method: Method, attribute: RuntimeInvisibleAnnotationsAttribute) {
        visitRuntimeAnnotationsAttribute(classFile, method, attribute)
    }

    fun visitSignatureAttribute(classFile: ClassFile, method: Method, attribute: SignatureAttribute) {
        visitAnyAttribute(classFile, attribute)
    }

    fun visitSyntheticAttribute(classFile: ClassFile, method: Method, attribute: SyntheticAttribute) {
        visitAnyAttribute(classFile, attribute)
    }
}
