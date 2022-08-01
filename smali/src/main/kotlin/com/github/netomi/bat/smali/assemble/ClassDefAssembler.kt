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
import com.github.netomi.bat.smali.parser.SmaliBaseVisitor
import com.github.netomi.bat.smali.parser.SmaliParser.*

internal class ClassDefAssembler(private val dexEditor: DexEditor) : SmaliBaseVisitor<ClassDef?>() {

    private val dexFile: DexFile
        get() = dexEditor.dexFile

    private val encodedValueAssembler: EncodedValueAssembler = EncodedValueAssembler(dexEditor)
    private val annotationAssembler:   AnnotationAssembler   = AnnotationAssembler(encodedValueAssembler, dexEditor)

    private lateinit var classDefEditor: ClassDefEditor

    override fun visitSFile(ctx: SFileContext): ClassDef {
        val classType   = ctx.className.text
        val superType   = ctx.sSuper().firstOrNull()?.name?.text
        val sourceFile  = ctx.sSource().firstOrNull()?.src?.text?.removeSurrounding("\"")
        val accessFlags = parseAccessFlags(ctx.sAccList())

        classDefEditor = dexEditor.addClassDef(classType, accessFlags, superType, sourceFile)

        ctx.sInterface().forEach {
            classDefEditor.addInterface(it.name.text)
        }

        val annotations = mutableListOf<Annotation>()
        ctx.sAnnotation().forEach { annotations.add(annotationAssembler.parseAnnotation(it)) }
        classDefEditor.addClassAnnotations(annotations)

        ctx.sField().forEach  { visitSField(it) }
        ctx.sMethod().forEach { visitSMethod(it) }

        return classDefEditor.classDef
    }

    override fun visitSField(ctx: SFieldContext): ClassDef? {
        val (_, name, type) = parseFieldObject(ctx.fieldObj.text)
        val accessFlags  = parseAccessFlags(ctx.sAccList())

        val field = classDefEditor.addField(name, accessFlags, type)

        if (field.isStatic && ctx.sBaseValue() != null) {
            val staticValue = encodedValueAssembler.parseBaseValue(ctx.sBaseValue())
            classDefEditor.setStaticValue(field, staticValue)
        }

        val annotations = mutableListOf<Annotation>()
        ctx.sAnnotation().forEach { annotations.add(annotationAssembler.parseAnnotation(it)) }
        if (annotations.isNotEmpty()) {
            classDefEditor.addFieldAnnotations(field, annotations)
        }

        return null
    }

    override fun visitSMethod(ctx: SMethodContext): ClassDef? {
        val (_, name, parameterTypes, returnType) = parseMethodObject(ctx.methodObj.text)
        val accessFlags = parseAccessFlags(ctx.sAccList())

        val methodEditor = classDefEditor.addMethod(name, accessFlags, parameterTypes, returnType)
        val method       = methodEditor.method

        if (!method.isAbstract && !method.isNative) {
            val codeEditor = methodEditor.addCode()
            val codeAssembler = CodeAssembler(method, codeEditor)
            codeAssembler.parseCode(ctx.sInstruction(), ctx.sParameter())
        } else {
            if (ctx.sInstruction().isNotEmpty()) {
                parserError(ctx, "abstract method containing code instructions")
            }
        }

        val methodAnnotations = mutableListOf<Annotation>()
        ctx.sAnnotation().forEach { methodAnnotations.add(annotationAssembler.parseAnnotation(it)) }
        if (methodAnnotations.isNotEmpty()) {
            classDefEditor.addMethodAnnotations(method, methodAnnotations)
        }

        ctx.sParameter().forEach { pCtx ->
            val parameterIndex = parseParameterIndex(pCtx, dexFile, method)

            val parameterAnnotations = mutableListOf<Annotation>()
            pCtx.sAnnotation().forEach { parameterAnnotations.add(annotationAssembler.parseAnnotation(it)) }
            if (parameterAnnotations.isNotEmpty()) {
                classDefEditor.addParameterAnnotations(method, parameterIndex, parameterAnnotations)
            }
        }

        return null
    }
}