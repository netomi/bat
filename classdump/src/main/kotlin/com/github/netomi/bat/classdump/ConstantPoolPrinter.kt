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
import com.github.netomi.bat.classfile.constant.*
import com.github.netomi.bat.classfile.constant.ConstantType
import com.github.netomi.bat.classfile.constant.visitor.ConstantVisitor
import com.github.netomi.bat.io.IndentingPrinter

internal class ConstantPoolPrinter constructor(private val printer:         IndentingPrinter,
                                               private val constantPrinter: ConstantPrinter): ConstantVisitor {

    override fun visitAnyConstant(classFile: ClassFile, index: Int, constant: Constant) {
        TODO("implement printing of ${constant.type}")
    }

    override fun visitIntegerConstant(classFile: ClassFile, index: Int, constant: IntegerConstant) {
        printer.print("%-18s ".format("Integer"))
        constant.accept(classFile, index, constantPrinter)
    }

    override fun visitLongConstant(classFile: ClassFile, index: Int, constant: LongConstant) {
        printer.print("%-18s ".format("Long"))
        constant.accept(classFile, index, constantPrinter)
    }

    override fun visitFloatConstant(classFile: ClassFile, index: Int, constant: FloatConstant) {
        printer.print("%-18s ".format("Float"))
        constant.accept(classFile, index, constantPrinter)
    }

    override fun visitDoubleConstant(classFile: ClassFile, index: Int, constant: DoubleConstant) {
        printer.print("%-18s ".format("Double"))
        constant.accept(classFile, index, constantPrinter)
    }

    override fun visitUtf8Constant(classFile: ClassFile, index: Int, constant: Utf8Constant) {
        printer.print("%-18s ".format("Utf8"))
        constant.accept(classFile, index, constantPrinter)
    }

    override fun visitStringConstant(classFile: ClassFile, index: Int, constant: StringConstant) {
        printer.print("%-18s %s".format("String", "#" + constant.stringIndex))
        printer.padToPosition(42)
        printer.print("// ")
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
        printer.print("%-18s %s".format(type, "#${constant.classIndex}.#${constant.nameAndTypeIndex}"))
        printer.padToPosition(42)
        printer.print("// ")
        constant.accept(classFile, index, constantPrinter)
    }

    override fun visitClassConstant(classFile: ClassFile, index: Int, constant: ClassConstant) {
        printer.print("%-18s %s".format("Class", "#" + constant.nameIndex))
        printer.padToPosition(42)
        printer.print("// ")
        constant.accept(classFile, index, constantPrinter)
    }

    override fun visitNameAndTypeConstant(classFile: ClassFile, index: Int, constant: NameAndTypeConstant) {
        printer.print("%-18s %s".format("NameAndType", "#" + constant.nameIndex + ":#" + constant.descriptorIndex))
        printer.padToPosition(42)
        printer.print("// ")
        constant.accept(classFile, index, constantPrinter)
    }

    override fun visitModuleConstant(classFile: ClassFile, index: Int, constant: ModuleConstant) {
        printer.print(String.format("%-18s %s", "Module", "#" + constant.nameIndex))
        printer.padToPosition(42)
        printer.print("// ")
        constant.accept(classFile, index, constantPrinter)
    }

    override fun visitPackageConstant(classFile: ClassFile, index: Int, constant: PackageConstant) {
        printer.print("%-18s %s".format("Package", "#" + constant.nameIndex))
        printer.padToPosition(42)
        printer.print("// ")
        constant.accept(classFile, index, constantPrinter)
    }

    override fun visitMethodTypeConstant(classFile: ClassFile, index: Int, constant: MethodTypeConstant) {
        printer.print("%-18s %s".format("MethodType", "#" + constant.descriptorIndex))
        printer.padToPosition(42)
        printer.print("//  ")
        constant.accept(classFile, index, constantPrinter)
    }

    override fun visitMethodHandleConstant(classFile: ClassFile, index: Int, constant: MethodHandleConstant) {
        printer.print("%-18s %s".format("MethodHandle", "${constant.referenceKind.value}:#${constant.referenceIndex}"))
        printer.padToPosition(42)
        printer.print("// ")
        constant.accept(classFile, index, constantPrinter)
    }

    override fun visitInvokeDynamicConstant(classFile: ClassFile, index: Int, constant: InvokeDynamicConstant) {
        printer.print("%-18s %s".format("InvokeDynamic", "#${constant.bootstrapMethodAttrIndex}:#${constant.nameAndTypeIndex}"))
        printer.padToPosition(42)
        printer.print("// ")
        constant.accept(classFile, index, constantPrinter)
    }
}