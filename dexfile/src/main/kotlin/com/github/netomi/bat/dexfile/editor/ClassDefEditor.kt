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

import com.github.netomi.bat.dexfile.*
import com.github.netomi.bat.dexfile.annotation.*
import com.github.netomi.bat.dexfile.annotation.Annotation
import com.github.netomi.bat.dexfile.util.DexType
import com.github.netomi.bat.dexfile.value.EncodedValue
import com.github.netomi.bat.util.VOID_TYPE
import java.util.*

class ClassDefEditor private constructor(val dexEditor: DexEditor, val classDef: ClassDef) {

    val dexFile: DexFile
        get() = dexEditor.dexFile

    val classType: DexType
        get() = classDef.getType(dexFile)

    val classTypeString: String
        get() = classType.type

    fun addInterface(type: DexType) {
        addInterface(type.type)
    }

    fun addInterface(type: String) {
        val typeIndex = dexEditor.addOrGetTypeIDIndex(type)
        if (!classDef.interfaces.contains(typeIndex)) {
            classDef.interfaces.addType(typeIndex)
        }
    }

    fun setVisibility(visibility: Visibility) {
        classDef.accessFlags = accessFlagsOf(visibility, classDef.modifiers)
    }

    fun addModifier(modifier: ClassModifier) {
        val currentModifiers = classDef.modifiers.toMutableSet()
        currentModifiers.add(modifier)
        classDef.accessFlags = accessFlagsOf(classDef.visibility, currentModifiers)
    }

    fun removeModifier(modifier: ClassModifier) {
        val currentModifiers = classDef.modifiers.toMutableSet()
        currentModifiers.remove(modifier)
        classDef.accessFlags = accessFlagsOf(classDef.visibility, currentModifiers)
    }

    fun addField(fieldName:  String,
                 visibility: Visibility = Visibility.PUBLIC,
                 modifiers:  EnumSet<FieldModifier> = EnumSet.noneOf(FieldModifier::class.java),
                 type:       String): FieldEditor {
        return addField(fieldName, accessFlagsOf(visibility, modifiers), type)
    }

    fun addField(fieldName: String, accessFlags: Int, type: DexType, validate: Boolean = true): FieldEditor {
        return addField(fieldName, accessFlags, type.type, validate)
    }

    fun addField(fieldName: String, accessFlags: Int, type: String, validate: Boolean = true): FieldEditor {
        val fieldIDIndex = dexEditor.addOrGetFieldIDIndex(classTypeString, fieldName, type)
        val field = EncodedField.of(fieldIDIndex, accessFlags)
        classDef.addField(dexFile, field, validate)
        return FieldEditor.of(dexEditor, this, field)
    }

    fun addMethod(methodName:     String,
                  visibility:     Visibility = Visibility.PUBLIC,
                  modifiers:      EnumSet<MethodModifier> = EnumSet.noneOf(MethodModifier::class.java),
                  parameterTypes: List<String> = emptyList(),
                  returnType:     String = VOID_TYPE): MethodEditor {
        return addMethod(methodName, accessFlagsOf(visibility, modifiers), parameterTypes, returnType)
    }

    fun addMethod(methodName: String, accessFlags: Int, parameterTypes: List<DexType>, returnType: DexType, validate: Boolean = true): MethodEditor {
        return addMethod(methodName, accessFlags, parameterTypes.map { it.type }, returnType.type, validate)
    }

    fun addMethod(methodName: String, accessFlags: Int, parameterTypes: List<String>, returnType: String, validate: Boolean = true): MethodEditor {
        val methodIDIndex = dexEditor.addOrGetMethodIDIndex(classTypeString, methodName, parameterTypes, returnType)
        val method = EncodedMethod.of(methodIDIndex, accessFlags)
        classDef.addMethod(dexFile, method, validate)
        return MethodEditor.of(dexEditor, this, method)
    }

    fun setStaticValue(field: EncodedField, value: EncodedValue) {
        classDef.setStaticValue(dexFile, field, value)
    }

    fun addClassAnnotation(annotation: Annotation) {
        classDef.annotationsDirectory.classAnnotations.addAnnotation(dexFile, annotation)
    }

    fun addOrGetFieldAnnotationSet(field: EncodedField): AnnotationSet {
        var fieldAnnotation =
            classDef.annotationsDirectory.fieldAnnotations.find { it.fieldIndex == field.fieldIndex }

        if (fieldAnnotation == null) {
            fieldAnnotation = FieldAnnotation.of(field.fieldIndex, AnnotationSet.empty())
            classDef.annotationsDirectory.fieldAnnotations.add(fieldAnnotation)
        }

        return fieldAnnotation.annotationSet
    }

    fun addOrGetMethodAnnotationSet(method: EncodedMethod): AnnotationSet {
        var methodAnnotation =
            classDef.annotationsDirectory.methodAnnotations.find { it.methodIndex == method.methodIndex }

        if (methodAnnotation == null) {
            methodAnnotation = MethodAnnotation.of(method.methodIndex, AnnotationSet.empty())
            classDef.annotationsDirectory.methodAnnotations.add(methodAnnotation)
        }

        return methodAnnotation.annotationSet
    }

    fun addOrGetParameterAnnotationSet(method: EncodedMethod, parameterIndex: Int): AnnotationSet {
        var parameterAnnotation =
            classDef.annotationsDirectory.parameterAnnotations.find { it.methodIndex == method.methodIndex }

        if (parameterAnnotation == null) {
            parameterAnnotation = ParameterAnnotation.of(method.methodIndex, AnnotationSetRefList.empty())
            classDef.annotationsDirectory.parameterAnnotations.add(parameterAnnotation)
        }

        val annotationSetRefList = parameterAnnotation.annotationSetRefList
        while (annotationSetRefList.size <= parameterIndex) {
            annotationSetRefList.addAnnotationSetRef(AnnotationSetRef.of(AnnotationSet.empty()))
        }

        return annotationSetRefList[parameterIndex].annotationSet
    }

    companion object {
        fun of(dexEditor: DexEditor, classDef: ClassDef): ClassDefEditor {
            return ClassDefEditor(dexEditor, classDef)
        }
    }
}