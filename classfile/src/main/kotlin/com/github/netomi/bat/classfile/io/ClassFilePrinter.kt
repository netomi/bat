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

import com.github.netomi.bat.classfile.*
import com.github.netomi.bat.classfile.attribute.Attribute
import com.github.netomi.bat.classfile.attribute.ExceptionsAttribute
import com.github.netomi.bat.classfile.attribute.SignatureAttribute
import com.github.netomi.bat.classfile.attribute.SourceFileAttribute
import com.github.netomi.bat.classfile.attribute.annotations.*
import com.github.netomi.bat.classfile.attribute.annotations.visitor.ElementValueVisitor
import com.github.netomi.bat.classfile.attribute.visitor.AttributeVisitor
import com.github.netomi.bat.classfile.constant.*
import com.github.netomi.bat.classfile.constant.visitor.ConstantPoolVisitor
import com.github.netomi.bat.classfile.constant.visitor.ConstantVisitor
import com.github.netomi.bat.classfile.visitor.*
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
    ClassFileVisitor, ConstantPoolVisitor, ConstantVisitor, MemberVisitor, AttributeVisitor, ElementValueVisitor
{
    private val printer: IndentingPrinter

    constructor(os: OutputStream = System.out) : this(OutputStreamWriter(os))

    constructor(writer: Writer) {
        this.printer = IndentingPrinter(writer, 2)
    }

    override fun visitClassFile(classFile: ClassFile) {

        val externalModifiers = classFile.modifiers.filter { it != AccessFlag.SUPER }
                                                   .joinToString(" ") { it.toString().lowercase(Locale.getDefault()) }

        printer.println("%s class %s".format(externalModifiers, classFile.externalClassName))
        printer.levelUp()
        printer.println("minor version: " + classFile.minorVersion)
        printer.println("major version: " + classFile.majorVersion)

        val modifiers = classFile.modifiers.joinToString(", ") { txt -> "ACC_$txt" }
        printer.println("flags: (0x%04x) %s".format(classFile.accessFlags, modifiers))

        printer.println("this_class: #%-29d // %s".format(classFile.thisClassIndex,   classFile.className))
        printer.println("super_class: #%-28d // %s".format(classFile.superClassIndex, classFile.superClassName))

        printer.println("interfaces: %d, fields: %d, methods: %d, attributes: %d".format(
            classFile.interfaces().count(),
            classFile.fields().count(),
            classFile.methods().count(),
            classFile.attributes().count()))

        printer.levelDown()

        printer.println("Constant pool:")
        printer.levelUp()
        classFile.constantPoolAccept(this)
        printer.levelDown()

        printer.println("{")

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

    override fun visitAnyConstant(classFile: ClassFile, index: Int, constant: Constant) {
        printer.print(String.format("%4s = ", "#$index"))
        constant.accept(classFile, this)
        printer.println()
    }

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
        val str = classFile.getString(constant.stringIndex)
        printer.print("%-19s %-15s // %s".format("String", "#" + constant.stringIndex, str))
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
        printer.print("%-19s %-15s // %s".format(type,
                                                 "#" + refConstant.classIndex + ".#" + refConstant.nameAndTypeIndex,
                                                 str))
    }

    override fun visitClassConstant(classFile: ClassFile, constant: ClassConstant) {
        val str = classFile.getString(constant.nameIndex)
        printer.print("%-19s %-15s // %s".format("Class", "#" + constant.nameIndex, str))
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
        val str = constant.getModuleName(classFile)
        printer.print(String.format("%-19s %-15s // %s", "Module", "#" + constant.nameIndex, str))
    }

    override fun visitPackageConstant(classFile: ClassFile, constant: PackageConstant) {
        val str = constant.getPackageName(classFile)
        printer.print("%-19s %-15s // %s".format("Class", "#" + constant.nameIndex, str))
    }

    override fun visitAnyMember(classFile: ClassFile, index: Int, member: Member) {
        // TODO("Not yet implemented")
    }

    override fun visitField(classFile: ClassFile, index: Int, field: Field) {
        val externalModifiers = field.modifiers.joinToString(" ") { txt -> txt.toString().lowercase(Locale.getDefault()) }
        val externalType = field.getDescriptor(classFile).asJvmType().toExternalType()
        printer.println("%s %s %s;".format(externalModifiers, externalType, field.getName(classFile)))

        printer.levelUp()

        printer.println("descriptor: %s".format(field.getDescriptor(classFile)))

        val modifiers = field.modifiers.joinToString(", ") { txt -> "ACC_$txt" }
        printer.println("flags: (0x%04x) %s".format(field.accessFlags, modifiers))

        field.attributesAccept(classFile, this)
        printer.levelDown()
   }

    override fun visitMethod(classFile: ClassFile, index: Int, method: Method) {
        printer.println()

        val externalModifiers = method.modifiers.joinToString(" ") { txt -> txt.toString().lowercase(Locale.getDefault()) }
        printer.println("%s %s;".format(externalModifiers, method.getExternalMethodSignature(classFile)))

        printer.levelUp()
        printer.println("descriptor: %s".format(method.getDescriptor(classFile)))

        val modifiers = method.modifiers.joinToString(", ") { txt -> "ACC_$txt" }
        printer.println("flags: (0x%04x) %s".format(method.accessFlags, modifiers))

        method.attributesAccept(classFile, this)
        printer.levelDown()
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

    override fun visitRuntimeInvisibleAnnotationsAttribute(classFile: ClassFile, attribute: RuntimeInvisibleAnnotationsAttribute) {
        printer.println("RuntimeInvisibleAnnotations:")

        printer.levelUp()

        attribute.annotations.forEachIndexed {
            index, annotation ->
                printer.println("%2d: #%d()".format(index, annotation.typeIndex))
                printer.levelUp()
                printer.println(annotation.getJvmType(classFile).toExternalType())
                printer.levelDown()
        }

        printer.levelDown()
    }

    override fun visitRuntimeVisibleAnnotationsAttribute(classFile: ClassFile, attribute: RuntimeVisibleAnnotationsAttribute) {
        printer.println("RuntimeVisibleAnnotations:")

        printer.levelUp()

        attribute.annotations.forEachIndexed { index, annotation ->
            printer.println("%2d: #%d()".format(index, annotation.typeIndex))
            printer.levelUp()
            printer.println(annotation.getType(classFile).asJvmType().toExternalType())

            printer.levelUp()
            annotation.elementValues.forEachIndexed { _, (elementNameIndex, elementValue) ->
                printer.print("%s=".format(classFile.getString(elementNameIndex)))
                elementValue.accept(classFile, this)
                printer.println()
            }
            printer.levelDown()
            printer.levelDown()
        }

        printer.levelDown()
    }

    override fun visitAnyElementValue(classFile: ClassFile, elementValue: ElementValue) {
        // TODO("Not yet implemented")
    }

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
        elementValue.acceptElementValues(classFile, this.joinedByElementValueConsumer { _, _ -> printer.print(",") } )
        printer.print("]")
    }
}

private fun Method.getExternalMethodSignature(classFile: ClassFile): String {
    return buildString {
        val (parameterTypes, returnType) = parseDescriptorToJvmTypes(getDescriptor(classFile))

        append(returnType.toExternalType())
        append(' ')
        append(getName(classFile))
        append(parameterTypes.joinToString(separator = ", ", prefix = "(", postfix = ")") { it.toExternalType() })
    }
}