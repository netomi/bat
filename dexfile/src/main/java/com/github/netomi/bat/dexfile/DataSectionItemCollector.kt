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
package com.github.netomi.bat.dexfile

import com.github.netomi.bat.dexfile.annotation.*
import com.github.netomi.bat.dexfile.annotation.Annotation
import com.github.netomi.bat.dexfile.debug.DebugInfo
import com.github.netomi.bat.dexfile.visitor.DataItemVisitor

internal class DataSectionItemCollector(private val dataItemMap: DataItemMapImpl) : DataItemVisitor {

    override fun visitAnyDataItem(dexFile: DexFile, dataItem: DataItem) {}

    override fun visitStringData(dexFile: DexFile, stringID: StringID, stringData: StringData) {
        dataItemMap.addDataItem(stringData)
    }

    override fun visitParameterTypes(dexFile: DexFile, protoID: ProtoID, typeList: TypeList) {
        dataItemMap.addDataItem(typeList)
    }

    override fun visitClassData(dexFile: DexFile, classDef: ClassDef, classData: ClassData) {
        dataItemMap.addDataItem(classData)
    }

    override fun visitInterfaceTypes(dexFile: DexFile, classDef: ClassDef, typeList: TypeList) {
        dataItemMap.addDataItem(typeList)
    }

    override fun visitStaticValuesArray(dexFile: DexFile, classDef: ClassDef, encodedArray: EncodedArray) {
        dataItemMap.addDataItem(encodedArray)
    }

    override fun visitAnnotationsDirectory(dexFile: DexFile, classDef: ClassDef, annotationsDirectory: AnnotationsDirectory) {
        dataItemMap.addDataItem(annotationsDirectory)
    }

    override fun visitClassAnnotations(dexFile: DexFile, annotationsDirectory: AnnotationsDirectory, annotationSet: AnnotationSet) {
        dataItemMap.addDataItem(annotationSet)
    }

    override fun visitFieldAnnotations(dexFile: DexFile, fieldAnnotation: FieldAnnotation, annotationSet: AnnotationSet) {
        dataItemMap.addDataItem(annotationSet)
    }

    override fun visitMethodAnnotations(dexFile: DexFile, methodAnnotation: MethodAnnotation, annotationSet: AnnotationSet) {
        dataItemMap.addDataItem(annotationSet)
    }

    override fun visitParameterAnnotations(dexFile: DexFile, parameterAnnotation: ParameterAnnotation, annotationSetRefList: AnnotationSetRefList) {
        dataItemMap.addDataItem(annotationSetRefList)
    }

    override fun visitAnnotationSet(dexFile: DexFile, annotationSetRef: AnnotationSetRef, annotationSet: AnnotationSet) {
        dataItemMap.addDataItem(annotationSet)
    }

    override fun visitAnnotation(dexFile: DexFile, annotationSet: AnnotationSet, index: Int, annotation: Annotation) {
        dataItemMap.addDataItem(annotation)
    }

    override fun visitCode(dexFile: DexFile, encodedMethod: EncodedMethod, code: Code) {
        dataItemMap.addDataItem(code)
    }

    override fun visitDebugInfo(dexFile: DexFile, code: Code, debugInfo: DebugInfo) {
        dataItemMap.addDataItem(debugInfo)
    }

    override fun visitCallSite(dexFile: DexFile, callSiteID: CallSiteID, callSite: CallSite) {
        dataItemMap.addDataItem(callSite)
    }
}