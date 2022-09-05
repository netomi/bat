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

package com.github.netomi.bat.classfile.printer

import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.attribute.annotation.TargetInfo
import com.github.netomi.bat.classfile.attribute.annotation.TypeParameterBoundTargetInfo
import com.github.netomi.bat.classfile.attribute.annotation.visitor.TargetInfoVisitor
import com.github.netomi.bat.io.IndentingPrinter

internal class TargetInfoPrinter constructor(private val printer: IndentingPrinter): TargetInfoVisitor {

    override fun visitAnyTargetInfo(classFile: ClassFile, targetInfo: TargetInfo) {
        TODO("implement")
    }

    override fun visitTypeParameterBoundTargetInfo(classFile: ClassFile, targetInfo: TypeParameterBoundTargetInfo) {
        printer.print(", param_index=${targetInfo.typeParameterIndex}, bound_index=${targetInfo.boundIndex}")
    }
}