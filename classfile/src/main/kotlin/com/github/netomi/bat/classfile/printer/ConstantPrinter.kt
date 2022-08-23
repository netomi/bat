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

internal class ConstantPrinter constructor(private val printer: IndentingPrinter): ConstantVisitor {

    override fun visitAnyConstant(classFile: ClassFile, constant: Constant) {}

    override fun visitIntegerConstant(classFile: ClassFile, constant: IntegerConstant) {
        printer.print("%-19s %d".format("Integer", constant.value))
    }

    override fun visitLongConstant(classFile: ClassFile, constant: LongConstant) {
        printer.print("%-19s %d".format("Long", constant.value))
    }

    override fun visitFloatConstant(classFile: ClassFile, constant: FloatConstant) {
        printer.print("%-19s %f".format("Float", constant.value))
    }

    override fun visitDoubleConstant(classFile: ClassFile, constant: DoubleConstant) {
        printer.print("%-19s %f".format("Double", constant.value))
    }

    override fun visitUtf8Constant(classFile: ClassFile, constant: Utf8Constant) {
        val output = if (!constant.value.isAsciiPrintable()) {
            constant.value.escapeAsJavaString()
        } else {
            constant.value
        }

        printer.print("%-19s %s".format("Utf8", output))
    }

    override fun visitStringConstant(classFile: ClassFile, constant: StringConstant) {
        printer.print("%-19s %-15s // %s".format("String", "#" + constant.stringIndex, constant.getString(classFile)))
    }

    override fun visitAnyRefConstant(classFile: ClassFile, refConstant: RefConstant) {
        val className  = refConstant.getClassName(classFile)
        val memberName = refConstant.getMemberName(classFile)
        val descriptor = refConstant.getDescriptor(classFile)

        val str = "$className.$memberName:$descriptor"
        var type = "Unknown"
        when (refConstant.type) {
            ConstantType.FIELD_REF            -> type = "Fieldref"
            ConstantType.METHOD_REF           -> type = "Methodref"
            ConstantType.INTERFACE_METHOD_REF -> type = "InterfaceMethodref"
            else -> {
                // do nothing
            }
        }
        printer.print("%-19s %-15s // %s".format(type, "#${refConstant.classIndex}.#${refConstant.nameAndTypeIndex}", str))
    }

    override fun visitClassConstant(classFile: ClassFile, constant: ClassConstant) {
        printer.print("%-19s %-15s // %s".format("Class", "#" + constant.nameIndex, constant.getClassName(classFile)))
    }

    override fun visitNameAndTypeConstant(classFile: ClassFile, constant: NameAndTypeConstant) {
        val memberName = classFile.getString(constant.nameIndex)
        val descriptor = classFile.getString(constant.descriptorIndex)
        val str = "$memberName:$descriptor"
        printer.print("%-19s %-15s // %s".format("NameAndType",
            "#" + constant.nameIndex + ".#" + constant.descriptorIndex,
            str))
    }

    override fun visitModuleConstant(classFile: ClassFile, constant: ModuleConstant) {
        printer.print(String.format("%-19s %-15s // %s", "Module", "#" + constant.nameIndex, constant.getModuleName(classFile)))
    }

    override fun visitPackageConstant(classFile: ClassFile, constant: PackageConstant) {
        printer.print("%-19s %-15s // %s".format("Class", "#" + constant.nameIndex, constant.getPackageName(classFile)))
    }
}