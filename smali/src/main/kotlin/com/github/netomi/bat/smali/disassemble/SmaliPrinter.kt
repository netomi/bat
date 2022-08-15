/*
 *  Copyright (c) 2020 Thomas Neidhart.
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
package com.github.netomi.bat.smali.disassemble

import com.github.netomi.bat.dexfile.*
import com.github.netomi.bat.dexfile.visitor.*
import com.github.netomi.bat.io.IndentingPrinter
import java.io.OutputStreamWriter
import java.io.Writer
import java.util.*

class SmaliPrinter constructor(writer: Writer = OutputStreamWriter(System.out)) : ClassDefVisitor, EncodedFieldVisitor, EncodedMethodVisitor, CodeVisitor {

    private val printer: IndentingPrinter = IndentingPrinter(writer, 4)
    private val annotationPrinter         = AnnotationPrinter(printer)

    override fun visitClassDef(dexFile: DexFile, classDef: ClassDef) {
        printer.print(".class")

        val accessFlags =
            formatAccessFlagsAsHumanReadable(classDef.accessFlags, DexAccessFlagTarget.CLASS).lowercase(Locale.getDefault())

        if (accessFlags.isNotEmpty()) {
            printer.print(" $accessFlags")
        }

        printer.print(" ${classDef.getType(dexFile)}")
        printer.println()
        val superClassType = classDef.getSuperClassType(dexFile)
        if (superClassType != null) {
            printer.println(".super $superClassType")
        }

        val sourceFile = classDef.getSourceFile(dexFile)
        if (sourceFile != null) {
            printer.println(".source \"$sourceFile\"")
        }

        if (!classDef.interfaces.isEmpty) {
            printer.println()
            printer.println("# interfaces")
            classDef.interfacesAccept(dexFile) { _, _, _, _, type: String -> printer.println(".implements $type") }
        }

        classDef.classAnnotationSetAccept(dexFile, classDef, annotationPrinter)

        if (classDef.staticFields.isNotEmpty()) {
            printer.println()
            printer.println()
            printer.println("# static fields")
            classDef.staticFieldsAccept(dexFile, this.joinedByFieldConsumer { _, _ -> printer.println() })
        }
        if (classDef.instanceFields.isNotEmpty()) {
            printer.println()
            printer.println()
            printer.println("# instance fields")
            classDef.instanceFieldsAccept(dexFile, this.joinedByFieldConsumer { _, _ -> printer.println() })
        }
        if (classDef.directMethods.isNotEmpty()) {
            printer.println()
            printer.println()
            printer.println("# direct methods")
            classDef.directMethodsAccept(dexFile, this.joinedByMethodConsumer { _, _ -> printer.println() })
        }
        if (classDef.virtualMethods.isNotEmpty()) {
            printer.println()
            printer.println()
            printer.println("# virtual methods")
            classDef.virtualMethodsAccept(dexFile, this.joinedByMethodConsumer { _, _ -> printer.println() })
        }

        printer.flush()
    }

    override fun visitAnyField(dexFile: DexFile, classDef: ClassDef, field: EncodedField) {
        printer.print(".field")

        val accessFlags =
            formatAccessFlagsAsHumanReadable(field.accessFlags, DexAccessFlagTarget.FIELD).lowercase(Locale.getDefault())

        if (accessFlags.isNotEmpty()) {
            printer.print(" $accessFlags")
        }

        printer.print(" " + field.getName(dexFile) + ":" + field.getType(dexFile))

        if (field.isStatic) {
            val detector = InitializationDetector(field.getName(dexFile), field.getType(dexFile))
            classDef.methodsAccept(dexFile, "<clinit>", allCode(allInstructions(detector)))

            if (!detector.fieldIsSetInStaticInitializer || !field.modifiers.contains(FieldModifier.FINAL)) {
                field.staticValueAccept(dexFile, classDef, EncodedValuePrinter(printer, null, " = "))
            }
        }

        printer.println()
        field.annotationSetAccept(dexFile, classDef, annotationPrinter)
    }

    override fun visitAnyMethod(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod) {
        printer.print(".method")

        val accessFlags =
            formatAccessFlagsAsHumanReadable(method.accessFlags, DexAccessFlagTarget.METHOD)
                .replace("_".toRegex(), "-")
                .lowercase(Locale.getDefault())

        if (accessFlags.isNotEmpty()) {
            printer.print(" $accessFlags")
        }

        printer.println(" " + method.getName(dexFile) + method.getDescriptor(dexFile))

        // print code.
        printer.levelUp()
        if (!method.isAbstract && !method.code.isEmpty) {
            method.codeAccept(dexFile, classDef, this)
        } else if (classDef.hasAnnotations()) {
            printParameterAnnotations(dexFile, classDef, method)
            method.annotationSetAccept(dexFile, classDef, annotationPrinter)
        }

        printer.levelDown()
        printer.println(".end method")
    }

    private fun printParameterAnnotations(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod) {
        var registerIndex = if (method.isStatic) 0 else 1

        for ((parameterIndex, parameterType) in method.getParameterDexTypes(dexFile).withIndex()) {
            annotationPrinter.apply {
                printParameterInfo   = true
                currentParameterType = parameterType.type
                currentRegisterIndex = registerIndex
            }
            classDef.parameterAnnotationSetAccept(dexFile, classDef, method, parameterIndex, annotationPrinter)

            registerIndex += parameterType.getArgumentSize()
        }

        annotationPrinter.printParameterInfo = false
    }

    override fun visitCode(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code) {
        printer.println(".registers " + code.registersSize)

        val localVariableInfos = arrayOfNulls<LocalVariableInfo>(code.registersSize)
        if (!code.debugInfo.isEmpty) {
            val localVariables = code.registersSize - code.insSize
            var registerIndex  = if (method.isStatic) 0 else 1

            if (!method.isStatic) {
                val classType = method.getClassType(dexFile)
                localVariableInfos[localVariables] = LocalVariableInfo("this", classType, null)
            }

            for ((parameterIndex, parameterType) in method.getParameterDexTypes(dexFile).withIndex()) {
                val parameterName = code.debugInfo.getParameterName(dexFile, parameterIndex)
                if (parameterName != null) {
                    printer.println(".param p%d, \"%s\"    # %s".format(registerIndex, parameterName, parameterType))
                    annotationPrinter.printParameterInfo = false
                } else {
                    annotationPrinter.apply {
                        printParameterInfo   = true
                        currentRegisterIndex = registerIndex
                        currentParameterType = parameterType.type
                    }
                }

                val localVariableInfo = LocalVariableInfo(parameterName, parameterType.type, null)
                localVariableInfos[localVariables + registerIndex] = localVariableInfo
                classDef.parameterAnnotationSetAccept(dexFile, classDef, method, parameterIndex, annotationPrinter)
                annotationPrinter.printParameterInfo = false

                registerIndex += parameterType.getArgumentSize()
            }
        } else {
            printParameterAnnotations(dexFile, classDef, method)
        }
        classDef.methodAnnotationSetAccept(dexFile, classDef, method, annotationPrinter)

        val registerPrinter     = RegisterPrinter(code)
        val branchTargetPrinter = BranchTargetPrinter()
        val debugState: MutableMap<Int, MutableList<String>> = HashMap()

        if (!code.debugInfo.isEmpty) {
            code.debugInfo.debugSequenceAccept(dexFile, SourceLineCollector(debugState, code.debugInfo.lineStart))
            code.debugInfo.debugSequenceAccept(dexFile, LocalVariableCollector(debugState, localVariableInfos, registerPrinter))
        }

        // collect branch target / label infos.
        code.instructionsAccept(dexFile, classDef, method, branchTargetPrinter)

        // print the instructions.
        code.instructionsAccept(
            dexFile, classDef, method,
            InstructionPrinter(printer, registerPrinter, branchTargetPrinter, debugState)
        )
    }
}