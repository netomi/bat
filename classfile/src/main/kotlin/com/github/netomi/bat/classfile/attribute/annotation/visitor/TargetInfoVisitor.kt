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

package com.github.netomi.bat.classfile.attribute.annotation.visitor

import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.attribute.annotation.*

fun interface TargetInfoVisitor {
    fun visitAnyTargetInfo(classFile: ClassFile, targetInfo: TargetInfo)

    fun visitTypeParameterTargetInfo(classFile: ClassFile, targetInfo: TypeParameterTargetInfo) {
        visitAnyTargetInfo(classFile, targetInfo)
    }

    fun visitSuperTypeTargetInfo(classFile: ClassFile, targetInfo: SuperTypeTargetInfo) {
        visitAnyTargetInfo(classFile, targetInfo)
    }

    fun visitTypeParameterBoundTargetInfo(classFile: ClassFile, targetInfo: TypeParameterBoundTargetInfo) {
        visitAnyTargetInfo(classFile, targetInfo)
    }

    fun visitEmptyTargetInfo(classFile: ClassFile, targetInfo: EmptyTargetInfo) {
        visitAnyTargetInfo(classFile, targetInfo)
    }

    fun visitFormalParameterTargetInfo(classFile: ClassFile, targetInfo: FormalParameterTargetInfo) {
        visitAnyTargetInfo(classFile, targetInfo)
    }

    fun visitThrowsTargetInfo(classFile: ClassFile, targetInfo: ThrowsTargetInfo) {
        visitAnyTargetInfo(classFile, targetInfo)
    }

    fun visitLocalVarTargetInfo(classFile: ClassFile, targetInfo: LocalVarTargetInfo) {
        visitAnyTargetInfo(classFile, targetInfo)
    }

    fun visitCatchTargetInfo(classFile: ClassFile, targetInfo: CatchTargetInfo) {
        visitAnyTargetInfo(classFile, targetInfo)
    }

    fun visitOffsetTargetInfo(classFile: ClassFile, targetInfo: OffsetTargetInfo) {
        visitAnyTargetInfo(classFile, targetInfo)
    }

    fun visitTypeArgumentTargetInfo(classFile: ClassFile, targetInfo: TypeArgumentTargetInfo) {
        visitAnyTargetInfo(classFile, targetInfo)
    }
}