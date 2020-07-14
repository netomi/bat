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
public interface DataItemVisitor {
    default void visitAnyDataItem(DexFile dexFile, DataItem dataItem) {
        throw new RuntimeException("Need to implement in class '" + this.getClass().getName() + "'.");
    }

    default void visitHeader(DexFile dexFile, DexHeader header) {
        visitAnyDataItem(dexFile, header);
    }

    default void visitMapList(DexFile dexFile, MapList mapList) {
        visitAnyDataItem(dexFile, mapList);
    }

    default void visitStringID(DexFile dexFile, StringID stringID) {
        visitAnyDataItem(dexFile, stringID);
    }

    default void visitStringData(DexFile dexFile, StringID stringID, StringData stringData) {
        visitAnyDataItem(dexFile, stringData);
    }

    default void visitTypeID(DexFile dexFile, TypeID typeID) {
        visitAnyDataItem(dexFile, typeID);
    }

    default void visitProtoID(DexFile dexFile, ProtoID protoID) {
        visitAnyDataItem(dexFile, protoID);
    }

    default void visitParameterTypes(DexFile dexFile, ProtoID protoID, TypeList typeList) {
        visitAnyDataItem(dexFile, typeList);
    }

    default void visitFieldID(DexFile dexFile, FieldID fieldID) {
        visitAnyDataItem(dexFile, fieldID);
    }

    default void visitMethodID(DexFile dexFile, MethodID methodID) {
        visitAnyDataItem(dexFile, methodID);
    }

    default void visitClassDef(DexFile dexFile, ClassDef classDef) {
        visitAnyDataItem(dexFile, classDef);
    }

    default void visitClassData(DexFile dexFile, ClassDef classDef, ClassData classData) {
        visitAnyDataItem(dexFile, classData);
    }

    default void visitInterfaceTypes(DexFile dexFile, ClassDef classDef, TypeList typeList) {
        visitAnyDataItem(dexFile, typeList);
    }

    default void visitStaticValuesArray(DexFile dexFile, ClassDef classDef, EncodedArray encodedArray) {
        visitAnyDataItem(dexFile, encodedArray);
    }

    default void visitAnnotationsDirectory(DexFile dexFile, ClassDef classDef, AnnotationsDirectory annotationsDirectory) {
        visitAnyDataItem(dexFile, annotationsDirectory);
    }

    default void visitClassAnnotations(DexFile dexFile, AnnotationsDirectory annotationsDirectory, AnnotationSet annotationSet) {
        visitAnyDataItem(dexFile, annotationSet);
    }

    default void visitFieldAnnotations(DexFile dexFile, FieldAnnotation fieldAnnotation, AnnotationSet annotationSet) {
        visitAnyDataItem(dexFile, annotationSet);
    }

    default void visitMethodAnnotations(DexFile dexFile, MethodAnnotation methodAnnotation, AnnotationSet annotationSet) {
        visitAnyDataItem(dexFile, annotationSet);
    }

    default void visitParameterAnnotations(DexFile dexFile, ParameterAnnotation parameterAnnotation, AnnotationSetRefList annotationSetRefList) {
        visitAnyDataItem(dexFile, annotationSetRefList);
    }

    default void visitAnnotationSet(DexFile dexFile, AnnotationSetRef annotationSetRef, AnnotationSet annotationSet) {
        visitAnyDataItem(dexFile, annotationSet);
    }

    default void visitAnnotation(DexFile dexFile, AnnotationSet annotationSet, int index, Annotation annotation) {
        visitAnyDataItem(dexFile, annotation);
    }

    default void visitCode(DexFile dexFile, EncodedMethod encodedMethod, Code code) {
        visitAnyDataItem(dexFile, code);
    }

    default void visitDebugInfo(DexFile dexFile, Code code, DebugInfo debugInfo) {
        visitAnyDataItem(dexFile, debugInfo);
    }

    default void visitCallSiteID(DexFile dexFile, CallSiteID callSiteID) {
        visitAnyDataItem(dexFile, callSiteID);
    }

    default void visitCallSite(DexFile dexFile, CallSiteID callSiteID, CallSite callSite) {
        visitAnyDataItem(dexFile, callSite);
    }

    default void visitMethodHandle(DexFile dexFile, MethodHandle methodHandle) {
        visitAnyDataItem(dexFile, methodHandle);
    }
}
