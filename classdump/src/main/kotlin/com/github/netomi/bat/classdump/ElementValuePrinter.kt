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

package com.github.netomi.bat.classdump

import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.attribute.annotation.*
import com.github.netomi.bat.classfile.attribute.annotation.Annotation
import com.github.netomi.bat.classfile.attribute.annotation.visitor.AnnotationVisitor
import com.github.netomi.bat.classfile.attribute.annotation.visitor.ElementValueVisitor
import com.github.netomi.bat.classfile.constant.visitor.ConstantVisitor
import com.github.netomi.bat.io.IndentingPrinter
import com.github.netomi.bat.util.escapeAsJavaString
import com.github.netomi.bat.util.isAsciiPrintable

internal class ElementValuePrinter constructor(private val printer:           IndentingPrinter,
                                               private val constantPrinter:   ConstantVisitor): ElementValueVisitor, AnnotationVisitor {

    override fun visitAnyElementValue(classFile: ClassFile, elementValue: ElementValue) {
        TODO("implement printing of ${elementValue.type}")
    }

    override fun visitAnyConstElementValue(classFile: ClassFile, elementValue: ConstElementValue) {
        classFile.constantAccept(elementValue.constValueIndex, constantPrinter)
    }

    override fun visitAnnotationElementValue(classFile: ClassFile, elementValue: AnnotationElementValue) {
        printer.print("@")
        elementValue.annotation.accept(classFile, this)
    }

    override fun visitBooleanElementValue(classFile: ClassFile, elementValue: ConstElementValue) {
        printer.print(elementValue.getBoolean(classFile))
    }

    override fun visitClassElementValue(classFile: ClassFile, elementValue: ClassElementValue) {
        printer.print("class ${elementValue.getType(classFile)}")
    }

    override fun visitEnumElementValue(classFile: ClassFile, elementValue: EnumElementValue) {
        printer.print("${elementValue.getType(classFile)}.${elementValue.getConstName(classFile)}")
    }

    override fun visitStringElementValue(classFile: ClassFile, elementValue: ConstElementValue) {
        val value = classFile.getString(elementValue.constValueIndex).escapeAsJavaString()
        printer.print("\"%s\"".format(value))
    }

    override fun visitArrayElementValue(classFile: ClassFile, elementValue: ArrayElementValue) {
        printer.print("[")
        elementValue.elementValuesAccept(classFile, this.joinedByElementValueConsumer { _, _ -> printer.print(",") } )
        printer.print("]")
    }

    // AnnotationVisitor.

    override fun visitAnyAnnotation(classFile: ClassFile, annotation: Annotation) {
        printer.print(annotation.getType(classFile).toExternalType())

        if (annotation.size > 0) {
            printer.println("(")
            printer.levelUp()
            annotation.forEachIndexed { _, component ->
                printer.print("${classFile.getString(component.nameIndex)}=")
                component.elementValue.accept(classFile, this)
                printer.println()
            }
            printer.levelDown()
            printer.print(")")
        }
    }

}