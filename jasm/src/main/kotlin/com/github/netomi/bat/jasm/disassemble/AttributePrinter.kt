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
import com.github.netomi.bat.classfile.attribute.*
import com.github.netomi.bat.classfile.attribute.annotation.RuntimeInvisibleAnnotationsAttribute
import com.github.netomi.bat.classfile.attribute.annotation.RuntimeVisibleAnnotationsAttribute
import com.github.netomi.bat.classfile.attribute.module.ModuleAttribute
import com.github.netomi.bat.classfile.attribute.visitor.AttributeVisitor
import com.github.netomi.bat.io.IndentingPrinter
import java.util.*

internal class AttributePrinter constructor(private val printer:         IndentingPrinter,
                                            private val constantPrinter: ConstantPrinter): AttributeVisitor {

    private val annotationPrinter = AnnotationPrinter(printer, constantPrinter)

    var printedAttributes: Boolean = false
        private set

    fun reset() {
        printedAttributes = false
    }

    // Common Attributes.

    override fun visitAnyAttribute(classFile: ClassFile, attribute: Attribute) {
        //TODO("implement")
    }

    override fun visitAnyDeprecated(classFile: ClassFile, attribute: DeprecatedAttribute) {
        printer.println(".deprecated")
        printedAttributes = true
    }

    override fun visitAnySynthetic(classFile: ClassFile, attribute: SyntheticAttribute) {
        printer.println(".synthetic")
        printedAttributes = true
    }

    override fun visitAnySignature(classFile: ClassFile, attribute: SignatureAttribute) {
        printer.println(".signature \"${attribute.getSignature(classFile)}\"")
        printedAttributes = true
    }

    override fun visitAnyRuntimeVisibleAnnotations(classFile: ClassFile, attribute: RuntimeVisibleAnnotationsAttribute) {
        if (attribute.size > 0) {
            annotationPrinter.visibility = AnnotationVisibility.RUNTIME
            attribute.annotationsAccept(classFile, annotationPrinter)
            printedAttributes = true
        }
    }

    override fun visitAnyRuntimeInvisibleAnnotations(classFile: ClassFile, attribute: RuntimeInvisibleAnnotationsAttribute) {
        if (attribute.size > 0) {
            annotationPrinter.visibility = AnnotationVisibility.BUILD
            attribute.annotationsAccept(classFile, annotationPrinter)
            printedAttributes = true
        }
    }

    // ClassAttributeVisitor.

    override fun visitSourceFile(classFile: ClassFile, attribute: SourceFileAttribute) {
        val sourceFile = attribute.getSourceFile(classFile)
        printer.println(".source \"$sourceFile\"")
        printedAttributes = true
    }

    override fun visitModule(classFile: ClassFile, attribute: ModuleAttribute) {
        printer.print(".module ${attribute.getModuleName(classFile)}")

        val moduleVersion = attribute.getModuleVersion(classFile)
        if (moduleVersion != null) {
            printer.print("@${moduleVersion}")
        }

        if (attribute.moduleFlags != 0) {
            printer.print(", ${attribute.moduleFlagsAsSet.toPrintableString()}")
        }

        printer.println()
        printer.levelUp()

        for (requireEntry in attribute.requires) {
            printer.print(".requires ${requireEntry.getRequiredModuleName(classFile)}")

            val requiredVersion = requireEntry.getRequiredVersion(classFile)
            if (requiredVersion != null) {
                printer.print("@${requiredVersion}")
            }

            if (requireEntry.flags != 0) {
                printer.print(", ${requireEntry.flagsAsSet.toPrintableString()}")
            }
            printer.println()
        }

        for (exportEntry in attribute.exports) {
            printer.print(".exports ${exportEntry.getExportedPackageName(classFile)}")
            if (exportEntry.flags != 0) {
                printer.print(", ${exportEntry.flagsAsSet.toPrintableString()}")
            }
            val exportedToModuleNames = exportEntry.getExportedToModuleNames(classFile)
            if (exportedToModuleNames.isNotEmpty()) {
                printer.println(" to {")
                printer.levelUp()
                exportedToModuleNames.joinTo(printer, separator = ",\n", postfix = "\n")
                printer.levelDown()
                printer.println("}")
            } else {
                printer.println()
            }
        }

        printer.levelDown()
        printer.println(".end module")
    }

    // FieldAttributeVisitor.
}

internal fun Set<AccessFlag>.toPrintableString(filter: (AccessFlag) -> Boolean = { true }): String {
    return this.filter(filter)
               .joinToString(separator = " ") { it.name.lowercase(Locale.getDefault()) }
}