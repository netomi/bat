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

import com.github.netomi.bat.classfile.ClassFile
import com.github.netomi.bat.classfile.attribute.annotation.*
import com.github.netomi.bat.classfile.attribute.annotation.visitor.ElementValueVisitor
import com.github.netomi.bat.io.IndentingPrinter

internal class ElementValuePrinter constructor(private val printer:         IndentingPrinter,
                                               private val constantPrinter: ConstantPrinter): ElementValueVisitor {

    override fun visitAnyElementValue(classFile: ClassFile, elementValue: ElementValue) {
        TODO("implement")
    }

    override fun visitClassElementValue(classFile: ClassFile, elementValue: ClassElementValue) {
        printer.print(elementValue.getType(classFile))
    }

    override fun visitEnumElementValue(classFile: ClassFile, elementValue: EnumElementValue) {
        printer.print(".enum ${elementValue.getType(classFile)}.${elementValue.getConstName(classFile)}")
    }

    override fun visitArrayElementValue(classFile: ClassFile, elementValue: ArrayElementValue) {
        printer.print("{")
        elementValue.elementValuesAccept(classFile, this.joinedByElementValueConsumer { _, _ -> printer.print(",") })
        printer.print("}")
    }

    override fun visitAnnotationElementValue(classFile: ClassFile, elementValue: AnnotationElementValue) {
        val resetIndentation = if (printer.currentPosition > 0) {
            printer.resetIndentation(printer.currentPosition)
            true
        } else {
            false
        }

        printer.println(".subannotation ${elementValue.annotation.getType(classFile)}")
        printer.levelUp()
        for (component in elementValue.annotation) {
            printer.print("${classFile.getString(component.nameIndex)} = ")
            component.elementValue.accept(classFile, this)
            printer.println()
        }
        printer.levelDown()
        printer.print(".end subannotation")

        if (resetIndentation) {
            printer.levelDown()
        }
    }

    override fun visitAnyConstElementValue(classFile: ClassFile, elementValue: ConstElementValue) {
        classFile.constantAccept(elementValue.constValueIndex, constantPrinter)
    }

    override fun visitBooleanElementValue(classFile: ClassFile, elementValue: ConstElementValue) {
        printer.print("${elementValue.getBoolean(classFile)}")
    }
}