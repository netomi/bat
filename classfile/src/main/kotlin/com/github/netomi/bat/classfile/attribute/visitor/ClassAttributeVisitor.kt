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
import com.github.netomi.bat.classfile.annotation.RuntimeAnnotationsAttribute
import com.github.netomi.bat.classfile.annotation.RuntimeInvisibleAnnotationsAttribute
import com.github.netomi.bat.classfile.annotation.RuntimeVisibleAnnotationsAttribute

fun interface ClassAttributeVisitor: AnyAttributeVisitor {
    fun visitBootstrapMethodsAttribute(classFile: ClassFile, attribute: BootstrapMethodsAttribute) {
        visitAnyAttribute(classFile, attribute)
    }

    fun visitDeprecatedAttribute(classFile: ClassFile, attribute: DeprecatedAttribute) {
        visitAnyAttribute(classFile, attribute)
    }

    fun visitEnclosingMethodAttribute(classFile: ClassFile, attribute: EnclosingMethodAttribute) {
        visitAnyAttribute(classFile, attribute)
    }

    fun visitModuleMainClassAttribute(classFile: ClassFile, attribute: ModuleMainClassAttribute) {
        visitAnyAttribute(classFile, attribute)
    }

    fun visitModulePackages(classFile: ClassFile, attribute: ModulePackagesAttribute) {
        visitAnyAttribute(classFile, attribute)
    }

    fun visitNestHostAttribute(classFile: ClassFile, attribute: NestHostAttribute) {
        visitAnyAttribute(classFile, attribute)
    }

    fun visitNestMembersAttribute(classFile: ClassFile, attribute: NestMembersAttribute) {
        visitAnyAttribute(classFile, attribute)
    }

    fun visitPermittedSubclassesAttribute(classFile: ClassFile, attribute: PermittedSubclassesAttribute) {
        visitAnyAttribute(classFile, attribute)
    }

    fun visitRuntimeAnnotationsAttribute(classFile: ClassFile, attribute: RuntimeAnnotationsAttribute) {
        visitAnyAttribute(classFile, attribute)
    }

    fun visitRuntimeVisibleAnnotationsAttribute(classFile: ClassFile, attribute: RuntimeVisibleAnnotationsAttribute) {
        visitRuntimeAnnotationsAttribute(classFile, attribute)
    }

    fun visitRuntimeInvisibleAnnotationsAttribute(classFile: ClassFile, attribute: RuntimeInvisibleAnnotationsAttribute) {
        visitRuntimeAnnotationsAttribute(classFile, attribute)
    }

    fun visitSignatureAttribute(classFile: ClassFile, attribute: SignatureAttribute) {
        visitAnyAttribute(classFile, attribute)
    }

    fun visitSourceDebugExtensionAttribute(classFile: ClassFile, attribute: SourceDebugExtensionAttribute) {
        visitAnyAttribute(classFile, attribute)
    }

    fun visitSourceFileAttribute(classFile: ClassFile, attribute: SourceFileAttribute) {
        visitAnyAttribute(classFile, attribute)
    }

    fun visitSyntheticAttribute(classFile: ClassFile, attribute: SyntheticAttribute) {
        visitAnyAttribute(classFile, attribute)
    }
}