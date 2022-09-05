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

internal class ConstantPoolPrinter constructor(private val printer: IndentingPrinter): ConstantVisitor {

    private val constantPrinter = ConstantPrinter(printer)

    override fun visitAnyConstant(classFile: ClassFile, index: Int, constant: Constant) {}

    override fun visitIntegerConstant(classFile: ClassFile, index: Int, constant: IntegerConstant) {
        printer.print("%-19s ".format("Integer"))
        constant.accept(classFile, index, constantPrinter)
    }

    override fun visitLongConstant(classFile: ClassFile, index: Int, constant: LongConstant) {
        printer.print("%-19s ".format("Long"))
        constant.accept(classFile, index, constantPrinter)
    }

    override fun visitFloatConstant(classFile: ClassFile, index: Int, constant: FloatConstant) {
        printer.print("%-19s ".format("Float"))
        constant.accept(classFile, index, constantPrinter)
    }

    override fun visitDoubleConstant(classFile: ClassFile, index: Int, constant: DoubleConstant) {
        printer.print("%-19s ".format("Double"))
        constant.accept(classFile, index, constantPrinter)
    }

    override fun visitUtf8Constant(classFile: ClassFile, index: Int, constant: Utf8Constant) {
        printer.print("%-19s ".format("Utf8"))
        constant.accept(classFile, index, constantPrinter)
    }

    override fun visitStringConstant(classFile: ClassFile, index: Int, constant: StringConstant) {
        printer.print("%-19s %-15s // ".format("String", "#" + constant.stringIndex))
        constant.accept(classFile, index, constantPrinter)
    }

    override fun visitAnyRefConstant(classFile: ClassFile, index: Int, constant: RefConstant) {
        var type = "Unknown"
        when (constant.type) {
            ConstantType.FIELD_REF            -> type = "Fieldref"
            ConstantType.METHOD_REF           -> type = "Methodref"
            ConstantType.INTERFACE_METHOD_REF -> type = "InterfaceMethodref"
            else -> {
                // do nothing
            }
        }
        printer.print("%-19s %-15s // ".format(type, "#${constant.classIndex}.#${constant.nameAndTypeIndex}"))
        constant.accept(classFile, index, constantPrinter)
    }

    override fun visitClassConstant(classFile: ClassFile, index: Int, constant: ClassConstant) {
        printer.print("%-19s %-15s // ".format("Class", "#" + constant.nameIndex))
        constant.accept(classFile, index, constantPrinter)
    }

    override fun visitNameAndTypeConstant(classFile: ClassFile, index: Int, constant: NameAndTypeConstant) {
        printer.print("%-19s %-15s // ".format("NameAndType", "#" + constant.nameIndex + ".#" + constant.descriptorIndex))
        constant.accept(classFile, index, constantPrinter)
    }

    override fun visitModuleConstant(classFile: ClassFile, index: Int, constant: ModuleConstant) {
        printer.print(String.format("%-19s %-15s // ", "Module", "#" + constant.nameIndex))
        constant.accept(classFile, index, constantPrinter)
    }

    override fun visitPackageConstant(classFile: ClassFile, index: Int, constant: PackageConstant) {
        printer.print("%-19s %-15s // ".format("Class", "#" + constant.nameIndex))
        constant.accept(classFile, index, constantPrinter)
    }

    override fun visitMethodTypeConstant(classFile: ClassFile, index: Int, constant: MethodTypeConstant) {
        printer.print("%-19s %-15s // ".format("MethodType", "#" + constant.descriptorIndex))
        constant.accept(classFile, index, constantPrinter)
    }

    override fun visitMethodHandleConstant(classFile: ClassFile, index: Int, constant: MethodHandleConstant) {
        printer.print("%-19s %-15s // ".format("MethodHandle", "${constant.referenceKind.value}.#${constant.referenceIndex}"))
        constant.accept(classFile, index, constantPrinter)
    }
}