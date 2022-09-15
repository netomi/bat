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

import com.github.netomi.bat.classfile.*
import com.github.netomi.bat.classfile.attribute.annotation.visitor.AnnotationVisitorIndexed
import com.github.netomi.bat.classfile.attribute.*
import com.github.netomi.bat.classfile.attribute.annotation.*
import com.github.netomi.bat.classfile.attribute.annotation.Annotation
import com.github.netomi.bat.classfile.attribute.annotation.visitor.ElementValueVisitor
import com.github.netomi.bat.classfile.attribute.module.ModuleAttribute
import com.github.netomi.bat.classfile.attribute.module.ModulePackagesAttribute
import com.github.netomi.bat.classfile.attribute.preverification.StackMapTableAttribute
import com.github.netomi.bat.classfile.attribute.visitor.AttributeVisitor
import com.github.netomi.bat.io.IndentingPrinter
import com.github.netomi.bat.util.asJvmType
import com.github.netomi.bat.util.escapeAsJavaString
import com.github.netomi.bat.util.isAsciiPrintable

internal class AttributePrinter constructor(private val printer: IndentingPrinter): AttributeVisitor, ElementValueVisitor, AnnotationVisitorIndexed {

    private val referencedIndexPrinter = ReferencedIndexPrinter(printer)
    private val stackMapFramePrinter   = StackMapFramePrinter(printer)
    private val constantPrinter        = ConstantPrinter(printer)
    private val instructionPrinter     = InstructionPrinter(printer)
    private val targetInfoPrinter      = TargetInfoPrinter(printer)

    // common implementations

    override fun visitAnyAttribute(classFile: ClassFile, attribute: Attribute) {}

    override fun visitUnknownAttribute(classFile: ClassFile, attribute: UnknownAttribute) {
        printer.println("UnknownAttribute: name=${attribute.getAttributeName(classFile)} size=${attribute.data.size}")
    }

    override fun visitAnyDeprecated(classFile: ClassFile, attribute: DeprecatedAttribute) {
        printer.println("Deprecated: true")
    }

    override fun visitAnySignature(classFile: ClassFile, attribute: SignatureAttribute) {
        printer.println("Signature: #%-27d // %s".format(attribute.signatureIndex, attribute.getSignature(classFile)))
    }

    override fun visitAnyRuntimeAnnotations(classFile: ClassFile, attribute: RuntimeAnnotationsAttribute) {
        printer.levelUp()
        attribute.annotationsAcceptIndexed(classFile, this)
        printer.levelDown()
    }

    override fun visitAnyRuntimeInvisibleAnnotations(classFile: ClassFile, attribute: RuntimeInvisibleAnnotationsAttribute) {
        printer.println("RuntimeInvisibleAnnotations:")
        visitAnyRuntimeAnnotations(classFile, attribute)
    }

    override fun visitAnyRuntimeVisibleAnnotations(classFile: ClassFile, attribute: RuntimeVisibleAnnotationsAttribute) {
        printer.println("RuntimeVisibleAnnotations:")
        visitAnyRuntimeAnnotations(classFile, attribute)
    }

    override fun visitAnyRuntimeTypeAnnotations(classFile: ClassFile, attribute: RuntimeTypeAnnotationsAttribute) {
        printer.levelUp()
        attribute.typeAnnotationsAcceptIndexed(classFile, this)
        printer.levelDown()
    }

    override fun visitAnyRuntimeInvisibleTypeAnnotations(classFile: ClassFile, attribute: RuntimeInvisibleTypeAnnotationsAttribute) {
        printer.println("RuntimeInvisibleTypeAnnotations:")
        visitAnyRuntimeTypeAnnotations(classFile, attribute)
    }

    override fun visitAnyRuntimeVisibleTypeAnnotations(classFile: ClassFile, attribute: RuntimeVisibleTypeAnnotationsAttribute) {
        printer.println("RuntimeVisibleTypeAnnotations:")
        visitAnyRuntimeTypeAnnotations(classFile, attribute)
    }

    // Implementations for ClassAttributeVisitor

    override fun visitBootstrapMethods(classFile: ClassFile, attribute: BootstrapMethodsAttribute) {
        printer.println("BootstrapMethods:")

        printer.levelUp()

        attribute.forEachIndexed { index, bootstrapMethod ->
            printer.print("$index: #${bootstrapMethod.bootstrapMethodRefIndex} ")
            bootstrapMethod.bootstrapMethodRefAccept(classFile, constantPrinter)
            printer.println()
            printer.levelUp()
            printer.println("Method arguments:")
            printer.levelUp()
            for (argumentIndex in bootstrapMethod) {
                printer.print("#${argumentIndex} ")
                classFile.constantAccept(argumentIndex, constantPrinter)
                printer.println()
            }
            printer.levelDown()
            printer.levelDown()
        }

        printer.levelDown()
    }

    override fun visitEnclosingMethod(classFile: ClassFile, attribute: EnclosingMethodAttribute) {
        val str = buildString {
            append("EnclosingMethod: ")
            append("#${attribute.classIndex}.#${attribute.methodIndex}")
        }
        printer.print("%-39s // %s".format(str, attribute.getClassName(classFile).toExternalClassName()))

        if (attribute.methodIndex > 0) {
            printer.print(".${attribute.getMethodName(classFile)}")
        }

        printer.println()
    }

    override fun visitInnerClasses(classFile: ClassFile, attribute: InnerClassesAttribute) {
        printer.println("InnerClasses:")
        printer.levelUp()
        for (entry in attribute) {
            val str = buildString {
                val accessFlags = entry.innerClassAccessFlagsAsSet.toMutableSet()
                if (accessFlags.contains(AccessFlag.INTERFACE)) {
                    accessFlags.remove(AccessFlag.ABSTRACT)
                }
                accessFlags.remove(AccessFlag.INTERFACE)

                val externalModifiers = accessFlags.toPrintableString()
                if (externalModifiers.isNotEmpty()) {
                    append("$externalModifiers ")
                }

                if (entry.innerNameIndex != 0) {
                    append("#${entry.innerNameIndex}= ")
                }

                append("#${entry.innerClassIndex}")

                if (entry.outerClassIndex != 0) {
                    append(" of #${entry.outerClassIndex}")
                }

                append(";")
            }

            val desc = buildString {
                if (entry.innerNameIndex != 0) {
                    append("${classFile.getString(entry.innerNameIndex)}=")
                }

                append("class ${classFile.getClassName(entry.innerClassIndex)}")

                if (entry.outerClassIndex != 0) {
                    append(" of class ${classFile.getClassName(entry.outerClassIndex)}")
                }
            }

            printer.println("%-39s // %s".format(str, desc))
        }
        printer.levelDown()
    }

    override fun visitRecord(classFile: ClassFile, attribute: RecordAttribute) {
        printer.println("Record:")
        printer.levelUp()
        for (component in attribute) {
            printer.println("%s %s;".format(component.getDescriptor(classFile).asJvmType().toExternalType(), component.getName(classFile)))
            printer.levelUp()
            printer.println("descriptor: ${component.getDescriptor(classFile)}")
            component.attributesAccept(classFile, attribute, this)
            printer.levelDown()
            printer.println()
        }
        printer.levelDown()
    }

    override fun visitPermittedSubclasses(classFile: ClassFile, attribute: PermittedSubclassesAttribute) {
        printer.println("PermittedSubclasses:")
        printer.levelUp()
        for (classIndex in attribute) {
            classFile.constantAccept(classIndex, constantPrinter)
            printer.println()
        }
        printer.levelDown()
    }

    override fun visitSourceFile(classFile: ClassFile, attribute: SourceFileAttribute) {
        printer.println("SourceFile: \"%s\"".format(attribute.getSourceFile(classFile)))
    }

    override fun visitSourceDebugExtension(classFile: ClassFile, attribute: SourceDebugExtensionAttribute) {
        printer.println("SourceDebugExtension:")
        printer.levelUp()
        val stringList = String(attribute.debugExtension).split('\n')
        for (string in stringList) {
            if (string.isNotEmpty()) {
                printer.println(string)
            }
        }
        printer.levelDown()
    }

    override fun visitModule(classFile: ClassFile, attribute: ModuleAttribute) {
        printer.println("Module:")
        printer.levelUp()

        val moduleIndexAndFlags = "${attribute.moduleNameIndex},${attribute.moduleFlags.toHexString()}"
        printer.print("#%-38s // ".format(moduleIndexAndFlags))
        classFile.constantAccept(attribute.moduleNameIndex, constantPrinter)
        val moduleFlags = attribute.moduleFlagsAsSet
        if (moduleFlags.isNotEmpty()) {
            printer.print(" ${moduleFlags.toExternalStringWithPrefix()}")
        }
        printer.println()
        if (attribute.moduleVersionIndex > 0) {
            printer.println("#%-38s // %s".format(attribute.moduleVersionIndex, attribute.getModuleVersion(classFile)))
        } else {
            printer.println("#%-38s".format(attribute.moduleVersionIndex))
        }

        // requires
        printer.println("%-39d // requires".format(attribute.requires.size))
        printer.levelUp()
        for (requiresEntry in attribute.requires) {
            val requiresIndexAndFlags = "${requiresEntry.requiredModuleIndex},${requiresEntry.flags.toHexString()}"
            printer.print("#%-38s // ".format(requiresIndexAndFlags))
            classFile.constantAccept(requiresEntry.requiredModuleIndex, constantPrinter)

            val requireFlags = requiresEntry.flagsAsSet
            if (requireFlags.isNotEmpty()) {
                printer.print(" ${requireFlags.toExternalStringWithPrefix()}")
            }
            printer.println()

            if (requiresEntry.requiredVersionIndex > 0) {
                printer.println("#%-38s // %s".format(requiresEntry.requiredVersionIndex, requiresEntry.getRequiredVersion(classFile)))
            } else {
                printer.println("#%-38s".format(requiresEntry.requiredVersionIndex))
            }
        }
        printer.levelDown()

        // exports
        printer.println("%-39d // exports".format(attribute.exports.size))
        printer.levelUp()
        for (exportsEntry in attribute.exports) {
            val exportsIndexAndFlags = "${exportsEntry.exportedPackageIndex},${exportsEntry.flags.toHexString()}"
            printer.print("#%-38s // ".format(exportsIndexAndFlags))
            classFile.constantAccept(exportsEntry.exportedPackageIndex, constantPrinter)

            val exportFlags = exportsEntry.flagsAsSet
            if (exportFlags.isNotEmpty()) {
                printer.print(" ${exportFlags.toExternalStringWithPrefix()}")
            }

            if (exportsEntry.size > 0) {
                printer.println(" to ... ${exportsEntry.size}")
                printer.levelUp()
                for (exportsToModuleIndex in exportsEntry) {
                    printer.print("#%-38s // ... to ".format(exportsToModuleIndex))
                    classFile.constantAccept(exportsToModuleIndex, constantPrinter)
                    printer.println()
                }
                printer.levelDown()
            } else {
                printer.println()
            }
        }
        printer.levelDown()

        // opens
        printer.println("%-39d // opens".format(attribute.opens.size))
        printer.levelUp()
        for (opensEntry in attribute.opens) {
            val opensIndexAndFlags = "${opensEntry.openedPackageIndex},${opensEntry.flags.toHexString()}"
            printer.print("#%-38s // ".format(opensIndexAndFlags))
            classFile.constantAccept(opensEntry.openedPackageIndex, constantPrinter)

            val openFlags = opensEntry.flagsAsSet
            if (openFlags.isNotEmpty()) {
                printer.print(" ${openFlags.toExternalStringWithPrefix()}")
            }

            printer.println(" to ... ${opensEntry.size}")
            printer.levelUp()
            for (opensToModuleIndex in opensEntry) {
                printer.print("#%-38s // ... to ".format(opensToModuleIndex))
                classFile.constantAccept(opensToModuleIndex, constantPrinter)
                printer.println()
            }
            printer.levelDown()
        }
        printer.levelDown()

        // uses
        printer.println("%-39d // uses".format(attribute.uses.size))
        printer.levelUp()
        for (usedClassIndex in attribute.uses) {
            printer.print("#%-38s // ".format(usedClassIndex))
            classFile.constantAccept(usedClassIndex, constantPrinter)
            printer.println()
        }
        printer.levelDown()

        // provides
        printer.println("%-39d // provides".format(attribute.provides.size))
        printer.levelUp()
        for (providesEntry in attribute.provides) {
            printer.print("#%-38s // ".format(providesEntry.providedClassIndex))
            classFile.constantAccept(providesEntry.providedClassIndex, constantPrinter)
            if (providesEntry.size > 0) {
                printer.println(" with ... ${providesEntry.size}")
                printer.levelUp()
                for (providesWithClassIndex in providesEntry) {
                    printer.print("#%-38s // ... with ".format(providesWithClassIndex))
                    classFile.constantAccept(providesWithClassIndex, constantPrinter)
                    printer.println()
                }
                printer.levelDown()
            } else {
                printer.println()
            }
        }
        printer.levelDown()
        printer.levelDown()
    }

    override fun visitModulePackages(classFile: ClassFile, attribute: ModulePackagesAttribute) {
        printer.println("ModulePackages:")
        printer.levelUp()
        for (packageIndex in attribute) {
            printer.print("#%-38s // ".format(packageIndex))
            classFile.constantAccept(packageIndex, constantPrinter)
            printer.println()
        }
        printer.levelDown()
    }

    override fun visitNestHost(classFile: ClassFile, attribute: NestHostAttribute) {
        printer.print("NestHost: class ")
        classFile.constantAccept(attribute.hostClassIndex, constantPrinter)
        printer.println()
    }

    override fun visitNestMembers(classFile: ClassFile, attribute: NestMembersAttribute) {
        printer.println("NestMembers:")
        printer.levelUp()
        for (memberIndex in attribute) {
            classFile.constantAccept(memberIndex, constantPrinter)
            printer.println()
        }
        printer.levelDown()
    }

    // Implementations for FieldAttributeVisitor

    override fun visitConstantValue(classFile: ClassFile, field: Field, attribute: ConstantValueAttribute) {
        printer.print("ConstantValue: ")
        val constantValuePrinter = ConstantPrinter(printer, printConstantType = true, alwaysIncludeClassName = false)
        classFile.constantAccept(attribute.constantValueIndex, constantValuePrinter)
        printer.println()
    }

    // Implementations for MethodAttributeVisitor

    override fun visitAnnotationDefault(classFile: ClassFile, method: Method, attribute: AnnotationDefaultAttribute) {
        printer.println("AnnotationDefault:")
        printer.levelUp()
        printer.print("default_value: ")
        attribute.elementValue.accept(classFile, referencedIndexPrinter)
        printer.println()
        printer.levelUp()
        attribute.elementValue.accept(classFile, this)
        printer.println()
        printer.levelDown()
        printer.levelDown()
    }

    override fun visitCode(classFile: ClassFile, method: Method, attribute: CodeAttribute) {
        printer.println("Code:")
        printer.levelUp()
        printer.println("stack=${attribute.maxStack}, locals=${attribute.maxLocals}, args_size=${method.getArgumentCount(classFile)}")

        attribute.instructionsAccept(classFile, method, instructionPrinter)

        if (attribute.exceptionTable.isNotEmpty()) {
            printer.println("Exception table:")
            printer.println("   from    to  target type")
            for (exception in attribute.exceptionTable) {
                printer.print("  %6d %5d %5d".format(exception.startPC, exception.endPC, exception.handlerPC))

                if (exception.catchType > 0) {
                    printer.print("   Class ${classFile.getClassName(exception.catchType)}")
                } else {
                    printer.print("   any")
                }

                printer.println()
            }
        }

        attribute.attributesAccept(classFile, method, this)

        printer.levelDown()
    }

    override fun visitExceptions(classFile: ClassFile, method: Method, attribute: ExceptionsAttribute) {
        printer.println("Exceptions:")
        printer.levelUp()
        val exceptions =
            attribute.getExceptionClassNames(classFile)
                     .joinToString(separator = ", ",
                                   prefix    = "throws ",
                                   transform = { it.toExternalClassName() })
        printer.println(exceptions)
        printer.levelDown()
    }

    override fun visitRuntimeParameterAnnotations(classFile: ClassFile, method: Method, attribute: RuntimeParameterAnnotationsAttribute) {
        printer.levelUp()

        for (parameterIndex in 0 until attribute.size) {
            printer.println("parameter $parameterIndex:")
            printer.levelUp()
            attribute.parameterAnnotationsAcceptIndexed(classFile, parameterIndex, this)
            printer.levelDown()
        }

        printer.levelDown()
    }

    override fun visitRuntimeInvisibleParameterAnnotations(classFile: ClassFile, method: Method, attribute: RuntimeInvisibleParameterAnnotationsAttribute) {
        printer.println("RuntimeInvisibleParameterAnnotations:")
        visitRuntimeParameterAnnotations(classFile, method, attribute)
    }

    override fun visitRuntimeVisibleParameterAnnotations(classFile: ClassFile, method: Method, attribute: RuntimeVisibleParameterAnnotationsAttribute) {
        printer.println("RuntimeVisibleParameterAnnotations:")
        visitRuntimeParameterAnnotations(classFile, method, attribute)
    }

    override fun visitMethodParameters(classFile: ClassFile, method: Method, attribute: MethodParametersAttribute) {
        printer.println("MethodParameters:")
        printer.levelUp()
        printer.println("%-30s %4s".format("Name", "Flags"))
        for (entry in attribute) {
            printer.print("%-30s".format(entry.getName(classFile)))
            if (entry.accessFlags != 0) {
                printer.print(" %s".format(entry.accessFlagsAsSet.toPrintableString()))
            }
            printer.println()
        }
        printer.levelDown()
    }

    // Implementations for CodeAttributeVisitor

    override fun visitLineNumberTable(classFile: ClassFile, method: Method, code: CodeAttribute, attribute: LineNumberTableAttribute) {
        printer.println("LineNumberTable:")
        printer.levelUp()
        for (entry in attribute) {
            printer.println("line ${entry.lineNumber}: ${entry.startPC}")
        }
        printer.levelDown()
    }

    override fun visitLocalVariableTable(classFile: ClassFile, method: Method, code: CodeAttribute, attribute: LocalVariableTableAttribute) {
        printer.println("LocalVariableTable:")
        printer.levelUp()
        // TODO: better align name / signature to make output more readable
        printer.println("Start  Length  Slot  Name   Signature")
        for (entry in attribute) {
            printer.println("%5d  %6d  %4d %5s   %s"
                .format(entry.startPC,
                        entry.length,
                        entry.variableIndex,
                        entry.getName(classFile),
                        entry.getDescriptor(classFile)))
        }
        printer.levelDown()
    }

    override fun visitLocalVariableTypeTable(classFile: ClassFile, method: Method, code: CodeAttribute, attribute: LocalVariableTypeTableAttribute) {
        printer.println("LocalVariableTypeTable:")
        printer.levelUp()
        if (attribute.size > 0) {
            // TODO: better align name / signature to make output more readable
            printer.println("Start  Length  Slot  Name   Signature")
            for (entry in attribute) {
                printer.println("%5d  %6d  %4d %5s   %s"
                    .format(entry.startPC,
                            entry.length,
                            entry.variableIndex,
                            entry.getName(classFile),
                            entry.getSignature(classFile)))
            }
        }
        printer.levelDown()
    }

    override fun visitStackMapTable(classFile: ClassFile, method: Method, code: CodeAttribute, attribute: StackMapTableAttribute) {
        printer.println("StackMapTable: number_of_entries = ${attribute.size}")
        printer.levelUp()
        attribute.stackMapFramesAccept(classFile, stackMapFramePrinter)
        printer.levelDown()
    }

    // Implementations for AnnotationVisitor

    override fun visitAnyAnnotation(classFile: ClassFile, index: Int, annotation: Annotation) {
        printer.levelUp()
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
            printer.println(")")
        } else {
            printer.println()
        }

        printer.levelDown()
    }

    override fun visitAnnotation(classFile: ClassFile, index: Int, annotation: Annotation) {
        printer.print("${index}: ")
        referencedIndexPrinter.visitAnnotation(classFile, annotation)
        printer.println()
        visitAnyAnnotation(classFile, index, annotation)
    }

    override fun visitTypeAnnotation(classFile: ClassFile, index: Int, typeAnnotation: TypeAnnotation) {
        printer.print("${index}: ")
        referencedIndexPrinter.visitAnnotation(classFile, typeAnnotation)
        printer.print(": ${typeAnnotation.target.type}")
        typeAnnotation.target.accept(classFile, targetInfoPrinter)

        if (typeAnnotation.path.size > 0) {

            val pathTransformer: (TypePathEntry) -> CharSequence = { element ->
                when (element.type) {
                    TypePathType.INNER_TYPE    -> element.type.toString()
                    TypePathType.ARRAY         -> element.type.toString()
                    TypePathType.WILDCARD      -> element.type.toString()
                    TypePathType.TYPE_ARGUMENT -> "${element.type}(${element.typeArgumentIndex})"
                }
            }

            val location = typeAnnotation.path.joinToString(separator = ", ", prefix = "[", postfix = "]", transform = pathTransformer)
            printer.print(", location=${location}")
        }

        printer.println()
        visitAnyAnnotation(classFile, index, typeAnnotation)
    }

    // Implementations for ElementValueVisitor

    override fun visitAnyElementValue(classFile: ClassFile, elementValue: ElementValue) {}

    override fun visitAnyConstElementValue(classFile: ClassFile, elementValue: ConstElementValue) {
        classFile.constantAccept(elementValue.constValueIndex, constantPrinter)
    }

    override fun visitBooleanElementValue(classFile: ClassFile, elementValue: ConstElementValue) {
        printer.print(elementValue.getBoolean(classFile))
    }

    override fun visitEnumElementValue(classFile: ClassFile, elementValue: EnumElementValue) {
        printer.print("${elementValue.getTypeName(classFile)}.${elementValue.getConstName(classFile)}")
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

private fun Int.toHexString(): String {
    return Integer.toHexString(this)
}
