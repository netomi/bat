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
import com.github.netomi.bat.dexfile.annotation.Annotation
import com.github.netomi.bat.dexfile.editor.ClassDefEditor
import com.github.netomi.bat.dexfile.editor.DexEditor
import com.github.netomi.bat.dexfile.editor.MethodEditor
import com.github.netomi.bat.dexfile.util.DexClasses.externalClassNameFromInternalClassName
import com.github.netomi.bat.dexfile.util.DexClasses.fullExternalFieldDescriptor
import com.github.netomi.bat.dexfile.util.DexClasses.fullExternalMethodDescriptor
import com.github.netomi.bat.smali.parser.SmaliBaseVisitor
import com.github.netomi.bat.smali.parser.SmaliParser.*
import java.io.PrintWriter

internal class ClassDefAssembler(private val dexEditor:      DexEditor,
                                 private val lenientMode:    Boolean      = false,
                                 private val warningPrinter: PrintWriter? = null) : SmaliBaseVisitor<List<ClassDef>>() {

    private val dexFile: DexFile
        get() = dexEditor.dexFile

    private val encodedValueAssembler: EncodedValueAssembler = EncodedValueAssembler(dexEditor)
    private val annotationAssembler:   AnnotationAssembler   = AnnotationAssembler(encodedValueAssembler, dexEditor)

    private lateinit var classDefEditor: ClassDefEditor

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
                    val className = classDefEditor.classDef.getClassName(dexFile)
                    warningPrinter?.println(
                        "warning: class '%s': %s, skipping annotation"
                            .format(externalClassNameFromInternalClassName(className), exception.message))
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

        val field: EncodedField

        try {
            field = classDefEditor.addField(name, accessFlags, type)
        } catch (exception: RuntimeException) {
            if (lenientMode) {
                warningPrinter?.println("warning: ${exception.message}, skipping field")
                return emptyList()
            } else {
                throw exception
            }
        }

        if (field.isStatic && ctx.sBaseValue() != null) {
            val staticValue = encodedValueAssembler.parseBaseValue(ctx.sBaseValue())
            classDefEditor.setStaticValue(field, staticValue)
        }

        val annotations = mutableListOf<Annotation>()
        ctx.sAnnotation().forEach {
            annotations.add(annotationAssembler.parseAnnotation(it))
        }
        if (annotations.isNotEmpty()) {
            val annotationSet = classDefEditor.addOrGetFieldAnnotationSet(field)
            for (annotation in annotations) {
                try {
                    annotationSet.addAnnotation(dexFile, annotation)
                } catch (exception: RuntimeException) {
                    if (lenientMode) {
                        warningPrinter?.println(
                            "warning: field '%s': %s, skipping annotation"
                                .format(fullExternalFieldDescriptor(dexFile, field), exception.message))
                    } else {
                        throw exception
                    }
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
            methodEditor = classDefEditor.addMethod(name, accessFlags, parameterTypes, returnType)
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
                val message = "abstract method '${fullExternalMethodDescriptor(dexFile, method)}' containing code instructions"
                if (lenientMode) {
                    warningPrinter?.println("warning: $message, skipping code")
                } else {
                    parserError(ctx, message)
                }
            }
        }

        // ignore duplicate annotations that are encountered in obfuscated dex files.
        val methodAnnotations = mutableListOf<Annotation>()
        ctx.sAnnotation().forEach { methodAnnotations.add(annotationAssembler.parseAnnotation(it)) }
        if (methodAnnotations.isNotEmpty()) {
            val annotationSet = classDefEditor.addOrGetMethodAnnotationSet(method)
            for (annotation in methodAnnotations) {
                try {
                    annotationSet.addAnnotation(dexFile, annotation)
                } catch (exception: RuntimeException) {
                    if (lenientMode) {
                        warningPrinter?.println(
                            "warning: method '%s': %s, skipping annotation"
                                .format(fullExternalMethodDescriptor(dexFile, method), exception.message))
                    } else {
                        throw exception
                    }
                }
            }
        }

        ctx.sParameter().forEach { pCtx ->
            val parameterIndex = parseParameterIndex(pCtx, dexFile, method)

            val parameterAnnotations = mutableListOf<Annotation>()
            pCtx.sAnnotation().forEach { parameterAnnotations.add(annotationAssembler.parseAnnotation(it)) }
            if (parameterAnnotations.isNotEmpty()) {
                val annotationSet = classDefEditor.addOrGetParameterAnnotationSet(method, parameterIndex)
                for (annotation in parameterAnnotations) {
                    try {
                        annotationSet.addAnnotation(dexFile, annotation)
                    } catch (exception: RuntimeException) {
                        if (lenientMode) {
                            warningPrinter?.println(
                                "warning: parameter #%d at '%s': %s, skipping annotation"
                                    .format(parameterIndex + 1,
                                            fullExternalMethodDescriptor(dexFile, method),
                                            exception.message))
                        } else {
                            throw exception
                        }
                    }
                }
            }
        }

        return emptyList()
    }
}