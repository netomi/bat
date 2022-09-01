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
import com.github.netomi.bat.classfile.constant.*
import com.github.netomi.bat.classfile.constant.ConstantType
import com.github.netomi.bat.classfile.constant.visitor.ConstantVisitor
import com.github.netomi.bat.io.IndentingPrinter
import com.github.netomi.bat.util.escapeAsJavaString
import com.github.netomi.bat.util.isAsciiPrintable

internal class ConstantPoolPrinter constructor(private val printer: IndentingPrinter): ConstantVisitor {

    private val constantPrinter = ConstantPrinter(printer)

    override fun visitAnyConstant(classFile: ClassFile, constant: Constant) {}

    override fun visitIntegerConstant(classFile: ClassFile, constant: IntegerConstant) {
        printer.print("%-19s ".format("Integer"))
        constant.accept(classFile, constantPrinter)
    }

    override fun visitLongConstant(classFile: ClassFile, constant: LongConstant) {
        printer.print("%-19s ".format("Long"))
        constant.accept(classFile, constantPrinter)
    }

    override fun visitFloatConstant(classFile: ClassFile, constant: FloatConstant) {
        printer.print("%-19s ".format("Float"))
        constant.accept(classFile, constantPrinter)
    }

    override fun visitDoubleConstant(classFile: ClassFile, constant: DoubleConstant) {
        printer.print("%-19s ".format("Double"))
        constant.accept(classFile, constantPrinter)
    }

    override fun visitUtf8Constant(classFile: ClassFile, constant: Utf8Constant) {
        printer.print("%-19s ".format("Utf8"))
        constant.accept(classFile, constantPrinter)
    }

    override fun visitStringConstant(classFile: ClassFile, constant: StringConstant) {
        printer.print("%-19s %-15s // ".format("String", "#" + constant.stringIndex))
        constant.accept(classFile, constantPrinter)
    }

    override fun visitAnyRefConstant(classFile: ClassFile, refConstant: RefConstant) {
        var type = "Unknown"
        when (refConstant.type) {
            ConstantType.FIELD_REF            -> type = "Fieldref"
            ConstantType.METHOD_REF           -> type = "Methodref"
            ConstantType.INTERFACE_METHOD_REF -> type = "InterfaceMethodref"
            else -> {
                // do nothing
            }
        }
        printer.print("%-19s %-15s // ".format(type, "#${refConstant.classIndex}.#${refConstant.nameAndTypeIndex}"))
        refConstant.accept(classFile, constantPrinter)
    }

    override fun visitClassConstant(classFile: ClassFile, constant: ClassConstant) {
        printer.print("%-19s %-15s // ".format("Class", "#" + constant.nameIndex))
        constant.accept(classFile, constantPrinter)
    }

    override fun visitNameAndTypeConstant(classFile: ClassFile, constant: NameAndTypeConstant) {
        printer.print("%-19s %-15s // ".format("NameAndType", "#" + constant.nameIndex + ".#" + constant.descriptorIndex))
        constant.accept(classFile, constantPrinter)
    }

    override fun visitModuleConstant(classFile: ClassFile, constant: ModuleConstant) {
        printer.print(String.format("%-19s %-15s // ", "Module", "#" + constant.nameIndex))
        constant.accept(classFile, constantPrinter)
    }

    override fun visitPackageConstant(classFile: ClassFile, constant: PackageConstant) {
        printer.print("%-19s %-15s // ".format("Class", "#" + constant.nameIndex))
        constant.accept(classFile, constantPrinter)
    }

    override fun visitMethodTypeConstant(classFile: ClassFile, constant: MethodTypeConstant) {
        printer.print("%-19s %-15s // ".format("MethodType", "#" + constant.descriptorIndex))
        constant.accept(classFile, constantPrinter)
    }

    override fun visitMethodHandleConstant(classFile: ClassFile, constant: MethodHandleConstant) {
        printer.print("%-19s %-15s // ".format("MethodHandle", "${constant.referenceKind.value}.#${constant.referenceIndex}"))
        constant.accept(classFile, constantPrinter)
    }
}