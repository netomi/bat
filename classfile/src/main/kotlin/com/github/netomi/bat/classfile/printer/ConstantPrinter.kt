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
import com.github.netomi.bat.classfile.constant.visitor.ConstantVisitor
import com.github.netomi.bat.io.IndentingPrinter
import com.github.netomi.bat.util.escapeAsJavaString
import com.github.netomi.bat.util.isAsciiPrintable

internal class ConstantPrinter constructor(private val printer:                IndentingPrinter,
                                           private val printConstantType:      Boolean = false,
                                           private val alwaysIncludeClassName: Boolean = true): ConstantVisitor {

    override fun visitAnyConstant(classFile: ClassFile, constant: Constant) {
        printer.print(constant)
    }

    override fun visitIntegerConstant(classFile: ClassFile, constant: IntegerConstant) {
        if (printConstantType) {
            printer.print("int ")
        }
        printer.print(constant.value)
    }

    override fun visitLongConstant(classFile: ClassFile, constant: LongConstant) {
        if (printConstantType) {
            printer.print("long ")
        }
        printer.print(constant.value)
    }

    override fun visitFloatConstant(classFile: ClassFile, constant: FloatConstant) {
        if (printConstantType) {
            printer.print("float ")
        }
        printer.print("%f".format(constant.value))
    }

    override fun visitDoubleConstant(classFile: ClassFile, constant: DoubleConstant) {
        if (printConstantType) {
            printer.print("double ")
        }
        printer.print("%f".format(constant.value))
    }

    override fun visitUtf8Constant(classFile: ClassFile, constant: Utf8Constant) {
        val output = if (!constant.value.isAsciiPrintable()) {
            constant.value.escapeAsJavaString()
        } else {
            constant.value
        }

        if (printConstantType) {
            printer.print("String ")
        }
        printer.print(output)
    }

    override fun visitStringConstant(classFile: ClassFile, constant: StringConstant) {
        visitUtf8Constant(classFile, classFile.getUtf8Constant(constant.stringIndex))
    }

    override fun visitAnyRefConstant(classFile: ClassFile, refConstant: RefConstant) {
        val str = buildString {
            val className = refConstant.getClassName(classFile)
            if (alwaysIncludeClassName || className != classFile.className) {
                append(className)
                append(".")
            }
            val memberName = refConstant.getMemberName(classFile)
            append(memberName)
            append(":")
            val descriptor = refConstant.getDescriptor(classFile)
            append(descriptor)
        }

        if (printConstantType) {
            val type = when (refConstant) {
                is FieldrefConstant           -> "Field"
                is MethodrefConstant          -> "Method"
                is InterfaceMethodrefConstant -> "InterfaceMethod"
                else -> error("unexpected constant '$refConstant'")
            }
            printer.print("$type ")
        }
        printer.print(str)
    }

    override fun visitClassConstant(classFile: ClassFile, constant: ClassConstant) {
        if (printConstantType) {
            printer.print("class ")
        }
        printer.print(constant.getClassName(classFile))
    }

    override fun visitNameAndTypeConstant(classFile: ClassFile, constant: NameAndTypeConstant) {
        val memberName = classFile.getString(constant.nameIndex)
        val descriptor = classFile.getString(constant.descriptorIndex)
        printer.print("$memberName:$descriptor")
    }

    override fun visitMethodTypeConstant(classFile: ClassFile, constant: MethodTypeConstant) {
        visitUtf8Constant(classFile, classFile.getUtf8Constant(constant.descriptorIndex))
    }

    override fun visitMethodHandleConstant(classFile: ClassFile, constant: MethodHandleConstant) {
        printer.print("${constant.referenceKind.simpleName} ")
        constant.referenceAccept(classFile, this)
    }

    override fun visitModuleConstant(classFile: ClassFile, constant: ModuleConstant) {
        val moduleName = constant.getModuleName(classFile)
        if (moduleName.contains(".")) {
            printer.print("\"${moduleName}\"")
        } else {
            printer.print(moduleName)
        }
    }

    override fun visitPackageConstant(classFile: ClassFile, constant: PackageConstant) {
        printer.print(constant.getPackageName(classFile))
    }
}