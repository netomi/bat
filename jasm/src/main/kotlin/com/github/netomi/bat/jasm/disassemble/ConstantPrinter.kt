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

package com.github.netomi.bat.jasm.disassemble

import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.constant.*
import com.github.netomi.bat.classfile.constant.visitor.ConstantVisitor
import com.github.netomi.bat.io.IndentingPrinter
import com.github.netomi.bat.util.escapeAsJavaString

internal class ConstantPrinter constructor(private val printer: IndentingPrinter): ConstantVisitor {

    override fun visitAnyConstant(classFile: ClassFile, index: Int, constant: Constant) {
        //TODO("Not yet implemented")
    }

    override fun visitIntegerConstant(classFile: ClassFile, index: Int, constant: IntegerConstant) {
        val v = constant.value
        if (v < 0) {
            printer.print("-0x%x".format(-v))
        } else {
            printer.print("0x%x".format(v))
        }
    }

    override fun visitLongConstant(classFile: ClassFile, index: Int, constant: LongConstant) {
        val v = constant.value
        if (v < 0) {
            printer.print("-0x%xL".format(-v))
        } else {
            printer.print("0x%xL".format(v))
        }
    }

    override fun visitFloatConstant(classFile: ClassFile, index: Int, constant: FloatConstant) {
        printer.print("${constant.value}f")
    }

    override fun visitDoubleConstant(classFile: ClassFile, index: Int, constant: DoubleConstant) {
        printer.print("${constant.value}")
    }

    override fun visitStringConstant(classFile: ClassFile, index: Int, constant: StringConstant) {
        classFile.constantAccept(constant.stringIndex, this)
    }

    override fun visitUtf8Constant(classFile: ClassFile, index: Int, constant: Utf8Constant) {
        printer.print("\"${constant.value.escapeAsJavaString()}\"")
    }

    override fun visitFieldRefConstant(classFile: ClassFile, index: Int, constant: FieldrefConstant) {
        printer.print("${constant.getClassName(classFile)}->${constant.getMemberName(classFile)}:${constant.getDescriptor(classFile)}")
    }

    override fun visitMethodRefConstant(classFile: ClassFile, index: Int, constant: MethodrefConstant) {
        printer.print("${constant.getClassName(classFile)}->${constant.getMemberName(classFile)}${constant.getDescriptor(classFile)}")
    }

    override fun visitInterfaceMethodRefConstant(classFile: ClassFile, index: Int, constant: InterfaceMethodrefConstant) {
        printer.print("${constant.getClassName(classFile)}->${constant.getMemberName(classFile)}${constant.getDescriptor(classFile)}")
    }
}