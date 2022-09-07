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

    override fun visitAnyConstant(classFile: ClassFile, index: Int, constant: Constant) {
        TODO("implement")
    }

    override fun visitIntegerConstant(classFile: ClassFile, index: Int, constant: IntegerConstant) {
        if (printConstantType) {
            printer.print("int ")
        }
        printer.print(constant.value)
    }

    override fun visitLongConstant(classFile: ClassFile, index: Int, constant: LongConstant) {
        if (printConstantType) {
            printer.print("long ")
        }
        printer.print(constant.value)
        printer.print("l")
    }

    override fun visitFloatConstant(classFile: ClassFile, index: Int, constant: FloatConstant) {
        if (printConstantType) {
            printer.print("float ")
        }
        printer.print("%f".format(constant.value))
    }

    override fun visitDoubleConstant(classFile: ClassFile, index: Int, constant: DoubleConstant) {
        if (printConstantType) {
            printer.print("double ")
        }
        printer.print("%f".format(constant.value))
    }

    override fun visitUtf8Constant(classFile: ClassFile, index: Int, constant: Utf8Constant) {
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

    override fun visitStringConstant(classFile: ClassFile, index: Int, constant: StringConstant) {
        visitUtf8Constant(classFile, index, classFile.getUtf8Constant(constant.stringIndex))
    }

    override fun visitAnyRefConstant(classFile: ClassFile, index: Int, constant: RefConstant) {
        val str = buildString {
            val className = constant.getClassName(classFile)
            if (alwaysIncludeClassName || className != classFile.className) {
                append(className)
                append(".")
            }
            val memberName = constant.getMemberName(classFile)
            if (memberName.contains("<")) {
                append("\"$memberName\"")
            } else {
                append(memberName)
            }
            append(":")
            val descriptor = constant.getDescriptor(classFile)
            append(descriptor)
        }

        if (printConstantType) {
            val type = when (constant) {
                is FieldrefConstant           -> "Field"
                is MethodrefConstant          -> "Method"
                is InterfaceMethodrefConstant -> "InterfaceMethod"
                else -> error("unexpected constant '$constant'")
            }
            printer.print("$type ")
        }
        printer.print(str)
    }

    override fun visitClassConstant(classFile: ClassFile, index: Int, constant: ClassConstant) {
        if (printConstantType) {
            printer.print("class ")
        }
        val className = constant.getClassName(classFile)
        if (className.isArrayClass) {
            printer.print("\"$className\"")
        } else {
            printer.print(className)
        }
    }

    override fun visitNameAndTypeConstant(classFile: ClassFile, index: Int, constant: NameAndTypeConstant) {
        val memberName = classFile.getString(constant.nameIndex)
        val descriptor = classFile.getString(constant.descriptorIndex)

        if (memberName.contains("<")) {
            printer.print("\"$memberName\":$descriptor")
        } else {
            printer.print("$memberName:$descriptor")
        }
    }

    override fun visitMethodTypeConstant(classFile: ClassFile, index: Int, constant: MethodTypeConstant) {
        visitUtf8Constant(classFile, index, classFile.getUtf8Constant(constant.descriptorIndex))
    }

    override fun visitMethodHandleConstant(classFile: ClassFile, index: Int, constant: MethodHandleConstant) {
        printer.print("${constant.referenceKind.simpleName} ")
        constant.referenceAccept(classFile, this)
    }

    override fun visitInvokeDynamicConstant(classFile: ClassFile, index: Int, constant: InvokeDynamicConstant) {
        val nameAndType = classFile.getNameAndType(constant.nameAndTypeIndex)
        val name = nameAndType.getMemberName(classFile)
        val type = nameAndType.getDescriptor(classFile)

        printer.print("#${constant.bootstrapMethodAttrIndex}:${name}:${type}")
    }

    override fun visitModuleConstant(classFile: ClassFile, index: Int, constant: ModuleConstant) {
        val moduleName = constant.getModuleName(classFile)
        if (moduleName.contains(".")) {
            printer.print("\"${moduleName}\"")
        } else {
            printer.print(moduleName)
        }
    }

    override fun visitPackageConstant(classFile: ClassFile, index: Int, constant: PackageConstant) {
        printer.print(constant.getPackageName(classFile))
    }
}