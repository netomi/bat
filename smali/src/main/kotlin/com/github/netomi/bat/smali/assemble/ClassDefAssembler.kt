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
package com.github.netomi.bat.smali.assemble

import com.github.netomi.bat.dexfile.*
import com.github.netomi.bat.dexfile.editor.ClassDefEditor
import com.github.netomi.bat.dexfile.editor.DexEditor
import com.github.netomi.bat.dexfile.editor.FieldEditor
import com.github.netomi.bat.dexfile.editor.MethodEditor
import com.github.netomi.bat.smali.parser.SmaliBaseVisitor
import com.github.netomi.bat.smali.parser.SmaliParser.*
import com.github.netomi.bat.util.asInternalJavaClassName
import java.io.PrintWriter

internal class ClassDefAssembler(private val dexEditor:      DexEditor,
                                 private val lenientMode:    Boolean      = false,
                                 private val warningPrinter: PrintWriter? = null) : SmaliBaseVisitor<List<ClassDef>>() {

    private val dexFile: DexFile
        get() = dexEditor.dexFile

    private val encodedValueAssembler: EncodedValueAssembler = EncodedValueAssembler(dexEditor)
    private val annotationAssembler:   AnnotationAssembler   = AnnotationAssembler(encodedValueAssembler, dexEditor)

    private lateinit var classDefEditor: ClassDefEditor

    private val addedFields  = mutableSetOf<FieldID>()
    private val addedMethods = mutableSetOf<MethodID>()

    override fun aggregateResult(aggregate: List<ClassDef>?, nextResult: List<ClassDef>?): List<ClassDef> {
        return (aggregate ?: emptyList()) + (nextResult ?: emptyList())
    }

    override fun visitSFile(ctx: SFileContext): List<ClassDef> {
        val classType   = ctx.className.text
        val superType   = ctx.sSuper().firstOrNull()?.name?.text
        val sourceFile  = ctx.sSource().firstOrNull()?.src?.text?.removeSurrounding("\"")
        val accessFlags = parseAccessFlags(ctx.sAccList())

        classDefEditor = dexEditor.addClassDef(classType, accessFlags, superType, sourceFile)

        ctx.sInterface().forEach {
            classDefEditor.addInterface(it.name.text)
        }

        ctx.sAnnotation().forEach {
            val annotation = annotationAssembler.parseAnnotation(it)
            try {
                classDefEditor.addClassAnnotation(annotation)
            } catch (exception: RuntimeException) {
                if (lenientMode) {
                    val externalClassName =
                        classDefEditor.classDef.getClassName(dexFile).asInternalJavaClassName().toExternalClassName()
                    warningPrinter?.println(
                        "warning: class '%s': %s, skipping annotation"
                            .format(externalClassName, exception.message))
                } else {
                    throw exception
                }
            }
        }

        ctx.sField().forEach  { visitSField(it) }
        ctx.sMethod().forEach { visitSMethod(it) }

        return listOf(classDefEditor.classDef)
    }

    override fun visitSField(ctx: SFieldContext): List<ClassDef> {
        val (_, name, type) = parseFieldObject(ctx.fieldObj.text)
        val accessFlags  = parseAccessFlags(ctx.sAccList())

        val fieldEditor: FieldEditor

        try {
            val fieldIDIndex = dexEditor.addOrGetFieldIDIndex(classDefEditor.classType, name, type)
            val fieldID      = dexFile.getFieldID(fieldIDIndex)
            if (addedFields.contains(fieldID)) {
                throw RuntimeException("field '${fieldID.getFullExternalFieldDescriptor(dexFile)}' already exists in this class")
            }
            fieldEditor = classDefEditor.addField(name, accessFlags, type, false)
            addedFields.add(fieldID)
        } catch (exception: RuntimeException) {
            if (lenientMode) {
                warningPrinter?.println("warning: ${exception.message}, skipping field")
                return emptyList()
            } else {
                throw exception
            }
        }

        val field = fieldEditor.field
        if (field.isStatic && ctx.sBaseValue() != null) {
            val staticValue = encodedValueAssembler.parseBaseValue(ctx.sBaseValue())
            fieldEditor.setStaticValue(staticValue)
        }

        ctx.sAnnotation().forEach {
            val annotation = annotationAssembler.parseAnnotation(it)
            try {
                fieldEditor.addAnnotation(annotation)
            } catch (exception: RuntimeException) {
                if (lenientMode) {
                    val fieldID = field.getFieldID(dexFile)
                    warningPrinter?.println(
                        "warning: field '%s': %s, skipping annotation"
                            .format(fieldID.getFullExternalFieldDescriptor(dexFile), exception.message))
                } else {
                    throw exception
                }
            }
        }

        return emptyList()
    }

    override fun visitSMethod(ctx: SMethodContext): List<ClassDef> {
        val (_, name, parameterTypes, returnType) = parseMethodObject(ctx.methodObj.text)
        val accessFlags = parseAccessFlags(ctx.sAccList())

        val methodEditor: MethodEditor

        try {
            val methodIDIndex = dexEditor.addOrGetMethodIDIndex(classDefEditor.classType, name, parameterTypes, returnType)
            val methodID      = dexFile.getMethodID(methodIDIndex)
            if (addedMethods.contains(methodID)) {
                throw RuntimeException("method '${methodID.getFullExternalMethodDescriptor(dexFile)}' already exists in this class")
            }
            methodEditor = classDefEditor.addMethod(name, accessFlags, parameterTypes, returnType, false)
            addedMethods.add(methodID)
        } catch (exception: RuntimeException) {
            if (lenientMode) {
                warningPrinter?.println("warning: ${exception.message}, skipping method")
                return emptyList()
            } else {
                throw exception
            }
        }

        val method = methodEditor.method
        if (!method.isAbstract && !method.isNative) {
            val codeEditor = methodEditor.addCode()
            val codeAssembler = CodeAssembler(method, codeEditor, lenientMode)
            codeAssembler.parseCode(ctx.sInstruction(), ctx.sParameter())
        } else {
            if (ctx.sInstruction().isNotEmpty()) {
                val methodDescriptor = method.getMethodID(dexFile).getFullExternalMethodDescriptor(dexFile)
                val message = "abstract method '$methodDescriptor' containing code instructions"
                if (lenientMode) {
                    warningPrinter?.println("warning: $message, skipping code")
                } else {
                    parserError(ctx, message)
                }
            }
        }

        ctx.sAnnotation().forEach {
            val annotation = annotationAssembler.parseAnnotation(it)
            try {
                methodEditor.addAnnotation(annotation)
            } catch (exception: RuntimeException) {
                if (lenientMode) {
                    val methodID = method.getMethodID(dexFile)
                    warningPrinter?.println(
                        "warning: method '%s': %s, skipping annotation"
                            .format(methodID.getFullExternalMethodDescriptor(dexFile), exception.message))
                } else {
                    throw exception
                }
            }
        }

        ctx.sParameter().forEach { pCtx ->
            val parameterIndex = parseParameterIndex(pCtx, dexFile, method)

            pCtx.sAnnotation().forEach {
                val annotation = annotationAssembler.parseAnnotation(it)
                try {
                    methodEditor.addParameterAnnotation(parameterIndex, annotation)
                } catch (exception: RuntimeException) {
                    if (lenientMode) {
                        val methodID = method.getMethodID(dexFile)
                        warningPrinter?.println(
                            "warning: parameter #%d at '%s': %s, skipping annotation"
                                .format(parameterIndex + 1,
                                    methodID.getFullExternalMethodDescriptor(dexFile),
                                    exception.message))
                    } else {
                        throw exception
                    }
                }
            }
        }

        return emptyList()
    }
}