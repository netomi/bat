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
package org.netomi.bat.classfile.io

import org.netomi.bat.classfile.ClassFile
import org.netomi.bat.classfile.ClassFile.Companion.externalClassName
import org.netomi.bat.classfile.ConstantPool
import org.netomi.bat.classfile.constant.*
import org.netomi.bat.classfile.visitor.ClassFileVisitor
import org.netomi.bat.classfile.visitor.ConstantPoolVisitor
import org.netomi.bat.classfile.visitor.ConstantVisitor
import java.io.PrintStream

class ClassFilePrinter constructor(private val ps: PrintStream = System.out) : ClassFileVisitor, ConstantPoolVisitor, ConstantVisitor {
    override fun visitClassFile(classFile: ClassFile) {
        ps.println("class " + externalClassName(classFile.className))
        ps.println("  minor version: " + classFile.minorVersion)
        ps.println("  major version: " + classFile.majorVersion)
        ps.println("  flags: (0x%04x)".format(classFile.accessFlags!!.rawFlags))
        ps.println("  this_class: #%-29d // %s".format(classFile.thisClassIndex,   classFile.className))
        ps.println("  super_class: #%-28d // %s".format(classFile.superClassIndex, classFile.superClassName))
        classFile.constantPoolAccept(this)
    }

    override fun visitConstantPoolStart(classFile: ClassFile, constantPool: ConstantPool) {
        ps.println("Constant pool:")
    }

    override fun visitAnyConstant(classFile: ClassFile, constantPool: ConstantPool, index: Int, constant: Constant) {
        ps.print(String.format("%6s = ", "#$index"))
        constant.accept(classFile, this)
        ps.println()
    }

    override fun visitIntegerConstant(classFile: ClassFile, constant: IntegerConstant) {
        ps.print("%-19s %d".format("Integer", constant.value))
    }

    override fun visitLongConstant(classFile: ClassFile, constant: LongConstant) {
        ps.print("%-19s %d".format("Long", constant.value))
    }

    override fun visitFloatConstant(classFile: ClassFile, constant: FloatConstant) {
        ps.print("%-19s %f".format("Float", constant.value))
    }

    override fun visitDoubleConstant(classFile: ClassFile, constant: DoubleConstant) {
        ps.print("%-19s %f".format("Double", constant.value))
    }

    override fun visitUtf8Constant(classFile: ClassFile, constant: Utf8Constant) {
        ps.print("%-19s %s".format("Utf8", constant.value))
    }

    override fun visitStringConstant(classFile: ClassFile, constant: StringConstant) {
        val str = classFile.constantPool.getString(constant.stringIndex)
        ps.print("%-19s %-15s // %s".format("String", "#" + constant.stringIndex, str))
    }

    override fun visitAnyRefConstant(classFile: ClassFile, refConstant: RefConstant) {
        val cp = classFile.constantPool
        val className = cp.getClassName(refConstant.classIndex)
        val memberName = cp.getName(refConstant.nameAndTypeIndex)
        val descriptor = cp.getType(refConstant.nameAndTypeIndex)
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
        ps.print("%-19s %-15s // %s".format(type,
                                            "#" + refConstant.classIndex + ".#" + refConstant.nameAndTypeIndex,
                                            str))
    }

    override fun visitClassConstant(classFile: ClassFile, constant: ClassConstant) {
        val str = classFile.constantPool.getString(constant.nameIndex)
        ps.print("%-19s %-15s // %s".format("Class", "#" + constant.nameIndex, str))
    }

    override fun visitNameAndTypeConstant(classFile: ClassFile, constant: NameAndTypeConstant) {
        val cp = classFile.constantPool
        val memberName = cp.getString(constant.nameIndex)
        val descriptor = cp.getString(constant.descriptorIndex)
        val str = "$memberName:$descriptor"
        ps.print("%-19s %-15s // %s".format("NameAndType",
                                            "#" + constant.nameIndex + ".#" + constant.descriptorIndex,
                                            str))
    }

    override fun visitModuleConstant(classFile: ClassFile, constant: ModuleConstant) {
        val str = constant.getName(classFile.constantPool)
        ps.print(String.format("%-19s %-15s // %s", "Module", "#" + constant.nameIndex, str))
    }

    override fun visitPackageConstant(classFile: ClassFile, constant: PackageConstant) {
        val str = constant.getName(classFile.constantPool)
        ps.print("%-19s %-15s // %s".format("Class", "#" + constant.nameIndex, str))
    }

}