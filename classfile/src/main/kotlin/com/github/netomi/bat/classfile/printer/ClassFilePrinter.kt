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
import com.github.netomi.bat.classfile.attribute.Attribute
import com.github.netomi.bat.classfile.attribute.ExceptionsAttribute
import com.github.netomi.bat.classfile.attribute.SignatureAttribute
import com.github.netomi.bat.classfile.attribute.SourceFileAttribute
import com.github.netomi.bat.classfile.attribute.annotations.*
import com.github.netomi.bat.classfile.attribute.annotations.visitor.ElementValueVisitor
import com.github.netomi.bat.classfile.attribute.visitor.AttributeVisitor
import com.github.netomi.bat.classfile.visitor.ClassFileVisitor
import com.github.netomi.bat.classfile.visitor.MemberVisitor
import com.github.netomi.bat.io.IndentingPrinter
import com.github.netomi.bat.util.asJvmType
import com.github.netomi.bat.util.escapeAsJavaString
import com.github.netomi.bat.util.isAsciiPrintable
import com.github.netomi.bat.util.parseDescriptorToJvmTypes
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.Writer
import java.util.*

class ClassFilePrinter :
    ClassFileVisitor, MemberVisitor, AttributeVisitor, ElementValueVisitor
{
    private val printer: IndentingPrinter

    constructor(os: OutputStream = System.out) : this(OutputStreamWriter(os))

    constructor(writer: Writer) {
        this.printer = IndentingPrinter(writer, 2)
    }

    override fun visitClassFile(classFile: ClassFile) {

        val externalModifiers = classFile.modifiers.filter { it != AccessFlag.SUPER }
                                                   .joinToString(" ") { it.toString().lowercase(Locale.getDefault()) }

        printer.println("%s class %s".format(externalModifiers, classFile.className.toExternalClassName()))
        printer.levelUp()
        printer.println("minor version: " + classFile.minorVersion)
        printer.println("major version: " + classFile.majorVersion)

        val modifiers = classFile.modifiers.joinToString(", ") { txt -> "ACC_$txt" }
        printer.println("flags: (0x%04x) %s".format(classFile.accessFlags, modifiers))

        printer.println("this_class: #%-29d // %s".format(classFile.thisClassIndex,   classFile.className))
        printer.println("super_class: #%-28d // %s".format(classFile.superClassIndex, classFile.superClassName))

        printer.println("interfaces: %d, fields: %d, methods: %d, attributes: %d"
            .format(classFile.interfaces.count(),
                    classFile.fields.count(),
                    classFile.methods.count(),
                    classFile.attributes.count()))

        printer.levelDown()

        printer.println("Constant pool:")
        printer.levelUp()

        val constantPrinter = ConstantPrinter(printer)
        classFile.constantPoolAccept { cf, index, constant ->
            printer.print(String.format("%4s = ", "#$index"))
            constant.accept(cf, constantPrinter)
            printer.println()
        }

        printer.levelDown()

        printer.print("{")

        printer.levelUp()
        classFile.fieldsAccept(this)
        printer.levelDown()

        printer.levelUp()
        classFile.methodsAccept(this)
        printer.levelDown()

        printer.println("}")

        classFile.attributesAccept(this)

        printer.flush()
    }

    override fun visitAnyMember(classFile: ClassFile, index: Int, member: Member) {
        printer.levelUp()

        printer.println("descriptor: %s".format(member.getDescriptor(classFile)))

        val modifiers = member.modifiers.joinToString(", ") { txt -> "ACC_$txt" }
        printer.println("flags: (0x%04x) %s".format(member.accessFlags, modifiers))

        member.attributesAccept(classFile, this)
        printer.levelDown()
    }

    override fun visitField(classFile: ClassFile, index: Int, field: Field) {
        printer.println()
        val externalModifiers = field.modifiers.joinToString(" ") { txt -> txt.toString().lowercase(Locale.getDefault()) }
        val externalType = field.getDescriptor(classFile).asJvmType().toExternalType()
        printer.println("%s %s %s;".format(externalModifiers, externalType, field.getName(classFile)))
        visitAnyMember(classFile, index, field)
   }

    override fun visitMethod(classFile: ClassFile, index: Int, method: Method) {
        printer.println()
        val externalModifiers = method.modifiers.joinToString(" ") { txt -> txt.toString().lowercase(Locale.getDefault()) }
        printer.println("%s %s;".format(externalModifiers, method.getExternalMethodSignature(classFile)))
        visitAnyMember(classFile, index, method)
    }

    override fun visitAnyAttribute(classFile: ClassFile, attribute: Attribute) {
        // TODO("Not yet implemented")
    }

    override fun visitSignatureAttribute(classFile: ClassFile, attribute: SignatureAttribute) {
        printer.println("Signature: #%-27d // %s".format(attribute.signatureIndex, attribute.getSignature(classFile)))
    }

    override fun visitSourceFileAttribute(classFile: ClassFile, attribute: SourceFileAttribute) {
        printer.println("SourceFile: \"%s\"".format(attribute.getSourceFile(classFile)))
    }

    override fun visitExceptionsAttributes(classFile: ClassFile, attribute: ExceptionsAttribute) {
        printer.println("Exceptions:")
        printer.levelUp()
        attribute.getExceptionClassNames(classFile).forEach { printer.println("throws ${it.toExternalClassName()}") }
        printer.levelDown()
    }

    override fun visitAnyRuntimeAnnotationsAttribute(classFile: ClassFile, attribute: RuntimeAnnotationsAttribute) {
        printer.levelUp()

        val referencedIndexPrinter = ReferencedIndexPrinter(printer)

        attribute.annotations.forEachIndexed { index, annotation ->
            printer.print("${index}: ")
            referencedIndexPrinter.visitAnnotation(classFile, annotation)
            printer.println()
            printer.levelUp()
            printer.println(annotation.getType(classFile).toExternalType())

            printer.levelUp()
            annotation.elementValues.forEachIndexed { _, (elementNameIndex, elementValue) ->
                printer.print("${classFile.getString(elementNameIndex)}=")
                elementValue.accept(classFile, this)
                printer.println()
            }
            printer.levelDown()
            printer.levelDown()
        }

        printer.levelDown()
    }

    override fun visitRuntimeInvisibleAnnotationsAttribute(classFile: ClassFile, attribute: RuntimeInvisibleAnnotationsAttribute) {
        printer.println("RuntimeInvisibleAnnotations:")
        visitAnyRuntimeAnnotationsAttribute(classFile, attribute)
    }

    override fun visitRuntimeVisibleAnnotationsAttribute(classFile: ClassFile, attribute: RuntimeVisibleAnnotationsAttribute) {
        printer.println("RuntimeVisibleAnnotations:")
        visitAnyRuntimeAnnotationsAttribute(classFile, attribute)
    }

    override fun visitAnyElementValue(classFile: ClassFile, elementValue: ElementValue) {}

    override fun visitIntElementValue(classFile: ClassFile, elementValue: ConstElementValue) {
        printer.print("%s".format(classFile.getInteger(elementValue.constValueIndex)))
    }

    override fun visitStringElementValue(classFile: ClassFile, elementValue: ConstElementValue) {
        val value = classFile.getString(elementValue.constValueIndex)

        val output = if (!value.isAsciiPrintable()) {
            value.escapeAsJavaString()
        } else {
            value
        }

        printer.print("\"%s\"".format(output))
    }

    override fun visitArrayElementValue(classFile: ClassFile, elementValue: ArrayElementValue) {
        printer.print("[")
        elementValue.elementValuesAccept(classFile, this.joinedByElementValueConsumer { _, _ -> printer.print(",") } )
        printer.print("]")
    }
}

private fun Method.getExternalMethodSignature(classFile: ClassFile): String {
    return buildString {
        val (parameterTypes, returnType) = parseDescriptorToJvmTypes(getDescriptor(classFile))

        append(returnType.toExternalType())
        append(' ')

        val methodName = getName(classFile)
        if (methodName == "<init>") {
            append(classFile.className.toExternalClassName())
        } else {
            append(getName(classFile))
        }

        append(parameterTypes.joinToString(separator = ", ", prefix = "(", postfix = ")") { it.toExternalType() })
    }
}
