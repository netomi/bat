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
import com.github.netomi.bat.classfile.attribute.annotation.visitor.AnnotationVisitor
import com.github.netomi.bat.classfile.attribute.annotation.visitor.ElementValueVisitor
import com.github.netomi.bat.classfile.attribute.annotation.*
import com.github.netomi.bat.classfile.attribute.annotation.Annotation
import com.github.netomi.bat.io.IndentingPrinter

internal class ReferencedIndexPrinter constructor(private val printer: IndentingPrinter): ElementValueVisitor, AnnotationVisitor {

    override fun visitAnyAnnotation(classFile: ClassFile, annotation: Annotation) {
        printer.print("#${annotation.typeIndex}(")
        annotation.elementValues.forEachIndexed { index, (elementNameIndex, elementValue) ->
            printer.print("#${elementNameIndex}=")
            elementValue.accept(classFile, this)
            if (index < annotation.elementValues.lastIndex) {
                printer.print(",")
            }
        }
        printer.print(")")
    }

    override fun visitAnyElementValue(classFile: ClassFile, elementValue: ElementValue) {}

    override fun visitClassElementValue(classFile: ClassFile, elementValue: ClassElementValue) {
        TODO("implement")
    }

    override fun visitEnumElementValue(classFile: ClassFile, elementValue: EnumElementValue) {
        printer.print("e#${elementValue.constNameIndex}")
    }

    override fun visitArrayElementValue(classFile: ClassFile, elementValue: ArrayElementValue) {
        printer.print("[")
        elementValue.elementValuesAccept(classFile, this.joinedByElementValueConsumer { _, _ -> printer.print(",") } )
        printer.print("]")
    }

    override fun visitAnnotationElementValue(classFile: ClassFile, elementValue: AnnotationElementValue) {
        visitAnnotation(classFile, elementValue.annotation)
    }

    override fun visitByteElementValue(classFile: ClassFile, elementValue: ConstElementValue) {
        printer.print("B#${elementValue.constValueIndex}")
    }

    override fun visitCharElementValue(classFile: ClassFile, elementValue: ConstElementValue) {
        printer.print("C#${elementValue.constValueIndex}")
    }

    override fun visitIntElementValue(classFile: ClassFile, elementValue: ConstElementValue) {
        printer.print("I#${elementValue.constValueIndex}")
    }

    override fun visitLongElementValue(classFile: ClassFile, elementValue: ConstElementValue) {
        printer.print("J#${elementValue.constValueIndex}")
    }

    override fun visitShortElementValue(classFile: ClassFile, elementValue: ConstElementValue) {
        printer.print("S#${elementValue.constValueIndex}")
    }

    override fun visitFloatElementValue(classFile: ClassFile, elementValue: ConstElementValue) {
        printer.print("F#${elementValue.constValueIndex}")
    }

    override fun visitDoubleElementValue(classFile: ClassFile, elementValue: ConstElementValue) {
        printer.print("D#${elementValue.constValueIndex}")
    }

    override fun visitBooleanElementValue(classFile: ClassFile, elementValue: ConstElementValue) {
        printer.print("Z#${elementValue.constValueIndex}")
    }

    override fun visitStringElementValue(classFile: ClassFile, elementValue: ConstElementValue) {
        printer.print("s#${elementValue.constValueIndex}")
    }
}