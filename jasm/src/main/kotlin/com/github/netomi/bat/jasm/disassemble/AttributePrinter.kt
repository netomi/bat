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

import com.github.netomi.bat.classfile.*
import com.github.netomi.bat.classfile.attribute.*
import com.github.netomi.bat.classfile.attribute.annotation.*
import com.github.netomi.bat.classfile.attribute.module.ModuleAttribute
import com.github.netomi.bat.classfile.attribute.visitor.AttributeVisitor
import com.github.netomi.bat.io.IndentingPrinter
import com.github.netomi.bat.util.parseDescriptorToJvmTypes
import java.util.*

internal class AttributePrinter constructor(private val printer:         IndentingPrinter,
                                            private val constantPrinter: ConstantPrinter): AttributeVisitor {

    private val elementValuePrinter = ElementValuePrinter(printer, constantPrinter)
    private val annotationPrinter   = AnnotationPrinter(printer, elementValuePrinter)


    var printedAttributes: Boolean = false
        private set

    fun reset() {
        printedAttributes = false
    }

    // Common Attributes.

    override fun visitAnyAttribute(classFile: ClassFile, attribute: Attribute) {
        TODO("implement ${attribute.type}")
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
            attribute.annotationsAccept(classFile, annotationPrinter.joinedByAnnotationConsumer { _, _ -> printer.println() })
            printedAttributes = true
        }
    }

    override fun visitAnyRuntimeInvisibleAnnotations(classFile: ClassFile, attribute: RuntimeInvisibleAnnotationsAttribute) {
        if (attribute.size > 0) {
            annotationPrinter.visibility = AnnotationVisibility.BUILD
            attribute.annotationsAccept(classFile, annotationPrinter.joinedByAnnotationConsumer { _, _ -> printer.println() })
            printedAttributes = true
        }
    }

    override fun visitAnyRuntimeVisibleTypeAnnotations(classFile: ClassFile, attribute: RuntimeVisibleTypeAnnotationsAttribute) {
        if (attribute.size > 0) {
            annotationPrinter.visibility = AnnotationVisibility.RUNTIME
            attribute.typeAnnotationsAccept(classFile, annotationPrinter.joinedByAnnotationConsumer { _, _ -> printer.println() })
            printedAttributes = true
        }
    }

    override fun visitAnyRuntimeInvisibleTypeAnnotations(classFile: ClassFile, attribute: RuntimeInvisibleTypeAnnotationsAttribute) {
        if (attribute.size > 0) {
            annotationPrinter.visibility = AnnotationVisibility.BUILD
            attribute.typeAnnotationsAccept(classFile, annotationPrinter.joinedByAnnotationConsumer { _, _ -> printer.println() })
            printedAttributes = true
        }
    }

    // ClassAttributeVisitor.

    override fun visitSourceFile(classFile: ClassFile, attribute: SourceFileAttribute) {
        val sourceFile = attribute.getSourceFile(classFile)
        printer.println(".source \"$sourceFile\"")
        printedAttributes = true
    }

    override fun visitInnerClasses(classFile: ClassFile, attribute: InnerClassesAttribute) {
        for (entry in attribute) {
            printer.print(".innerclass")

            val accessFlags =
                formatAccessFlagsAsHumanReadable(entry.innerClassAccessFlags, AccessFlagTarget.INNER_CLASS).lowercase(Locale.getDefault())

            if (accessFlags.isNotEmpty()) {
                printer.print(" $accessFlags")
            }

            val innerClassName = entry.getInnerClass(classFile)
            printer.print(" $innerClassName")

            val name = entry.getInnerName(classFile)
            if (name != null) {
                printer.print(" as $name")
            }

            val outerClassName = entry.getOuterClass(classFile)
            if (outerClassName != null) {
                printer.print(" in $outerClassName")
            }

            printer.println()
        }

        printedAttributes = attribute.size > 0
    }

    override fun visitEnclosingMethod(classFile: ClassFile, attribute: EnclosingMethodAttribute) {
        val methodData = buildString {
            append(attribute.getClassName(classFile))

            if (attribute.methodIndex > 0) {
                append("->")
                append(attribute.getMethodName(classFile))
                append(attribute.getMethodDescriptor(classFile))
            }
        }

        printer.println(".enclosingmethod $methodData")
        printedAttributes = true
    }

    override fun visitBootstrapMethods(classFile: ClassFile, attribute: BootstrapMethodsAttribute) {
        //TODO("implement")
    }

    override fun visitModule(classFile: ClassFile, attribute: ModuleAttribute) {
        printer.print(".module")

        if (attribute.moduleFlags != 0) {
            printer.print(" ${attribute.moduleFlagsAsSet.toPrintableString()}")
        }

        printer.print(" ${attribute.getModuleName(classFile)}")

        val moduleVersion = attribute.getModuleVersion(classFile)
        if (moduleVersion != null) {
            printer.print("@${moduleVersion}")
        }

        printer.println()
        printer.levelUp()

        for (requireEntry in attribute.requires) {
            printer.print(".requires")

            if (requireEntry.flags != 0) {
                printer.print(" ${requireEntry.flagsAsSet.toPrintableString()}")
            }

            printer.print(" ${requireEntry.getRequiredModuleName(classFile)}")

            val requiredVersion = requireEntry.getRequiredVersion(classFile)
            if (requiredVersion != null) {
                printer.print("@${requiredVersion}")
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
        printedAttributes = true
    }

    override fun visitSourceDebugExtension(classFile: ClassFile, attribute: SourceDebugExtensionAttribute) {
        printer.println(".sourcedebugextension")
        val stringList = String(attribute.debugExtension).split('\n')
        printer.print("\"")
        for (string in stringList) {
            if (string.isNotEmpty()) {
                printer.println(string)
            }
        }
        printer.println("\"")
        printedAttributes = true
    }

    // FieldAttributeVisitor.

    // printed directly in JasmPrinter when visiting a field.
    override fun visitConstantValue(classFile: ClassFile, field: Field, attribute: ConstantValueAttribute) {}

    // MethodAttributeVisitor.

    override fun visitAnnotationDefault(classFile: ClassFile, method: Method, attribute: AnnotationDefaultAttribute) {
        printer.print(".annotationdefault ")
        attribute.elementValue.accept(classFile, elementValuePrinter)
        printer.println()
        printedAttributes = true
    }

    override fun visitExceptions(classFile: ClassFile, method: Method, attribute: ExceptionsAttribute) {
        for (exceptionClassName in attribute.getExceptionClassNames(classFile)) {
            printer.println(".throws $exceptionClassName")
        }
        printedAttributes = attribute.size > 0
    }

    override fun visitRuntimeParameterAnnotations(classFile: ClassFile, method: Method, attribute: RuntimeParameterAnnotationsAttribute) {
        val parameterTypes = parseDescriptorToJvmTypes(method.getDescriptor(classFile))
        for ((parameterIndex, parameterType) in parameterTypes.first.withIndex()) {
            if (attribute.getParameterAnnotationCount(parameterIndex) > 0) {
                printer.println(".param %d    # %s".format(parameterIndex, parameterType))
                printer.levelUp()
                attribute.parameterAnnotationsAccept(classFile, parameterIndex, annotationPrinter.joinedByAnnotationConsumer { _, _ -> printer.println() })
                printer.levelDown()
                printer.println(".end param")
            }
        }
        printedAttributes = true
    }

    override fun visitRuntimeVisibleParameterAnnotations(classFile: ClassFile, method: Method, attribute: RuntimeVisibleParameterAnnotationsAttribute) {
        if (attribute.size > 0) {
            annotationPrinter.visibility = AnnotationVisibility.RUNTIME
            visitRuntimeParameterAnnotations(classFile, method, attribute)
        }
    }

    override fun visitRuntimeInvisibleParameterAnnotations(classFile: ClassFile, method: Method, attribute: RuntimeInvisibleParameterAnnotationsAttribute) {
        if (attribute.size > 0) {
            annotationPrinter.visibility = AnnotationVisibility.BUILD
            visitRuntimeParameterAnnotations(classFile, method, attribute)
        }
    }

    // print the code differently to ensure that it's the last attribute to be printed
    override fun visitCode(classFile: ClassFile, method: Method, attribute: CodeAttribute) {}
}

internal fun Set<AccessFlag>.toPrintableString(filter: (AccessFlag) -> Boolean = { true }): String {
    return this.filter(filter)
               .joinToString(separator = " ") { it.name.lowercase(Locale.getDefault()) }
}