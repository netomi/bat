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
import com.github.netomi.bat.classfile.attribute.annotation.*
import com.github.netomi.bat.classfile.attribute.module.*

fun interface ClassAttributeVisitor: AnyAttributeVisitor {
    fun visitBootstrapMethods(classFile: ClassFile, attribute: BootstrapMethodsAttribute) {
        visitAnyAttribute(classFile, attribute)
    }

    fun visitDeprecated(classFile: ClassFile, attribute: DeprecatedAttribute) {
        visitAnyAttribute(classFile, attribute)
    }

    fun visitEnclosingMethod(classFile: ClassFile, attribute: EnclosingMethodAttribute) {
        visitAnyAttribute(classFile, attribute)
    }

    fun visitInnerClasses(classFile: ClassFile, attribute: InnerClassesAttribute) {
        visitAnyAttribute(classFile, attribute)
    }

    fun visitModule(classFile: ClassFile, attribute: ModuleAttribute) {
        visitAnyAttribute(classFile, attribute)
    }

    fun visitModuleMainClass(classFile: ClassFile, attribute: ModuleMainClassAttribute) {
        visitAnyAttribute(classFile, attribute)
    }

    fun visitModulePackages(classFile: ClassFile, attribute: ModulePackagesAttribute) {
        visitAnyAttribute(classFile, attribute)
    }

    fun visitModuleHashes(classFile: ClassFile, attribute: ModuleHashesAttribute) {
        visitAnyAttribute(classFile, attribute)
    }

    fun visitModuleTarget(classFile: ClassFile, attribute: ModuleTargetAttribute) {
        visitAnyAttribute(classFile, attribute)
    }

    fun visitNestHost(classFile: ClassFile, attribute: NestHostAttribute) {
        visitAnyAttribute(classFile, attribute)
    }

    fun visitNestMembers(classFile: ClassFile, attribute: NestMembersAttribute) {
        visitAnyAttribute(classFile, attribute)
    }

    fun visitPermittedSubclasses(classFile: ClassFile, attribute: PermittedSubclassesAttribute) {
        visitAnyAttribute(classFile, attribute)
    }

    fun visitRecord(classFile: ClassFile, attribute: RecordAttribute) {
        visitAnyAttribute(classFile, attribute)
    }

    fun visitRuntimeAnnotations(classFile: ClassFile, attribute: RuntimeAnnotationsAttribute) {
        visitAnyAttribute(classFile, attribute)
    }

    fun visitRuntimeVisibleAnnotations(classFile: ClassFile, attribute: RuntimeVisibleAnnotationsAttribute) {
        visitRuntimeAnnotations(classFile, attribute)
    }

    fun visitRuntimeInvisibleAnnotations(classFile: ClassFile, attribute: RuntimeInvisibleAnnotationsAttribute) {
        visitRuntimeAnnotations(classFile, attribute)
    }

    fun visitRuntimeTypeAnnotations(classFile: ClassFile, attribute: RuntimeTypeAnnotationsAttribute) {
        visitAnyAttribute(classFile, attribute)
    }

    fun visitRuntimeVisibleTypeAnnotations(classFile: ClassFile, attribute: RuntimeVisibleTypeAnnotationsAttribute) {
        visitRuntimeTypeAnnotations(classFile, attribute)
    }

    fun visitRuntimeInvisibleTypeAnnotations(classFile: ClassFile, attribute: RuntimeInvisibleTypeAnnotationsAttribute) {
        visitRuntimeTypeAnnotations(classFile, attribute)
    }

    fun visitSignature(classFile: ClassFile, attribute: SignatureAttribute) {
        visitAnyAttribute(classFile, attribute)
    }

    fun visitSourceDebugExtension(classFile: ClassFile, attribute: SourceDebugExtensionAttribute) {
        visitAnyAttribute(classFile, attribute)
    }

    fun visitSourceFile(classFile: ClassFile, attribute: SourceFileAttribute) {
        visitAnyAttribute(classFile, attribute)
    }

    fun visitSynthetic(classFile: ClassFile, attribute: SyntheticAttribute) {
        visitAnyAttribute(classFile, attribute)
    }
}