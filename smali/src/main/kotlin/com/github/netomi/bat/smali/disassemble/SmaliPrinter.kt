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
import com.github.netomi.bat.dexfile.annotation.*
import com.github.netomi.bat.dexfile.annotation.Annotation
import com.github.netomi.bat.dexfile.util.DexClasses
import com.github.netomi.bat.dexfile.value.AnnotationElement
import com.github.netomi.bat.dexfile.visitor.*
import com.github.netomi.bat.io.IndentingPrinter
import com.github.netomi.bat.util.Classes
import java.io.OutputStreamWriter
import java.io.Writer
import java.util.*

class SmaliPrinter constructor(writer: Writer = OutputStreamWriter(System.out)) :
    ClassDefVisitor, ClassDataVisitor, AnnotationSetVisitor, AnnotationVisitor, AnnotationElementVisitor,
    EncodedFieldVisitor, EncodedMethodVisitor, CodeVisitor {

    private val printer: IndentingPrinter = IndentingPrinter(writer, 4)

    private var printParameterInfo    = false
    private var currentParameterIndex = 0
    private var currentRegisterIndex  = 0

    private var currentParameterType: String? = null

    override fun visitClassDef(dexFile: DexFile, index: Int, classDef: ClassDef) {
        printer.print(".class")

        val accessFlags =
            DexAccessFlags.formatAsHumanReadable(classDef.accessFlags, DexAccessFlags.Target.CLASS)
                          .lowercase(Locale.getDefault())

        if (accessFlags.isNotEmpty()) {
            printer.print(" $accessFlags")
        }

        printer.print(" " + Classes.internalTypeFromClassName(classDef.getClassName(dexFile)))
        printer.println()
        printer.println(".super " + classDef.getSuperClassType(dexFile))

        val sourceFile = classDef.getSourceFile(dexFile)
        if (sourceFile != null) {
            printer.println(".source \"$sourceFile\"")
        }

        classDef.interfaceListAccept(dexFile) { df, typeList ->
            if (!typeList.isEmpty) {
                printer.println()
                printer.println("# interfaces")
                classDef.interfacesAccept(df) { _, _, _, type: String -> printer.println(".implements $type") }
            }
        }

        classDef.annotationsDirectory.classAnnotationSetAccept(dexFile, classDef, this)
        classDef.classDataAccept(dexFile, this)
        printer.flush()
    }

    override fun visitClassData(dexFile: DexFile, classDef: ClassDef, classData: ClassData) {
        if (classData.staticFieldCount > 0) {
            printer.println()
            printer.println()
            printer.println("# static fields")
            classData.staticFieldsAccept(dexFile, classDef, joinedByFieldConsumer { _, _ -> printer.println() })
        }
        if (classData.instanceFieldCount > 0) {
            printer.println()
            printer.println()
            printer.println("# instance fields")
            classData.instanceFieldsAccept(dexFile, classDef, joinedByFieldConsumer { _, _ -> printer.println() })
        }
        if (classData.directMethodCount > 0) {
            printer.println()
            printer.println()
            printer.println("# direct methods")
            classData.directMethodsAccept(dexFile, classDef, joinedBy { _, _ -> printer.println() })
        }
        if (classData.virtualMethodCount > 0) {
            printer.println()
            printer.println()
            printer.println("# virtual methods")
            classData.virtualMethodsAccept(dexFile, classDef, joinedBy { _, _ -> printer.println() })
        }
    }

    override fun visitAnyField(dexFile: DexFile, classDef: ClassDef, index: Int, field: EncodedField) {
        printer.print(".field")

        val accessFlags =
            DexAccessFlags.formatAsHumanReadable(field.accessFlags, DexAccessFlags.Target.FIELD)
                          .lowercase(Locale.getDefault())

        if (accessFlags.isNotEmpty()) {
            printer.print(" $accessFlags")
        }

        printer.print(" " + field.getName(dexFile) + ":" + field.getType(dexFile))

        if (field.isStatic) {
            val detector = InitializationDetector(field.getName(dexFile), field.getType(dexFile))
            classDef.methodsAccept(dexFile, "<clinit>", allCode(AllInstructionsVisitor(detector)))

            if (!detector.fieldIsSetInStaticInitializer || !field.modifiers.contains(FieldModifier.FINAL)) {
                field.staticValueAccept(dexFile, classDef, index, EncodedValuePrinter(printer, null, " = "))
            }
        }

        printer.println()
        classDef.annotationsDirectory.fieldAnnotationSetAccept(dexFile, classDef, field, this)
    }

    override fun visitAnyMethod(dexFile: DexFile, classDef: ClassDef, index: Int, method: EncodedMethod) {
        printer.print(".method")

        val accessFlags =
            DexAccessFlags.formatAsHumanReadable(method.accessFlags, DexAccessFlags.Target.METHOD)
                          .replace("_".toRegex(), "-")
                          .lowercase(Locale.getDefault())

        if (accessFlags.isNotEmpty()) {
            printer.print(" $accessFlags")
        }

        printer.println(" " + method.getName(dexFile) + method.getDescriptor(dexFile))

        // print code.
        printer.levelUp()
        if (method.code != null && !method.isAbstract) {
            method.codeAccept(dexFile, classDef, this)
        } else if (!classDef.annotationsDirectory.isEmpty) {
            var registerIndex = if (method.isStatic) 0 else 1
            val protoID = method.getProtoID(dexFile)
            val parameters = protoID.parameters

            for ((parameterIndex, parameterType) in parameters.getTypes(dexFile).withIndex()) {
                printParameterInfo    = true
                currentParameterIndex = parameterIndex
                currentParameterType  = parameterType
                currentRegisterIndex  = registerIndex
                classDef.annotationsDirectory.parameterAnnotationSetAccept(dexFile, classDef, method, this)

                registerIndex += DexClasses.getArgumentSizeForType(parameterType)
            }

            printParameterInfo = false
            classDef.annotationsDirectory.methodAnnotationSetAccept(dexFile, classDef, method, this)
        }

        printer.levelDown()
        printer.println(".end method")
    }

    override fun visitCode(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code) {
        printer.println(".registers " + code.registersSize)

        val localVariableInfos = arrayOfNulls<LocalVariableInfo>(code.registersSize)
        if (code.debugInfo != null) {
            val localVariables = code.registersSize - code.insSize
            var registerIndex  = if (method.isStatic) 0 else 1

            if (!method.isStatic) {
                val classType = method.getClassType(dexFile)
                localVariableInfos[localVariables] = LocalVariableInfo("this", classType, null)
            }

            val protoID    = method.getProtoID(dexFile)
            val parameters = protoID.parameters

            for ((parameterIndex, parameterType) in parameters.getTypes(dexFile).withIndex()) {
                currentParameterIndex = parameterIndex
                val parameterName     = code.debugInfo.getParameterName(dexFile, parameterIndex)
                if (parameterName != null) {
                    printer.println(".param p%d, \"%s\"    # %s".format(registerIndex, parameterName, parameterType))
                } else {
                    currentRegisterIndex = registerIndex
                    currentParameterType = parameterType
                    printParameterInfo   = true
                }

                val localVariableInfo = LocalVariableInfo(parameterName, parameterType, null)
                localVariableInfos[localVariables + registerIndex] = localVariableInfo
                classDef.annotationsDirectory.parameterAnnotationSetAccept(dexFile, classDef, method, this)

                printParameterInfo = false
                registerIndex += DexClasses.getArgumentSizeForType(parameterType)
            }
        }
        classDef.annotationsDirectory.methodAnnotationSetAccept(dexFile, classDef, method, this)

        val registerPrinter     = RegisterPrinter(code)
        val branchTargetPrinter = BranchTargetPrinter()
        val debugState: MutableMap<Int, MutableList<String>> = HashMap()

        if (code.debugInfo != null) {
            code.debugInfo.debugSequenceAccept(dexFile, SourceLineCollector(debugState, code.debugInfo.lineStart))
            code.debugInfo.debugSequenceAccept(dexFile, LocalVariableCollector(debugState, localVariableInfos, registerPrinter))
        }

        // collect branch target / label infos.
        code.instructionsAccept(dexFile, classDef, method, code, branchTargetPrinter)

        // print the instructions.
        code.instructionsAccept(
            dexFile, classDef, method, code,
            InstructionPrinter(printer, registerPrinter, branchTargetPrinter, debugState)
        )
    }

    override fun visitAnyAnnotationSet(dexFile: DexFile, classDef: ClassDef, annotationSet: AnnotationSet) {}

    override fun visitClassAnnotationSet(dexFile: DexFile, classDef: ClassDef, annotationSet: AnnotationSet) {
        val annotationCount = annotationSet.annotationCount
        if (annotationCount > 0) {
            printer.println()
            printer.println()
            printer.println("# annotations")
            annotationSet.accept(dexFile, classDef, joinedByAnnotationConsumer { _, _ -> printer.println() })
        }
    }

    override fun visitFieldAnnotationSet(dexFile: DexFile, classDef: ClassDef, fieldAnnotation: FieldAnnotation, annotationSet: AnnotationSet) {
        val annotationCount = annotationSet.annotationCount
        if (annotationCount > 0) {
            printer.levelUp()
            annotationSet.accept(dexFile, classDef, joinedByAnnotationConsumer { _, _ -> printer.println() })
            printer.levelDown()
            printer.println(".end field")
        }
    }

    override fun visitMethodAnnotationSet(dexFile: DexFile, classDef: ClassDef, methodAnnotation: MethodAnnotation, annotationSet: AnnotationSet) {
        annotationSet.accept(dexFile, classDef, joinedByAnnotationConsumer { _, _ -> printer.println() } )
    }

    override fun visitParameterAnnotationSet(dexFile: DexFile, classDef: ClassDef, parameterAnnotation: ParameterAnnotation, annotationSetRefList: AnnotationSetRefList) {
        if (currentParameterIndex < annotationSetRefList.annotationSetRefCount) {
            val annotationSetRef = annotationSetRefList.getAnnotationSetRef(currentParameterIndex)
            val annotationSet = annotationSetRef.annotationSet
            if (!annotationSet.isEmpty) {
                if (printParameterInfo) {
                    printer.println(".param p%d    # %s".format(currentRegisterIndex, currentParameterType))
                }
                printer.levelUp()
                annotationSet.accept(dexFile, classDef, this)
                printer.levelDown()
                printer.println(".end param")
            }
        }
    }

    override fun visitAnnotation(dexFile: DexFile, classDef: ClassDef, annotationSet: AnnotationSet, index: Int, annotation: Annotation) {
        printer.print(".annotation " + annotation.visibility.simpleName + " ")
        val annotationValue = annotation.annotationValue
        printer.println(annotationValue.getType(dexFile))
        printer.levelUp()
        annotationValue.annotationElementsAccept(dexFile, this)
        printer.levelDown()
        printer.println(".end annotation")
    }

    override fun visitAnnotationElement(dexFile: DexFile, element: AnnotationElement) {
        printer.print(element.getName(dexFile))
        printer.print(" = ")
        element.value.accept(dexFile, EncodedValuePrinter(printer, this))
        printer.println()
    }
}