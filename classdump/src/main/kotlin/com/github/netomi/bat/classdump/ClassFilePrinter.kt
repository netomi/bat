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
import com.github.netomi.bat.classfile.attribute.module.ModuleAttribute
import com.github.netomi.bat.classfile.visitor.ClassFileVisitor
import com.github.netomi.bat.classfile.visitor.MemberVisitor
import com.github.netomi.bat.io.IndentingPrinter
import com.github.netomi.bat.util.*
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.Writer
import java.util.*

internal class ClassFilePrinter : ClassFileVisitor, MemberVisitor
{
    private val printer:             IndentingPrinter
    private val attributePrinter:    AttributePrinter
    private val constantPrinter:     ConstantPrinter
    private val constantPoolPrinter: ConstantPoolPrinter

    private var methodCount: Int = 0

    constructor(os: OutputStream = System.out) : this(OutputStreamWriter(os))

    constructor(writer: Writer) {
        this.printer             = IndentingPrinter(writer, 2)
        this.attributePrinter    = AttributePrinter(printer)
        this.constantPrinter     = ConstantPrinter(printer)
        this.constantPoolPrinter = ConstantPoolPrinter(printer, constantPrinter)
    }

    override fun visitClassFile(classFile: ClassFile) {

        if (classFile.isInterface) {
            val accessFlagsString =
                classFile.accessFlagsAsSet.toPrintableString { !EnumSet.of(AccessFlag.INTERFACE, AccessFlag.ABSTRACT).contains(it) }
            if (accessFlagsString.isNotEmpty()) {
                printer.print("$accessFlagsString ")
            }

            val signature = classFile.signature
            if (signature != null) {
                val externalSignature = getExternalClassSignature(signature, true)
                printer.print("interface %s%s".format(classFile.className.toExternalClassName(), externalSignature))
            } else {
                printer.print("interface %s".format(classFile.className.toExternalClassName()))

                if (classFile.interfaces.isNotEmpty()) {
                    val interfaceString = classFile.interfaces.joinToString(separator = ",", transform = { it.toExternalClassName() })
                    printer.print(" extends $interfaceString")
                }
            }
        } else if (classFile.isModule) {
            val module = classFile.attributes.filterIsInstance<ModuleAttribute>().single()

            val moduleName    = module.getModuleName(classFile)
            printer.print("module $moduleName")

            val moduleVersion = module.getModuleVersion(classFile)
            if (moduleVersion != null) {
                printer.print("@$moduleVersion")
            }
        } else {
            val accessFlagsString = classFile.accessFlagsAsSet.toPrintableString()
            if (accessFlagsString.isNotEmpty()) {
                printer.print("$accessFlagsString ")
            }

            val signature = classFile.signature
            if (signature != null) {
                val externalSignature = getExternalClassSignature(signature)
                printer.print("class %s%s".format(classFile.className.toExternalClassName(), externalSignature))
            } else {
                printer.print("class %s".format(classFile.className.toExternalClassName()))

                val superClassName = classFile.superClassName
                if (superClassName != null && superClassName.className != JAVA_LANG_OBJECT_TYPE.toInternalClassName()) {
                    printer.print(" extends ${superClassName.toExternalClassName()}")
                }

                if (classFile.interfaces.isNotEmpty()) {
                    val interfaceString = classFile.interfaces.joinToString(separator = ",", transform = { it.toExternalClassName() })
                    printer.print(" implements $interfaceString")
                }
            }
        }

        printer.println()
        printer.levelUp()
        printer.println("minor version: " + classFile.minorVersion)
        printer.println("major version: " + classFile.majorVersion)

        val flags = classFile.accessFlagsAsSet.toExternalStringWithPrefix(", ")
        printer.println("flags: (0x%04x) %s".format(classFile.accessFlags, flags))

        printer.print("this_class: #%-26d // ".format(classFile.thisClassIndex))
        classFile.constantAccept(classFile.thisClassIndex, constantPrinter)
        printer.println()
        if (classFile.superClassIndex > 0) {
            printer.print("super_class: #%-25d // ".format(classFile.superClassIndex))
            classFile.constantAccept(classFile.superClassIndex, constantPrinter)
            printer.println()
        } else {
            printer.println("super_class: #%-25d".format(classFile.superClassIndex))
        }

        printer.println("interfaces: %d, fields: %d, methods: %d, attributes: %d"
            .format(classFile.interfaces.count(),
                    classFile.fields.count(),
                    classFile.methods.count(),
                    classFile.attributes.count()))

        printer.levelDown()

        printer.println("Constant pool:")
        printer.levelUp()
        val indexWidth = classFile.constantPoolSize.toString().length + 1
        classFile.constantsAccept { cf, index, constant ->
            printer.print(String.format("%${indexWidth}s = ", "#$index"))
            constant.accept(cf, index, constantPoolPrinter)
            printer.println()
        }
        printer.levelDown()

        printer.println("{")

        printer.levelUp()
        classFile.fieldsAccept(this)
        printer.levelDown()

        printer.levelUp()
        methodCount = classFile.methods.size
        classFile.methodsAccept(this)
        printer.levelDown()

        printer.println("}")

        classFile.attributesAccept(attributePrinter)

        printer.flush()
    }

    override fun visitAnyMember(classFile: ClassFile, index: Int, member: Member) {
        printer.levelUp()

        printer.println("descriptor: %s".format(member.getDescriptor(classFile)))
        printer.print("flags: (0x%04x)".format(member.accessFlags))

        val flags = member.accessFlagsAsSet.toExternalStringWithPrefix(", ")
        if (flags.isNotEmpty()) {
            printer.print(" $flags")
        }

        printer.println()

        member.attributesAccept(classFile, attributePrinter)
        printer.levelDown()
    }

    override fun visitField(classFile: ClassFile, index: Int, field: Field) {
        val accessFlagsString = field.accessFlagsAsSet.toPrintableString()
        if (accessFlagsString.isNotEmpty()) {
            printer.print("$accessFlagsString ")
        }

        val signature = field.getSignature(classFile)
        val externalType = if (signature != null) {
            getExternalFieldSignature(signature)
        } else {
            field.getDescriptor(classFile).asJvmType().toExternalType()
        }

        printer.println("%s %s;".format(externalType, field.getName(classFile)))
        visitAnyMember(classFile, index, field)
        printer.println()
    }

    override fun visitMethod(classFile: ClassFile, index: Int, method: Method) {
        val accessFlagsString = method.accessFlagsAsSet.toPrintableString()
        if (accessFlagsString.isNotEmpty()) {
            printer.print("$accessFlagsString ")
        }

        if (classFile.isInterface && !method.isStatic) {
            if (method.hasCode) {
                printer.print("default ")
            }
        }

        val hasVarArgs = method.modifiers.contains(MethodModifier.VARARGS)

        var exceptionsPrinted = false
        val signature = method.getSignature(classFile)
        if (signature != null) {
            val methodSig = getExternalMethodSignature(signature)

            if (methodSig.formalTypeParameters.isNotEmpty()) {
                printer.print("${methodSig.formalTypeParameters} ")
            }

            val methodName = method.getName(classFile)

            if (methodName != "<init>" &&
                methodName != "<clinit>") {
                printer.print("${methodSig.returnType} ")
            }

            if (methodName == "<init>") {
                printer.print(classFile.className.toExternalClassName())
            } else {
                printer.print(methodName)
            }

            var parameters = methodSig.parameters

            if (hasVarArgs) {
                val bracketsIndex = parameters.lastIndexOf("[]")
                if (bracketsIndex == parameters.lastIndex - 2) {
                    parameters = parameters.replaceRange(bracketsIndex, bracketsIndex + 2, "...")
                }
            }

            printer.print(parameters)

            if (methodSig.exceptions.isNotEmpty()) {
                printer.print(methodSig.exceptions)
                exceptionsPrinted = true
            }
        } else {
            printer.print("%s".format(method.getExternalMethodSignature(classFile, hasVarArgs)))
        }

        if (!exceptionsPrinted) {
            val exceptions = method.getExceptionClassNames(classFile)
            if (exceptions.isNotEmpty()) {
                val exceptionString = " throws " + exceptions.joinToString(separator = ", ", transform = { it.toExternalClassName() })
                printer.print(exceptionString)
            }
        }

        printer.println(";")

        visitAnyMember(classFile, index, method)
        if (index < methodCount - 1) {
            printer.println()
        }
    }
}

internal fun Set<AccessFlag>.toPrintableString(filter: (AccessFlag) -> Boolean = { true }): String {
    val combinedFilter = { accessFlag: AccessFlag -> !accessFlag.synthetic && filter.invoke(accessFlag) }

    return this.filter(combinedFilter)
               .joinToString(separator = " ") { it.name.lowercase(Locale.getDefault()) }
}

internal fun Set<AccessFlag>.toExternalStringWithPrefix(separator: String = " "): String {
    return joinToString(separator) { txt -> "ACC_$txt" }
}

private fun Method.getExternalMethodSignature(classFile: ClassFile, hasVarArgs: Boolean): String {
    return buildString {
        val (parameterTypes, returnType) = parseDescriptorToJvmTypes(getDescriptor(classFile))

        val methodName = getName(classFile)
        val isStaticInitializer = methodName == "<clinit>"
        val isConstructor       = methodName == "<init>"

        if (!isStaticInitializer && !isConstructor) {
            append(returnType.toExternalType())
            append(' ')
        }

        if (isStaticInitializer) {
            append("{}")
        } else {
            if (isConstructor) {
                append(classFile.className.toExternalClassName())
            } else {
                append(getName(classFile))
            }

            var externalParameterTypes = parameterTypes.map { it.toExternalType() }

            if (hasVarArgs) {
                val lastParameter = externalParameterTypes.last()
                val bracketsIndex = lastParameter.lastIndexOf("[]")
                if (bracketsIndex == lastParameter.lastIndex - 1) {
                    externalParameterTypes =
                        externalParameterTypes.updated(
                            externalParameterTypes.lastIndex,
                            lastParameter.replaceRange(bracketsIndex, bracketsIndex + 2, "...")
                        )
                }
            }

            append(externalParameterTypes.joinToString(separator = ", ", prefix = "(", postfix = ")"))
        }
    }
}

private fun <E> Iterable<E>.updated(index: Int, elem: E) = mapIndexed { i, existing ->  if (i == index) elem else existing }