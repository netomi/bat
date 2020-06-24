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

package com.github.netomi.bat.dexfile.visitor;

import com.github.netomi.bat.dexfile.*;
import com.github.netomi.bat.dexfile.annotation.*;
import com.github.netomi.bat.dexfile.debug.DebugInfo;

/**
 * @author Thomas Neidhart
 */
public class DefaultDataItemVisitor
implements   DataItemVisitor
{
    public void visitAnyDataItem(DexFile dexFile, DataItem dataItem) {
        throw new RuntimeException("Need to implement in class '" + this.getClass().getName() + "'.");
    }

    @Override
    public void visitHeader(DexFile dexFile, DexHeader header) {
        visitAnyDataItem(dexFile, header);
    }

    @Override
    public void visitMapList(DexFile dexFile, DexHeader header, MapList mapList) {
        visitAnyDataItem(dexFile, mapList);
    }

    @Override
    public void visitStringID(DexFile dexFile, StringID stringID) {
        visitAnyDataItem(dexFile, stringID);
    }

    @Override
    public void visitStringData(DexFile dexFile, StringID stringID, StringData stringData) {
        visitAnyDataItem(dexFile, stringData);
    }

    @Override
    public void visitTypeID(DexFile dexFile, TypeID typeID) {
        visitAnyDataItem(dexFile, typeID);
    }

    @Override
    public void visitProtoID(DexFile dexFile, ProtoID protoID) {
        visitAnyDataItem(dexFile, protoID);
    }

    @Override
    public void visitFieldID(DexFile dexFile, FieldID fieldID) {
        visitAnyDataItem(dexFile, fieldID);
    }

    @Override
    public void visitMethodID(DexFile dexFile, MethodID methodID) {
        visitAnyDataItem(dexFile, methodID);
    }

    @Override
    public void visitClassDef(DexFile dexFile, ClassDef classDef) {
        visitAnyDataItem(dexFile, classDef);
    }

    @Override
    public void visitClassData(DexFile dexFile, ClassDef classDef, ClassData classData) {
        visitAnyDataItem(dexFile, classData);
    }

    @Override
    public void visitStaticValuesArray(DexFile dexFile, ClassDef classDef, EncodedArray encodedArray) {
        visitAnyDataItem(dexFile, encodedArray);
    }

    @Override
    public void visitInterfaceTypes(DexFile dexFile, ClassDef classDef, TypeList typeList) {
        visitAnyDataItem(dexFile, typeList);
    }

    @Override
    public void visitParameterTypes(DexFile dexFile, ProtoID protoID, TypeList typeList) {
        visitAnyDataItem(dexFile, typeList);
    }

    @Override
    public void visitCode(DexFile dexFile, EncodedMethod encodedMethod, Code code) {
        visitAnyDataItem(dexFile, code);
    }

    @Override
    public void visitDebugInfo(DexFile dexFile, Code code, DebugInfo debugInfo) {
        visitAnyDataItem(dexFile, debugInfo);
    }

    @Override
    public void visitCallSiteID(DexFile dexFile, CallSiteID callSiteID) {
        visitAnyDataItem(dexFile, callSiteID);
    }

    @Override
    public void visitCallSite(DexFile dexFile, CallSiteID callSiteID, CallSite callSite) {
        visitAnyDataItem(dexFile, callSite);
    }

    @Override
    public void visitMethodHandle(DexFile dexFile, MethodHandle methodHandle) {
        visitAnyDataItem(dexFile, methodHandle);
    }

    @Override
    public void visitAnnotation(DexFile dexFile, AnnotationSet annotationSet, int index, Annotation annotation) {
        visitAnyDataItem(dexFile, annotation);
    }

    @Override
    public void visitAnnotationsDirectory(DexFile dexFile, ClassDef classDef, AnnotationsDirectory annotationsDirectory) {
        visitAnyDataItem(dexFile, annotationsDirectory);
    }

    @Override
    public void visitClassAnnotations(DexFile dexFile, AnnotationsDirectory annotationsDirectory, AnnotationSet annotationSet) {
        visitAnyDataItem(dexFile, annotationSet);
    }

    @Override
    public void visitFieldAnnotations(DexFile dexFile, FieldAnnotation fieldAnnotation, AnnotationSet annotationSet) {
        visitAnyDataItem(dexFile, annotationSet);
    }

    @Override
    public void visitMethodAnnotations(DexFile dexFile, MethodAnnotation methodAnnotation, AnnotationSet annotationSet) {
        visitAnyDataItem(dexFile, annotationSet);
    }

    @Override
    public void visitParameterAnnotations(DexFile dexFile, ParameterAnnotation parameterAnnotation, AnnotationSetRefList annotationSetRefList) {
        visitAnyDataItem(dexFile, annotationSetRefList);
    }

    @Override
    public void visitAnnotationSet(DexFile dexFile, AnnotationSetRef annotationSetRef, AnnotationSet annotationSet) {
        visitAnyDataItem(dexFile, annotationSet);
    }
}
