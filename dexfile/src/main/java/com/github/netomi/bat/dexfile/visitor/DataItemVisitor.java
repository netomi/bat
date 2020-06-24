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
public interface DataItemVisitor
{
    void visitHeader (DexFile dexFile, DexHeader header);
    void visitMapList(DexFile dexFile, DexHeader header, MapList mapList);

    void visitStringID  (DexFile dexFile, StringID stringID);
    void visitStringData(DexFile dexFile, StringID stringID, StringData stringData);

    void visitTypeID  (DexFile dexFile, TypeID typeID);

    void visitProtoID       (DexFile dexFile, ProtoID protoID);
    void visitParameterTypes(DexFile dexFile, ProtoID protoID, TypeList typeList);

    void visitFieldID (DexFile dexFile, FieldID fieldID);
    void visitMethodID(DexFile dexFile, MethodID methodID);

    void visitClassDef            (DexFile dexFile, ClassDef classDef);
    void visitClassData           (DexFile dexFile, ClassDef classDef, ClassData classData);
    void visitInterfaceTypes      (DexFile dexFile, ClassDef classDef, TypeList typeList);
    void visitStaticValuesArray   (DexFile dexFile, ClassDef classDef, EncodedArray encodedArray);
    void visitAnnotationsDirectory(DexFile dexFile, ClassDef classDef, AnnotationsDirectory annotationsDirectory);

    void visitClassAnnotations    (DexFile dexFile, AnnotationsDirectory annotationsDirectory, AnnotationSet annotationSet);
    void visitFieldAnnotations    (DexFile dexFile, FieldAnnotation fieldAnnotation, AnnotationSet annotationSet);
    void visitMethodAnnotations   (DexFile dexFile, MethodAnnotation methodAnnotation, AnnotationSet annotationSet);
    void visitParameterAnnotations(DexFile dexFile, ParameterAnnotation parameterAnnotation, AnnotationSetRefList annotationSetRefList);
    void visitAnnotationSet       (DexFile dexFile, AnnotationSetRef annotationSetRef, AnnotationSet annotationSet);
    void visitAnnotation          (DexFile dexFile, AnnotationSet annotationSet, int index, Annotation annotation);

    void visitCode             (DexFile dexFile, EncodedMethod encodedMethod, Code code);
    void visitDebugInfo        (DexFile dexFile, Code code, DebugInfo debugInfo);

    void visitCallSiteID(DexFile dexFile, CallSiteID callSiteID);
    void visitCallSite  (DexFile dexFile, CallSiteID callSiteID, CallSite callSite);

    void visitMethodHandle(DexFile dexFile, MethodHandle methodHandle);
}
