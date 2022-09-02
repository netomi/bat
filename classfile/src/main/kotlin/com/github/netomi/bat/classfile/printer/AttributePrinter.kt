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
import com.github.netomi.bat.classfile.Method
import com.github.netomi.bat.classfile.attribute.annotation.visitor.AnnotationVisitorIndexed
import com.github.netomi.bat.classfile.attribute.*
import com.github.netomi.bat.classfile.attribute.annotation.*
import com.github.netomi.bat.classfile.attribute.annotation.Annotation
import com.github.netomi.bat.classfile.attribute.annotation.visitor.ElementValueVisitor
import com.github.netomi.bat.classfile.attribute.preverification.StackMapTableAttribute
import com.github.netomi.bat.classfile.attribute.visitor.AttributeVisitor
import com.github.netomi.bat.io.IndentingPrinter
import com.github.netomi.bat.util.escapeAsJavaString
import com.github.netomi.bat.util.isAsciiPrintable

internal class AttributePrinter constructor(private val printer: IndentingPrinter): AttributeVisitor, ElementValueVisitor, AnnotationVisitorIndexed {

    private val referencedIndexPrinter = ReferencedIndexPrinter(printer)
    private val stackMapFramePrinter   = StackMapFramePrinter(printer)
    private val constantPrinter        = ConstantPrinter(printer)

    // common implementations

    override fun visitAnyAttribute(classFile: ClassFile, attribute: Attribute) {
        // TODO("Not yet implemented")
    }

    override fun visitAnySignatureAttribute(classFile: ClassFile, attribute: SignatureAttribute) {
        printer.println("Signature: #%-27d // %s".format(attribute.signatureIndex, attribute.getSignature(classFile)))
    }

    override fun visitAnyRuntimeAnnotationsAttribute(classFile: ClassFile, attribute: RuntimeAnnotationsAttribute) {
        printer.levelUp()
        attribute.annotationAcceptIndexed(classFile, this)
        printer.levelDown()
    }

    override fun visitAnyRuntimeInvisibleAnnotationsAttribute(classFile: ClassFile, attribute: RuntimeInvisibleAnnotationsAttribute) {
        printer.println("RuntimeInvisibleAnnotations:")
        visitAnyRuntimeAnnotationsAttribute(classFile, attribute)
    }

    override fun visitAnyRuntimeVisibleAnnotationsAttribute(classFile: ClassFile, attribute: RuntimeVisibleAnnotationsAttribute) {
        printer.println("RuntimeVisibleAnnotations:")
        visitAnyRuntimeAnnotationsAttribute(classFile, attribute)
    }

    // Implementations for ClassAttributeVisitor

    override fun visitBootstrapMethodsAttribute(classFile: ClassFile, attribute: BootstrapMethodsAttribute) {
        printer.println("BootstrapMethods:")

        printer.levelUp()

        for (index in 0 until attribute.size) {
            val element = attribute[index]
            printer.print("$index: #${element.bootstrapMethodRefIndex} ")
            element.bootstrapMethodRefAccept(classFile, constantPrinter)
            printer.println()
            printer.levelUp()
            printer.println("Method arguments:")
            printer.levelUp()
            for (argumentIndex in element) {
                printer.print("#${argumentIndex} ")
                classFile.getConstant(argumentIndex).accept(classFile, constantPrinter)
                printer.println()
            }
            printer.levelDown()
            printer.levelDown()
        }

        printer.levelDown()
    }

    override fun visitSourceFileAttribute(classFile: ClassFile, attribute: SourceFileAttribute) {
        printer.println("SourceFile: \"%s\"".format(attribute.getSourceFile(classFile)))
    }

    // Implementations for MethodAttributeVisitor

    override fun visitCodeAttribute(classFile: ClassFile, method: Method, attribute: CodeAttribute) {
        printer.println("Code:")
        printer.levelUp()
        printer.println("stack=${attribute.maxStack}, locals=${attribute.maxLocals}, args_size=${method.getArgumentSize(classFile)}")

        attribute.attributesAccept(classFile, method, this)

        printer.levelDown()
    }

    override fun visitExceptionsAttribute(classFile: ClassFile, method: Method, attribute: ExceptionsAttribute) {
        printer.println("Exceptions:")
        printer.levelUp()
        attribute.getExceptionClassNames(classFile).forEach { printer.println("throws ${it.toExternalClassName()}") }
        printer.levelDown()
    }

    override fun visitRuntimeParameterAnnotationsAttribute(classFile: ClassFile, method: Method, attribute: RuntimeParameterAnnotationsAttribute) {
        printer.levelUp()

        for (parameterIndex in 0 until attribute.size) {
            printer.println("parameter $parameterIndex:")
            printer.levelUp()
            attribute.parameterAnnotationsAcceptIndexed(classFile, parameterIndex, this)
            printer.levelDown()
        }

        printer.levelDown()
    }

    override fun visitRuntimeInvisibleParameterAnnotationsAttribute(classFile: ClassFile, method: Method, attribute: RuntimeInvisibleParameterAnnotationsAttribute) {
        printer.println("RuntimeInvisibleParameterAnnotations:")
        visitRuntimeParameterAnnotationsAttribute(classFile, method, attribute)
    }

    override fun visitRuntimeVisibleParameterAnnotationsAttribute(classFile: ClassFile, method: Method, attribute: RuntimeVisibleParameterAnnotationsAttribute) {
        printer.println("RuntimeVisibleParameterAnnotations:")
        visitRuntimeParameterAnnotationsAttribute(classFile, method, attribute)
    }

    // Implementations for CodeAttributeVisitor

    override fun visitLineNumberTableAttribute(classFile: ClassFile, method: Method, code: CodeAttribute, attribute: LineNumberTableAttribute) {
        printer.println("LineNumberTable:")
        printer.levelUp()
        for (element in attribute) {
            printer.println("line ${element.lineNumber}: ${element.startPC}")
        }
        printer.levelDown()
    }

    override fun visitLocalVariableTableAttribute(classFile: ClassFile, method: Method, code: CodeAttribute, attribute: LocalVariableTableAttribute) {
        printer.println("LocalVariableTable:")
        printer.levelUp()
        if (attribute.size > 0) {
            // TODO: better align name / signature to make output more readable
            printer.println("Start  Length  Slot  Name   Signature")
            for (element in attribute) {
                printer.println("%5d  %6d  %4d %5s   %s"
                    .format(element.startPC,
                            element.length,
                            element.variableIndex,
                            element.getName(classFile),
                            element.getDescriptor(classFile)))
            }
        }
        printer.levelDown()
    }

    override fun visitStackMapTableAttribute(classFile: ClassFile, method: Method, code: CodeAttribute, attribute: StackMapTableAttribute) {
        printer.println("StackMapTable: number_of_entries = ${attribute.size}")
        printer.levelUp()
        attribute.stackMapFramesAccept(classFile, stackMapFramePrinter)
        printer.levelDown()
    }

    // Implementations for AnnotationVisitor

    override fun visitAnnotation(classFile: ClassFile, index: Int, annotation: Annotation) {
        printer.print("${index}: ")
        referencedIndexPrinter.visitAnnotation(classFile, annotation)
        printer.println()
        printer.levelUp()
        printer.print(annotation.getType(classFile).toExternalType())

        if (annotation.elementValues.isNotEmpty()) {
            printer.println("(")
            printer.levelUp()
            annotation.elementValues.forEachIndexed { _, (elementNameIndex, elementValue) ->
                printer.print("${classFile.getString(elementNameIndex)}=")
                elementValue.accept(classFile, this)
                printer.println()
            }
            printer.levelDown()
            printer.println(")")
        } else {
            printer.println()
        }

        printer.levelDown()
    }

    // Implementations for ElementValueVisitor

    override fun visitAnyElementValue(classFile: ClassFile, elementValue: ElementValue) {}

    override fun visitIntElementValue(classFile: ClassFile, elementValue: ConstElementValue) {
        printer.print(classFile.getInteger(elementValue.constValueIndex))
    }

    override fun visitBooleanElementValue(classFile: ClassFile, elementValue: ConstElementValue) {
        printer.print(classFile.getBoolean(elementValue.constValueIndex))
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