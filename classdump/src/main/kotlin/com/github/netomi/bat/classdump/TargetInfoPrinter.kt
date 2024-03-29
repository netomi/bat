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

package com.github.netomi.bat.classdump

import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.attribute.annotation.*
import com.github.netomi.bat.classfile.attribute.annotation.visitor.TargetInfoVisitor
import com.github.netomi.bat.io.IndentingPrinter

internal class TargetInfoPrinter constructor(private val printer: IndentingPrinter): TargetInfoVisitor {

    override fun visitAnyTargetInfo(classFile: ClassFile, targetInfo: TargetInfo) {}

    override fun visitCatchTargetInfo(classFile: ClassFile, targetInfo: CatchTargetInfo) {
        printer.print(", exception_index=${targetInfo.exceptionTableIndex}")
    }

    override fun visitTypeParameterTargetInfo(classFile: ClassFile, targetInfo: TypeParameterTargetInfo) {
        printer.print(", type_index=${targetInfo.typeParameterIndex}")
    }

    override fun visitThrowsTargetInfo(classFile: ClassFile, targetInfo: ThrowsTargetInfo) {
        printer.print(", type_index=${targetInfo.throwsTypeIndex}")
    }

    override fun visitOffsetTargetInfo(classFile: ClassFile, targetInfo: OffsetTargetInfo) {
        printer.print(", offset=${targetInfo.offset}")
    }

    override fun visitLocalVarTargetInfo(classFile: ClassFile, targetInfo: LocalVarTargetInfo) {
        val localVarInfo = targetInfo.joinToString(separator = ",", transform = {
            "{start_pc=${it.startPC}, length=${it.length}, index=${it.variableIndex}}"
        })
        printer.print(", $localVarInfo")
    }

    override fun visitTypeArgumentTargetInfo(classFile: ClassFile, targetInfo: TypeArgumentTargetInfo) {
        printer.print(", offset=${targetInfo.offset}, type_index=${targetInfo.typeArgumentIndex}")
    }

    override fun visitSuperTypeTargetInfo(classFile: ClassFile, targetInfo: SuperTypeTargetInfo) {
        printer.print(", type_index=${targetInfo.superTypeIndex}")
    }

    override fun visitTypeParameterBoundTargetInfo(classFile: ClassFile, targetInfo: TypeParameterBoundTargetInfo) {
        printer.print(", param_index=${targetInfo.typeParameterIndex}, bound_index=${targetInfo.boundIndex}")
    }

    override fun visitFormalParameterTargetInfo(classFile: ClassFile, targetInfo: FormalParameterTargetInfo) {
        printer.print(", param_index=${targetInfo.formalParameterIndex}")
    }

    override fun visitEmptyTargetInfo(classFile: ClassFile, targetInfo: EmptyTargetInfo) {}
}