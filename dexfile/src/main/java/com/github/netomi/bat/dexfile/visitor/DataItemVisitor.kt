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
package com.github.netomi.bat.dexfile.visitor

import com.github.netomi.bat.dexfile.*
import com.github.netomi.bat.dexfile.annotation.*
import com.github.netomi.bat.dexfile.annotation.Annotation
import com.github.netomi.bat.dexfile.debug.DebugInfo

internal interface DataItemVisitor {
    fun visitAnyDataItem(dexFile: DexFile, dataItem: DataItem) {
        throw RuntimeException("Need to implement in class '${this.javaClass.name}'.")
    }

    fun visitHeader(dexFile: DexFile, header: DexHeader) {
        visitAnyDataItem(dexFile, header)
    }

    fun visitMapList(dexFile: DexFile, mapList: MapList) {
        visitAnyDataItem(dexFile, mapList)
    }

    fun visitStringID(dexFile: DexFile, stringID: StringID) {
        visitAnyDataItem(dexFile, stringID)
    }

    fun visitStringData(dexFile: DexFile, stringID: StringID, stringData: StringData) {
        visitAnyDataItem(dexFile, stringData)
    }

    fun visitTypeID(dexFile: DexFile, typeID: TypeID) {
        visitAnyDataItem(dexFile, typeID)
    }

    fun visitProtoID(dexFile: DexFile, protoID: ProtoID) {
        visitAnyDataItem(dexFile, protoID)
    }

    fun visitParameterTypes(dexFile: DexFile, protoID: ProtoID, typeList: TypeList) {
        visitAnyDataItem(dexFile, typeList)
    }

    fun visitFieldID(dexFile: DexFile, fieldID: FieldID) {
        visitAnyDataItem(dexFile, fieldID)
    }

    fun visitMethodID(dexFile: DexFile, methodID: MethodID) {
        visitAnyDataItem(dexFile, methodID)
    }

    fun visitClassDef(dexFile: DexFile, classDef: ClassDef) {
        visitAnyDataItem(dexFile, classDef)
    }

    fun visitClassData(dexFile: DexFile, classDef: ClassDef, classData: ClassData) {
        visitAnyDataItem(dexFile, classData)
    }

    fun visitInterfaceTypes(dexFile: DexFile, classDef: ClassDef, typeList: TypeList) {
        visitAnyDataItem(dexFile, typeList)
    }

    fun visitStaticValuesArray(dexFile: DexFile, classDef: ClassDef, encodedArray: EncodedArray) {
        visitAnyDataItem(dexFile, encodedArray)
    }

    fun visitAnnotationsDirectory(dexFile: DexFile, classDef: ClassDef, annotationsDirectory: AnnotationsDirectory) {
        visitAnyDataItem(dexFile, annotationsDirectory)
    }

    fun visitAnyAnnotationSet(dexFile: DexFile, annotationSet: AnnotationSet) {
        visitAnyDataItem(dexFile, annotationSet)
    }

    fun visitClassAnnotations(dexFile: DexFile, annotationsDirectory: AnnotationsDirectory, annotationSet: AnnotationSet) {
        visitAnyAnnotationSet(dexFile, annotationSet)
    }

    fun visitFieldAnnotations(dexFile: DexFile, fieldAnnotation: FieldAnnotation, annotationSet: AnnotationSet) {
        visitAnyAnnotationSet(dexFile, annotationSet)
    }

    fun visitMethodAnnotations(dexFile: DexFile, methodAnnotation: MethodAnnotation, annotationSet: AnnotationSet) {
        visitAnyAnnotationSet(dexFile, annotationSet)
    }

    fun visitParameterAnnotations(dexFile: DexFile, parameterAnnotation: ParameterAnnotation, annotationSetRefList: AnnotationSetRefList) {
        visitAnyDataItem(dexFile, annotationSetRefList)
    }

    fun visitAnnotationSet(dexFile: DexFile, annotationSetRef: AnnotationSetRef, annotationSet: AnnotationSet) {
        visitAnyAnnotationSet(dexFile, annotationSet)
    }

    fun visitAnnotation(dexFile: DexFile, annotationSet: AnnotationSet, index: Int, annotation: Annotation) {
        visitAnyDataItem(dexFile, annotation)
    }

    fun visitCode(dexFile: DexFile, encodedMethod: EncodedMethod, code: Code) {
        visitAnyDataItem(dexFile, code)
    }

    fun visitDebugInfo(dexFile: DexFile, code: Code, debugInfo: DebugInfo) {
        visitAnyDataItem(dexFile, debugInfo)
    }

    fun visitCallSiteID(dexFile: DexFile, callSiteID: CallSiteID) {
        visitAnyDataItem(dexFile, callSiteID)
    }

    fun visitCallSite(dexFile: DexFile, callSiteID: CallSiteID, callSite: CallSite) {
        visitAnyDataItem(dexFile, callSite)
    }

    fun visitMethodHandle(dexFile: DexFile, methodHandle: MethodHandle) {
        visitAnyDataItem(dexFile, methodHandle)
    }
}