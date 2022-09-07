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

import com.github.netomi.bat.classfile.*
import com.github.netomi.bat.classfile.visitor.ClassFileVisitor
import com.github.netomi.bat.classfile.visitor.MemberVisitor
import com.github.netomi.bat.io.IndentingPrinter
import com.github.netomi.bat.util.asJvmType
import com.github.netomi.bat.util.parseDescriptorToJvmTypes
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.Writer
import java.util.*

class ClassFilePrinter : ClassFileVisitor, MemberVisitor
{
    private val printer:             IndentingPrinter
    private val attributePrinter:    AttributePrinter
    private val constantPoolPrinter: ConstantPoolPrinter

    private var methodCount: Int = 0

    constructor(os: OutputStream = System.out) : this(OutputStreamWriter(os))

    constructor(writer: Writer) {
        this.printer             = IndentingPrinter(writer, 2)
        this.attributePrinter    = AttributePrinter(printer)
        this.constantPoolPrinter = ConstantPoolPrinter(printer)
    }

    override fun visitClassFile(classFile: ClassFile) {

        val externalModifiers = classFile.modifiers.filter { it != AccessFlag.SUPER }
                                                   .joinToString(" ") { it.toString().lowercase(Locale.getDefault()) }
        if (externalModifiers.isNotEmpty()) {
            printer.print("$externalModifiers ")
        }
        printer.print("class %s".format(classFile.className.toExternalClassName()))

        val superClassName = classFile.superClassName
        if (superClassName != null) {
            printer.print(" extends ${superClassName.toExternalClassName()}")
        }

        if (classFile.interfaces.isNotEmpty()) {
            val interfaceString = classFile.interfaces.joinToString(separator = ", ", transform = { it.toExternalClassName() })
            printer.print(" implements $interfaceString")
        }

        printer.println()
        printer.levelUp()
        printer.println("minor version: " + classFile.minorVersion)
        printer.println("major version: " + classFile.majorVersion)

        val modifiers = classFile.modifiers.joinToString(", ") { txt -> "ACC_$txt" }
        printer.println("flags: (0x%04x) %s".format(classFile.accessFlags, modifiers))

        printer.println("this_class: #%-26d // %s".format(classFile.thisClassIndex,   classFile.className))
        if (classFile.superClassIndex > 0) {
            printer.println("super_class: #%-25d // %s".format(classFile.superClassIndex, classFile.superClassName))
        } else {
            printer.println("super_class: #%-25d".format(classFile.superClassIndex))
        }

        printer.println("interfaces: %d, fields: %d, methods: %d, attributes: %d"
            .format(classFile.interfaces.count(),
                    classFile.fields.count(),
                    classFile.methods.count(),
                    classFile.attributes.count()))

        printer.levelDown()

        printer.println("Constant pool:")

        classFile.constantsAccept { cf, index, constant ->
            printer.print(String.format("%5s = ", "#$index"))
            constant.accept(cf, index, constantPoolPrinter)
            printer.println()
        }

        printer.println("{")

        printer.levelUp()
        classFile.fieldsAccept(this)
        printer.levelDown()

        printer.levelUp()
        methodCount = classFile.methods.size
        classFile.methodsAccept(this)
        printer.levelDown()

        printer.println("}")

        classFile.attributesAccept(attributePrinter)

        printer.flush()
    }

    override fun visitAnyMember(classFile: ClassFile, index: Int, member: Member) {
        printer.levelUp()

        printer.println("descriptor: %s".format(member.getDescriptor(classFile)))
        printer.print("flags: (0x%04x)".format(member.accessFlags))

        val modifiers = member.modifiers.joinToString(", ") { txt -> "ACC_$txt" }
        if (modifiers.isNotEmpty()) {
            printer.print(" $modifiers")
        }

        printer.println()

        member.attributesAccept(classFile, attributePrinter)
        printer.levelDown()
    }

    override fun visitField(classFile: ClassFile, index: Int, field: Field) {
        val externalModifiers = field.modifiers.joinToString(" ") { txt -> txt.toString().lowercase(Locale.getDefault()) }
        val externalType = field.getDescriptor(classFile).asJvmType().toExternalType()
        printer.println("%s %s %s;".format(externalModifiers, externalType, field.getName(classFile)))
        visitAnyMember(classFile, index, field)
        printer.println()
    }

    override fun visitMethod(classFile: ClassFile, index: Int, method: Method) {
        val externalModifiers = method.modifiers.joinToString(" ") { txt -> txt.toString().lowercase(Locale.getDefault()) }
        if (externalModifiers.isNotEmpty()) {
            printer.print("$externalModifiers ")
        }
        printer.println("%s;".format(method.getExternalMethodSignature(classFile)))
        visitAnyMember(classFile, index, method)
        if (index < methodCount - 1) {
            printer.println()
        }
    }
}

private fun Method.getExternalMethodSignature(classFile: ClassFile): String {
    return buildString {
        val (parameterTypes, returnType) = parseDescriptorToJvmTypes(getDescriptor(classFile))

        val methodName = getName(classFile)
        val isConstructor = methodName == "<init>"

        if (!isConstructor) {
            append(returnType.toExternalType())
            append(' ')
        }

        if (isConstructor) {
            append(classFile.className.toExternalClassName())
        } else {
            append(getName(classFile))
        }

        append(parameterTypes.joinToString(separator = ", ", prefix = "(", postfix = ")") { it.toExternalType() })
    }
}
