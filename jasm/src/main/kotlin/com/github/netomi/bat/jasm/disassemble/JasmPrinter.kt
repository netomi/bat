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

import com.github.netomi.bat.classfile.AccessFlag
import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.Field
import com.github.netomi.bat.classfile.Member
import com.github.netomi.bat.classfile.attribute.*
import com.github.netomi.bat.classfile.visitor.ClassFileVisitor
import com.github.netomi.bat.classfile.visitor.MemberVisitor
import com.github.netomi.bat.io.IndentingPrinter
import java.io.OutputStreamWriter
import java.io.Writer
import java.util.*

class JasmPrinter constructor(writer: Writer = OutputStreamWriter(System.out))
    : ClassFileVisitor, MemberVisitor {

    private val printer          = IndentingPrinter(writer, 4)
    private val attributePrinter = AttributePrinter(printer)
    private val constantPrinter  = ConstantPrinter(printer)

    override fun visitClassFile(classFile: ClassFile) {
        printer.print(".class")

        val modifiers = classFile.modifiers
        // the super flag is implicit in Java SE8 and above, think about how to handle it
        // it feels redundant to always print it, we should probably only do it when byte code version is below 52.0
        // modifiers.remove(AccessFlag.SUPER)

        if (modifiers.isNotEmpty()) {
            printer.print(" ${modifiers.joinToString(separator = " ", transform = { it.toString().lowercase(Locale.getDefault()) })}")
        }

        printer.println(" ${classFile.className}")
        printer.println(".bytecode ${classFile.majorVersion}.${classFile.minorVersion}")

        val superClassName = classFile.superClassName
        if (superClassName != null) {
            printer.println(".super $superClassName")
        }

        if (classFile.interfaces.isNotEmpty()) {
            printer.println()
            printer.println("# interfaces")
            for (interfaceName in classFile.interfaces) {
                printer.println(".implements $interfaceName")
            }
        }

        if (classFile.fields.isNotEmpty()) {
            printer.println()
            printer.println()
            printer.println("# fields")
            classFile.fieldsAccept(this)
        }

        if (classFile.methods.isNotEmpty()) {
            printer.println()
            printer.println()
            printer.println("# methods")
            classFile.methodsAccept(this)
        }

        val remainingAttributes = classFile.attributes.filter { it !is SourceFileAttribute }.size
        if (remainingAttributes > 0) {
            printer.println()
            printer.println()
            printer.println("# attributes")
            attributePrinter.reset()
            classFile.attributesAccept(attributePrinter)
        }

        printer.flush()
    }

    // MemberVisitor.

    override fun visitAnyMember(classFile: ClassFile, index: Int, member: Member) {}

    override fun visitField(classFile: ClassFile, index: Int, field: Field) {
        printer.print(".field")

        val modifiers =
            field.modifiers.joinToString(separator = " ", transform = { it.toString().lowercase(Locale.getDefault())})

        if (modifiers.isNotEmpty()) {
            printer.print(" $modifiers")
        }

        printer.print(" ${field.getName(classFile)}:${field.getDescriptor(classFile)}")

        if (field.isStatic) {
            field.constantValueAccept(classFile) { _, _, constant ->
                printer.print(" = ")
                constant.accept(classFile, constantPrinter)
            }
        }

        printer.println()
        printer.levelUp()
        attributePrinter.reset()
        field.attributesAccept(classFile, attributePrinter)
        printer.levelDown()

        if (attributePrinter.printedAttributes) {
            printer.println(".end field")
        }

        printer.println()
    }
}
