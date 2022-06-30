/*
 *  Copyright (c) 2020 Thomas Neidhart.
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
package com.github.netomi.bat.classfile.io

import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.ConstantPool
import com.github.netomi.bat.classfile.Field
import com.github.netomi.bat.classfile.constant.*
import com.github.netomi.bat.classfile.visitor.ClassFileVisitor
import com.github.netomi.bat.classfile.visitor.ConstantPoolVisitor
import com.github.netomi.bat.classfile.visitor.ConstantVisitor
import com.github.netomi.bat.classfile.visitor.MemberVisitor
import com.github.netomi.bat.io.IndentingPrinter
import com.github.netomi.bat.util.Classes
import com.github.netomi.bat.util.Strings
import java.io.OutputStreamWriter
import java.io.PrintStream
import java.io.Writer
import java.util.*

class ClassFilePrinter :
    ClassFileVisitor, ConstantPoolVisitor, ConstantVisitor, MemberVisitor
{
    private val printer: IndentingPrinter

    constructor(ps: PrintStream = System.out) : this(OutputStreamWriter(ps))

    constructor(writer: Writer) {
        this.printer = IndentingPrinter(writer, 2)
    }

    override fun visitClassFile(classFile: ClassFile) {
        printer.println("class " + classFile.externalClassName)
        printer.levelUp()
        printer.println("minor version: " + classFile.minorVersion)
        printer.println("major version: " + classFile.majorVersion)
        printer.println("flags: (0x%04x)".format(classFile.accessFlags.rawFlags))
        printer.println("this_class: #%-29d // %s".format(classFile.thisClassIndex,   classFile.className))
        printer.println("super_class: #%-28d // %s".format(classFile.superClassIndex, classFile.superClassName))
        printer.levelDown()

        classFile.constantPoolAccept(this)

        printer.println("{")

        printer.levelUp()
        classFile.fieldsAccept(this)
        printer.levelDown()

        printer.println("}")

        printer.flush()
    }

    override fun visitConstantPoolStart(classFile: ClassFile, constantPool: ConstantPool) {
        printer.println("Constant pool:")
    }

    override fun visitAnyConstant(classFile: ClassFile, constantPool: ConstantPool, index: Int, constant: Constant) {
        printer.print(String.format("%6s = ", "#$index"))
        constant.accept(classFile, this)
        printer.println()
    }

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
        val output = if (!Strings.isAsciiPrintable(constant.value)) {
            Strings.escapeString(constant.value)
        } else {
            constant.value
        }

        printer.print("%-19s %s".format("Utf8", output))
    }

    override fun visitStringConstant(classFile: ClassFile, constant: StringConstant) {
        val str = classFile.constantPool.getString(constant.stringIndex)
        printer.print("%-19s %-15s // %s".format("String", "#" + constant.stringIndex, str))
    }

    override fun visitAnyRefConstant(classFile: ClassFile, refConstant: RefConstant) {
        val cp = classFile.constantPool
        val className = cp.getClassName(refConstant.classIndex)
        val memberName = cp.getNameAndType(refConstant.nameAndTypeIndex).getMemberName(cp)
        val descriptor = cp.getNameAndType(refConstant.nameAndTypeIndex).getDescriptor(cp)
        val str = "$className.$memberName:$descriptor"
        var type = "Unknown"
        when (refConstant.type) {
            Constant.Type.FIELD_REF -> type = "Fieldref"
            Constant.Type.METHOD_REF -> type = "Methodref"
            Constant.Type.INTERFACE_METHOD_REF -> type = "InterfaceMethodref"
            else -> {
                // do nothing
            }
        }
        printer.print("%-19s %-15s // %s".format(type,
                                                 "#" + refConstant.classIndex + ".#" + refConstant.nameAndTypeIndex,
                                                 str))
    }

    override fun visitClassConstant(classFile: ClassFile, constant: ClassConstant) {
        val str = classFile.constantPool.getString(constant.nameIndex)
        printer.print("%-19s %-15s // %s".format("Class", "#" + constant.nameIndex, str))
    }

    override fun visitNameAndTypeConstant(classFile: ClassFile, constant: NameAndTypeConstant) {
        val cp = classFile.constantPool
        val memberName = cp.getString(constant.nameIndex)
        val descriptor = cp.getString(constant.descriptorIndex)
        val str = "$memberName:$descriptor"
        printer.print("%-19s %-15s // %s".format("NameAndType",
                                                 "#" + constant.nameIndex + ".#" + constant.descriptorIndex,
                                                 str))
    }

    override fun visitModuleConstant(classFile: ClassFile, constant: ModuleConstant) {
        val str = constant.getName(classFile.constantPool)
        printer.print(String.format("%-19s %-15s // %s", "Module", "#" + constant.nameIndex, str))
    }

    override fun visitPackageConstant(classFile: ClassFile, constant: PackageConstant) {
        val str = constant.getName(classFile.constantPool)
        printer.print("%-19s %-15s // %s".format("Class", "#" + constant.nameIndex, str))
    }

    override fun visitField(classFile: ClassFile, index: Int, field: Field) {
        val externalModifiers = field.accessFlags.modifiers.joinToString(" ") { txt -> txt.toString().lowercase(Locale.getDefault()) }
        val externalType = Classes.externalTypeFromType(field.descriptor(classFile))
        printer.println("%s %s %s;".format(externalModifiers, externalType, field.name(classFile)))

        printer.levelUp()

        printer.println("descriptor: %s".format(field.descriptor(classFile)))

        val modifiers = field.accessFlags.modifiers.joinToString(", ") { txt -> "ACC_$txt" }
        printer.println("flags: (0x%04x) %s".format(field.accessFlags.rawFlags, modifiers))
        printer.levelDown()
   }

}