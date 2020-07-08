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

package com.github.netomi.bat.dexfile.io;

import com.github.netomi.bat.dexfile.*;
import com.github.netomi.bat.dexfile.annotation.*;
import com.github.netomi.bat.dexfile.debug.DebugInfo;
import com.github.netomi.bat.dexfile.visitor.DataItemVisitor;

/**
 * @author Thomas Neidhart
 */
public class DataSectionItemCollector
implements   DataItemVisitor
{
    private DexFileWriter.DataItemMap dataItemMap;

    public DataSectionItemCollector(DexFileWriter.DataItemMap dataItemMap) {
        this.dataItemMap = dataItemMap;
    }

    @Override
    public void visitAnyDataItem(DexFile dexFile, DataItem dataItem) {}

    @Override
    public void visitStringData(DexFile dexFile, StringID stringID, StringData stringData) {
        dataItemMap.addDataItem(stringData);
    }

    @Override
    public void visitParameterTypes(DexFile dexFile, ProtoID protoID, TypeList typeList) {
        dataItemMap.addDataItem(typeList);
    }

    @Override
    public void visitClassData(DexFile dexFile, ClassDef classDef, ClassData classData) {
        dataItemMap.addDataItem(classData);
    }

    @Override
    public void visitInterfaceTypes(DexFile dexFile, ClassDef classDef, TypeList typeList) {
        dataItemMap.addDataItem(typeList);
    }

    @Override
    public void visitStaticValuesArray(DexFile dexFile, ClassDef classDef, EncodedArray encodedArray) {
        dataItemMap.addDataItem(encodedArray);
    }

    @Override
    public void visitAnnotationsDirectory(DexFile dexFile, ClassDef classDef, AnnotationsDirectory annotationsDirectory) {
        dataItemMap.addDataItem(annotationsDirectory);
    }

    @Override
    public void visitClassAnnotations(DexFile dexFile, AnnotationsDirectory annotationsDirectory, AnnotationSet annotationSet) {
        dataItemMap.addDataItem(annotationSet);
    }

    @Override
    public void visitFieldAnnotations(DexFile dexFile, FieldAnnotation fieldAnnotation, AnnotationSet annotationSet) {
        dataItemMap.addDataItem(annotationSet);
    }

    @Override
    public void visitMethodAnnotations(DexFile dexFile, MethodAnnotation methodAnnotation, AnnotationSet annotationSet) {
        dataItemMap.addDataItem(annotationSet);
    }

    @Override
    public void visitParameterAnnotations(DexFile dexFile, ParameterAnnotation parameterAnnotation, AnnotationSetRefList annotationSetRefList) {
        dataItemMap.addDataItem(annotationSetRefList);
    }

    @Override
    public void visitAnnotationSet(DexFile dexFile, AnnotationSetRef annotationSetRef, AnnotationSet annotationSet) {
        dataItemMap.addDataItem(annotationSet);
    }

    @Override
    public void visitAnnotation(DexFile dexFile, AnnotationSet annotationSet, int index, Annotation annotation) {
        dataItemMap.addDataItem(annotation);
    }

    @Override
    public void visitCode(DexFile dexFile, EncodedMethod encodedMethod, Code code) {
        dataItemMap.addDataItem(code);
    }

    @Override
    public void visitDebugInfo(DexFile dexFile, Code code, DebugInfo debugInfo) {
        dataItemMap.addDataItem(debugInfo);
    }

    @Override
    public void visitCallSite(DexFile dexFile, CallSiteID callSiteID, CallSite callSite) {
        dataItemMap.addDataItem(callSite);
    }
}
