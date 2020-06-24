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
import com.github.netomi.bat.dexfile.visitor.DefaultDataItemVisitor;

/**
 * @author Thomas Neidhart
 */
class   DataItemOffsetUpdater
extends DefaultDataItemVisitor
{
    DexFileWriter.DataItemMap dataItemMap;

    DataItemOffsetUpdater(DexFileWriter.DataItemMap dataItemMap) {
        this.dataItemMap = dataItemMap;
    }

    private int getOffset(DataItem dataItem) {
        return dataItemMap.getOffset(dataItem);
    }

    // Implementations for DataItemVisitor.

    @Override
    public void visitAnyDataItem(DexFile dexFile, DataItem dataItem) {}

    @Override
    public void visitStringData(DexFile dexFile, StringID stringID, StringData stringData) {
        stringID.stringDataOffset = getOffset(stringData);
    }

    @Override
    public void visitClassData(DexFile dexFile, ClassDef classDef, ClassData classData) {
        classDef.classDataOffset = getOffset(classData);
    }

    @Override
    public void visitStaticValuesArray(DexFile dexFile, ClassDef classDef, EncodedArray encodedArray) {
        classDef.staticValuesOffset = getOffset(encodedArray);
    }

    @Override
    public void visitInterfaceTypes(DexFile dexFile, ClassDef classDef, TypeList typeList) {
        classDef.interfacesOffset = getOffset(typeList);
    }

    @Override
    public void visitParameterTypes(DexFile dexFile, ProtoID protoID, TypeList typeList) {
        protoID.parametersOffset = getOffset(typeList);
    }

    @Override
    public void visitCode(DexFile dexFile, EncodedMethod encodedMethod, Code code) {
        encodedMethod.codeOffset = getOffset(code);
    }

    @Override
    public void visitDebugInfo(DexFile dexFile, Code code, DebugInfo debugInfo) {
        code.debugInfoOffset = getOffset(debugInfo);
    }

    @Override
    public void visitCallSite(DexFile dexFile, CallSiteID callSiteID, CallSite callSite) {
        callSiteID.callSiteOffset = getOffset(callSite);
    }

    @Override
    public void visitAnnotation(DexFile dexFile, AnnotationSet annotationSet, int index, Annotation annotation) {
        annotationSet.annotationOffsetEntries[index] = getOffset(annotation);
    }

    @Override
    public void visitAnnotationsDirectory(DexFile dexFile, ClassDef classDef, AnnotationsDirectory annotationsDirectory) {
        classDef.annotationsOffset = getOffset(annotationsDirectory);
    }

    @Override
    public void visitClassAnnotations(DexFile dexFile, AnnotationsDirectory annotationsDirectory, AnnotationSet annotationSet) {
        annotationsDirectory.classAnnotationsOffset = getOffset(annotationSet);
    }

    @Override
    public void visitFieldAnnotations(DexFile dexFile, FieldAnnotation fieldAnnotation, AnnotationSet annotationSet) {
        fieldAnnotation.annotationsOffset = getOffset(annotationSet);
    }

    @Override
    public void visitMethodAnnotations(DexFile dexFile, MethodAnnotation methodAnnotation, AnnotationSet annotationSet) {
        methodAnnotation.annotationsOffset = getOffset(annotationSet);
    }

    @Override
    public void visitParameterAnnotations(DexFile dexFile, ParameterAnnotation parameterAnnotation, AnnotationSetRefList annotationSetRefList) {
        parameterAnnotation.annotationsOffset = getOffset(annotationSetRefList);
    }

    @Override
    public void visitAnnotationSet(DexFile dexFile, AnnotationSetRef annotationSetRef, AnnotationSet annotationSet) {
        annotationSetRef.annotationsOffset = getOffset(annotationSet);
    }
}
