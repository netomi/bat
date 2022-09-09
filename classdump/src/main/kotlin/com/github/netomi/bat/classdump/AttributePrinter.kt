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

    override fun visitAnyDeprecatedAttribute(classFile: ClassFile, attribute: DeprecatedAttribute) {
        printer.println("Deprecated: true")
    }

    override fun visitAnySignatureAttribute(classFile: ClassFile, attribute: SignatureAttribute) {
        printer.println("Signature: #%-27d // %s".format(attribute.signatureIndex, attribute.getSignature(classFile)))
    }

    override fun visitAnyRuntimeAnnotationsAttribute(classFile: ClassFile, attribute: RuntimeAnnotationsAttribute) {
        printer.levelUp()
        attribute.annotationsAcceptIndexed(classFile, this)
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

    override fun visitAnyRuntimeTypeAnnotationsAttribute(classFile: ClassFile, attribute: RuntimeTypeAnnotationsAttribute) {
        printer.levelUp()
        attribute.typeAnnotationsAcceptIndexed(classFile, this)
        printer.levelDown()
    }

    override fun visitAnyRuntimeInvisibleTypeAnnotationsAttribute(classFile: ClassFile, attribute: RuntimeInvisibleTypeAnnotationsAttribute) {
        printer.println("RuntimeInvisibleTypeAnnotations:")
        visitAnyRuntimeTypeAnnotationsAttribute(classFile, attribute)
    }

    override fun visitAnyRuntimeVisibleTypeAnnotationsAttribute(classFile: ClassFile, attribute: RuntimeVisibleTypeAnnotationsAttribute) {
        printer.println("RuntimeVisibleTypeAnnotations:")
        visitAnyRuntimeTypeAnnotationsAttribute(classFile, attribute)
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
                classFile.constantAccept(argumentIndex, constantPrinter)
                printer.println()
            }
            printer.levelDown()
            printer.levelDown()
        }

        printer.levelDown()
    }

    override fun visitEnclosingMethodAttribute(classFile: ClassFile, attribute: EnclosingMethodAttribute) {
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

    override fun visitInnerClassesAttribute(classFile: ClassFile, attribute: InnerClassesAttribute) {
        printer.println("InnerClasses:")
        printer.levelUp()
        for (element in attribute) {
            val str = buildString {
                val modifiers = accessFlagModifiers(element.innerClassAccessFlags, AccessFlagTarget.INNER_CLASS)

                if (modifiers.contains(AccessFlag.INTERFACE)) {
                    modifiers.remove(AccessFlag.ABSTRACT)
                }
                modifiers.remove(AccessFlag.INTERFACE)

                val externalModifiers = modifiers.getPrintableModifiers().joinToString(separator = " ")
                if (externalModifiers.isNotEmpty()) {
                    append(externalModifiers)
                    append(" ")
                }

                if (element.innerNameIndex != 0) {
                    append("#${element.innerNameIndex}= ")
                }

                append("#${element.innerClassIndex}")

                if (element.outerClassIndex != 0) {
                    append(" of #${element.outerClassIndex}")
                }

                append(";")
            }

            val desc = buildString {
                if (element.innerNameIndex != 0) {
                    append("${classFile.getString(element.innerNameIndex)}=")
                }

                append("class ${classFile.getClassName(element.innerClassIndex)}")

                if (element.outerClassIndex != 0) {
                    append(" of class ${classFile.getClassName(element.outerClassIndex)}")
                }
            }

            printer.println("%-39s // %s".format(str, desc))
        }
        printer.levelDown()
    }

    override fun visitRecordAttribute(classFile: ClassFile, attribute: RecordAttribute) {
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

    override fun visitSourceFileAttribute(classFile: ClassFile, attribute: SourceFileAttribute) {
        printer.println("SourceFile: \"%s\"".format(attribute.getSourceFile(classFile)))
    }

    override fun visitSourceDebugExtensionAttribute(classFile: ClassFile, attribute: SourceDebugExtensionAttribute) {
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

    override fun visitModuleAttribute(classFile: ClassFile, attribute: ModuleAttribute) {
        printer.println("Module:")
        printer.levelUp()
        val moduleIndexAndFlags = "${attribute.moduleNameIndex},${attribute.moduleFlags.toHexString()}"
        printer.print("#%-38s // ".format(moduleIndexAndFlags))
        classFile.constantAccept(attribute.moduleNameIndex, constantPrinter)
        if (attribute.modifiers.isNotEmpty()) {
            printer.print(" " + attribute.modifiers.toExternalString())
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
        for (requiresElement in attribute.requires) {
            val requiresIndexAndFlags = "${requiresElement.requiresIndex},${requiresElement.requiresFlags.toHexString()}"
            printer.print("#%-38s // ".format(requiresIndexAndFlags))
            classFile.constantAccept(requiresElement.requiresIndex, constantPrinter)
            if (requiresElement.modifiers.isNotEmpty()) {
                printer.print(" " + requiresElement.modifiers.toExternalString())
            }
            printer.println()
            if (requiresElement.requiresVersionIndex > 0) {
                printer.println("#%-38s // %s".format(requiresElement.requiresVersionIndex, requiresElement.getRequiredVersion(classFile)))
            } else {
                printer.println("#%-38s".format(requiresElement.requiresVersionIndex))
            }
        }
        printer.levelDown()

        // exports
        printer.println("%-39d // exports".format(attribute.exports.size))
        printer.levelUp()
        for (exportsElement in attribute.exports) {
            val exportsIndexAndFlags = "${exportsElement.exportsIndex},${exportsElement.exportsFlags.toHexString()}"
            printer.print("#%-38s // ".format(exportsIndexAndFlags))
            classFile.constantAccept(exportsElement.exportsIndex, constantPrinter)
            // TODO: print accessflags
            printer.println()
        }
        printer.levelDown()

        // opens
        printer.println("%-39d // opens".format(attribute.opens.size))
        printer.levelUp()
        for (opensElement in attribute.opens) {
            val opensIndexAndFlags = "${opensElement.opensIndex},${opensElement.opensFlags.toHexString()}"
            printer.print("#%-38s // ".format(opensIndexAndFlags))
            classFile.constantAccept(opensElement.opensIndex, constantPrinter)
            // TODO: print accessflags
            printer.println(" to ... ${opensElement.size}")
            printer.levelUp()
            for (opensToIndex in opensElement) {
                printer.print("#%-38s // ... to ".format(opensToIndex))
                classFile.constantAccept(opensToIndex, constantPrinter)
                printer.println()
            }
            printer.levelDown()
        }
        printer.levelDown()

        // uses
        printer.println("%-39d // uses".format(attribute.uses.size))
        printer.levelUp()
        for (usesIndex in attribute.uses) {
            printer.print("#%-38s // ".format(usesIndex))
            classFile.constantAccept(usesIndex, constantPrinter)
            printer.println()
        }
        printer.levelDown()

        // provides
        printer.println("%-39d // provides".format(attribute.provides.size))
        printer.levelUp()
        for (providesElement in attribute.provides) {
            printer.print("#%-38s // ".format(providesElement.providesIndex))
            classFile.constantAccept(providesElement.providesIndex, constantPrinter)
            printer.levelUp()
            for (providesWithIndex in providesElement) {
                printer.print("#%-38s // ".format(providesWithIndex))
                classFile.constantAccept(providesWithIndex, constantPrinter)
                printer.println()
            }
            printer.levelDown()
        }
        printer.levelDown()
        printer.levelDown()
    }

    // Implementations for FieldAttributeVisitor

    override fun visitConstantValueAttribute(classFile: ClassFile, field: Field, attribute: ConstantValueAttribute) {
        printer.print("ConstantValue: ")
        val constantValuePrinter = ConstantPrinter(printer, printConstantType = true, alwaysIncludeClassName = false)
        classFile.constantAccept(attribute.constantValueIndex, constantValuePrinter)
        printer.println()
    }

    // Implementations for MethodAttributeVisitor

    override fun visitAnnotationDefaultAttribute(classFile: ClassFile, method: Method, attribute: AnnotationDefaultAttribute) {
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

    override fun visitCodeAttribute(classFile: ClassFile, method: Method, attribute: CodeAttribute) {
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

    override fun visitExceptionsAttribute(classFile: ClassFile, method: Method, attribute: ExceptionsAttribute) {
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
        printer.levelDown()
    }

    override fun visitLocalVariableTypeTableAttribute(classFile: ClassFile, method: Method, code: CodeAttribute, attribute: LocalVariableTypeTableAttribute) {
        printer.println("LocalVariableTypeTable:")
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
                        element.getSignature(classFile)))
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

    override fun visitAnyAnnotation(classFile: ClassFile, index: Int, annotation: Annotation) {
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

            val pathTransformer: (PathElement) -> CharSequence = { element ->
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

private fun Set<AccessFlag>.toExternalString(): String {
    return joinToString(" ") { txt -> "ACC_$txt" }
}