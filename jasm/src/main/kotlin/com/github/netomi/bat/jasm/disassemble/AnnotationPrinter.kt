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
import com.github.netomi.bat.classfile.attribute.annotation.Annotation
import com.github.netomi.bat.classfile.attribute.annotation.visitor.AnnotationVisitor
import com.github.netomi.bat.io.IndentingPrinter
import java.util.*

internal class AnnotationPrinter constructor(private val printer:             IndentingPrinter,
                                             private val elementValuePrinter: ElementValuePrinter): AnnotationVisitor {

    var visibility: AnnotationVisibility = AnnotationVisibility.RUNTIME

    override fun visitAnyAnnotation(classFile: ClassFile, annotation: Annotation) {}

    override fun visitAnnotation(classFile: ClassFile, annotation: Annotation) {
        printer.println(".annotation ${visibility.toExternalString()} ${annotation.getType(classFile)}")
        if (annotation.size > 0) {
            printer.levelUp()
            for (component in annotation) {
                printer.print("${classFile.getString(component.nameIndex)} = ")
                component.elementValue.accept(classFile, elementValuePrinter)
                printer.println()
            }
            printer.levelDown()
            printer.println(".end annotation")
        }
    }
}

enum class AnnotationVisibility {
    BUILD,
    RUNTIME;

    fun toExternalString(): String {
        return name.lowercase(Locale.getDefault())
    }

    companion object {
        fun of(text: String): AnnotationVisibility {
            for (visibility in values()) {
                if (visibility.name.equals(text, ignoreCase = true)) {
                    return visibility
                }
            }

            error("unexpected visibility '$text'")
        }
    }
}