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
import com.github.netomi.bat.dexfile.annotation.*
import com.github.netomi.bat.dexfile.editor.DexComposer
import com.github.netomi.bat.smali.parser.SmaliBaseVisitor
import com.github.netomi.bat.smali.parser.SmaliParser.*

internal class ClassDefAssembler(private val dexFile: DexFile) : SmaliBaseVisitor<ClassDef?>() {

    private val dexComposer:           DexComposer           = dexFile.composer
    private val encodedValueAssembler: EncodedValueAssembler = EncodedValueAssembler(dexComposer)
    private val annotationAssembler:   AnnotationAssembler   = AnnotationAssembler(encodedValueAssembler, dexComposer)

    private lateinit var classDef: ClassDef

    override fun visitSFile(ctx: SFileContext): ClassDef {
        val classType   = ctx.className.text
        val superType   = ctx.sSuper().firstOrNull()?.name?.text
        val sourceFile  = ctx.sSource().firstOrNull()?.src?.text?.removeSurrounding("\"")
        val accessFlags = parseAccessFlags(ctx.sAccList())

        val classTypeIndex  = dexComposer.addOrGetTypeIDIndex(classType)
        val superTypeIndex  = if (superType != null) dexComposer.addOrGetTypeIDIndex(superType) else NO_INDEX
        val sourceFileIndex = if (sourceFile != null) dexComposer.addOrGetStringIDIndex(sourceFile) else NO_INDEX

        classDef =
            ClassDef.of(classTypeIndex,
                        accessFlags,
                        superTypeIndex,
                        sourceFileIndex)

        dexFile.addClassDef(classDef)

        ctx.sInterface().forEach {
            classDef.interfaces.addType(dexComposer.addOrGetTypeIDIndex(it.name.text))
        }

        val annotationSet = classDef.annotationsDirectory.classAnnotations
        ctx.sAnnotation().forEach { annotationSet.addAnnotation(annotationAssembler.parseAnnotation(it)) }

        ctx.sField().forEach  { visitSField(it, classType) }
        ctx.sMethod().forEach { visitSMethod(it, classType) }

        return classDef
    }

    private fun visitSField(ctx: SFieldContext, classType: String) {
        val (_, name, type) = parseFieldObject(ctx.fieldObj.text)
        val accessFlags  = parseAccessFlags(ctx.sAccList())

        val fieldIDIndex = dexComposer.addOrGetFieldIDIndex(classType, name, type)
        val field = EncodedField.of(fieldIDIndex, accessFlags)

        classDef.addField(dexFile, field)

        if (field.isStatic && ctx.sBaseValue() != null) {
            val staticValue = encodedValueAssembler.parseBaseValue(ctx.sBaseValue())
            classDef.setStaticValue(dexFile, field, staticValue)
        }

        val annotationSet = AnnotationSet.empty()
        ctx.sAnnotation().forEach { annotationSet.addAnnotation(annotationAssembler.parseAnnotation(it)) }
        if (!annotationSet.isEmpty) {
            val fieldAnnotation = FieldAnnotation.of(field.fieldIndex, annotationSet)
            classDef.annotationsDirectory.fieldAnnotations.add(fieldAnnotation)
        }
    }

    private fun visitSMethod(ctx: SMethodContext, classType: String) {
        val (_, name, parameterTypes, returnType) = parseMethodObject(ctx.methodObj.text)
        val accessFlags = parseAccessFlags(ctx.sAccList())

        val methodIDIndex =
            dexComposer.addOrGetMethodIDIndex(classType,
                                              name,
                                              parameterTypes,
                                              returnType)

        val method = EncodedMethod.of(methodIDIndex, accessFlags)

        if (!method.isAbstract) {
            val codeAssembler = CodeAssembler(classDef, method, dexComposer)
            val code = codeAssembler.parseCode(ctx.sInstruction())
            method.code = code
        } else {
            if (ctx.sInstruction().isNotEmpty()) {
                parserError(ctx, "abstract method containing code instructions")
            }
        }

        classDef.addMethod(dexFile, method)

        val annotationSet = AnnotationSet.empty()
        ctx.sAnnotation().forEach { annotationSet.addAnnotation(annotationAssembler.parseAnnotation(it)) }
        if (!annotationSet.isEmpty) {
            val methodAnnotation = MethodAnnotation.of(method.methodIndex, annotationSet)
            classDef.annotationsDirectory.methodAnnotations.add(methodAnnotation)
        }
    }
}