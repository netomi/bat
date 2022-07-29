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

package com.github.netomi.bat.dexfile.editor

import com.github.netomi.bat.dexfile.ClassDef
import com.github.netomi.bat.dexfile.DexFile
import com.github.netomi.bat.dexfile.EncodedField
import com.github.netomi.bat.dexfile.EncodedMethod
import com.github.netomi.bat.dexfile.annotation.*
import com.github.netomi.bat.dexfile.annotation.Annotation
import com.github.netomi.bat.dexfile.value.EncodedValue

class ClassDefEditor private constructor(private val dexEditor: DexEditor, val classDef: ClassDef) {

    private val dexFile: DexFile
        get() = dexEditor.dexFile

    private val classType: String
        get() = classDef.getType(dexFile)

    fun addInterface(type: String) {
        classDef.interfaces.addType(dexEditor.addOrGetTypeIDIndex(type))
    }

    fun addField(fieldName: String, type: String, accessFlags: Int): EncodedField {
        val fieldIDIndex = dexEditor.addOrGetFieldIDIndex(classType, fieldName, type)
        val field = EncodedField.of(fieldIDIndex, accessFlags)
        classDef.addField(dexFile, field)
        return field
    }

    fun addMethod(methodName: String, parameterTypes: List<String>, returnType: String, accessFlags: Int): MethodEditor {
        val methodIDIndex = dexEditor.addOrGetMethodIDIndex(classType, methodName, parameterTypes, returnType)
        val method = EncodedMethod.of(methodIDIndex, accessFlags)
        classDef.addMethod(dexFile, method)
        return MethodEditor.of(dexEditor, classDef, method)
    }

    fun setStaticValue(field: EncodedField, value: EncodedValue) {
        classDef.setStaticValue(dexFile, field, value)
    }

    fun addClassAnnotations(annotations: List<Annotation>) {
        val existingAnnotations = classDef.annotationsDirectory.classAnnotations.annotations
        existingAnnotations.addAll(annotations.filter { !existingAnnotations.contains(it) })
    }

    fun addFieldAnnotations(field: EncodedField, annotations: List<Annotation>) {
        var fieldAnnotation =
            classDef.annotationsDirectory.fieldAnnotations.find { it.fieldIndex == field.fieldIndex }

        if (fieldAnnotation == null) {
            fieldAnnotation = FieldAnnotation.of(field.fieldIndex, AnnotationSet.of(annotations))
            classDef.annotationsDirectory.fieldAnnotations.add(fieldAnnotation)
        } else {
            val existingAnnotations = fieldAnnotation.annotationSet.annotations
            existingAnnotations.addAll(annotations.filter { !existingAnnotations.contains(it) })
        }
    }

    fun addMethodAnnotations(method: EncodedMethod, annotations: List<Annotation>) {
        var methodAnnotation =
            classDef.annotationsDirectory.methodAnnotations.find { it.methodIndex == method.methodIndex }

        if (methodAnnotation == null) {
            methodAnnotation = MethodAnnotation.of(method.methodIndex, AnnotationSet.of(annotations))
            classDef.annotationsDirectory.methodAnnotations.add(methodAnnotation)
        } else {
            val existingAnnotations = methodAnnotation.annotationSet.annotations
            existingAnnotations.addAll(annotations.filter { !existingAnnotations.contains(it) })
        }
    }

    fun addParameterAnnotations(method: EncodedMethod, parameterIndex: Int, annotations: List<Annotation>) {
        var parameterAnnotation =
            classDef.annotationsDirectory.parameterAnnotations.find { it.methodIndex == method.methodIndex }

        if (parameterAnnotation == null) {
            parameterAnnotation = ParameterAnnotation.of(method.methodIndex, AnnotationSetRefList.empty())
            classDef.annotationsDirectory.parameterAnnotations.add(parameterAnnotation)
        }

        val annotationSetRefList = parameterAnnotation.annotationSetRefList
        while (annotationSetRefList.annotationSetRefCount <= parameterIndex) {
            annotationSetRefList.annotationSetRefs.add(AnnotationSetRef.of(AnnotationSet.empty()))
        }

        val existingAnnotations = annotationSetRefList.getAnnotationSetRef(parameterIndex).annotationSet.annotations
        existingAnnotations.addAll(annotations.filter { !existingAnnotations.contains(it) })
    }

    companion object {
        fun of(dexEditor: DexEditor, classDef: ClassDef): ClassDefEditor {
            return ClassDefEditor(dexEditor, classDef)
        }
    }
}