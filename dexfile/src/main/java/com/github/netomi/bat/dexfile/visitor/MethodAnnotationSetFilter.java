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

import com.github.netomi.bat.dexfile.ClassDef;
import com.github.netomi.bat.dexfile.DexFile;
import com.github.netomi.bat.dexfile.EncodedMethod;
import com.github.netomi.bat.dexfile.annotation.*;

/**
 * This {@code AnnotationsDirectoryVisitor} and {@code AnnotationSetVisitor}
 * will apply the given {@code AnnotationSetVisitor} on matching method annotations
 * of visited annotation directories.
 *
 * @author Thomas Neidhart
 */
public class MethodAnnotationSetFilter
implements   AnnotationsDirectoryVisitor,
             AnnotationSetVisitor
{
    private final EncodedMethod        method;
    private final AnnotationSetVisitor visitor;

    public MethodAnnotationSetFilter(EncodedMethod        method,
                                     AnnotationSetVisitor visitor) {
        this.method  = method;
        this.visitor = visitor;
    }

    @Override
    public void visitAnnotationsDirectory(DexFile dexFile, ClassDef classDef, AnnotationsDirectory annotationsDirectory) {
        annotationsDirectory.methodAnnotationSetAccept(dexFile, classDef, method, visitor);
    }

    @Override
    public void visitAnyAnnotationSet(DexFile dexFile, ClassDef classDef, AnnotationSet annotationSet) {}

    @Override
    public void visitParameterAnnotationSet(DexFile dexFile, ClassDef classDef, ParameterAnnotation parameterAnnotation, AnnotationSetRefList annotationSetRefList) {}

    @Override
    public void visitMethodAnnotationSet(DexFile dexFile, ClassDef classDef, MethodAnnotation methodAnnotation, AnnotationSet annotationSet) {
        if (methodAnnotation.getMethodIndex() == method.getMethodIndex()) {
            visitor.visitMethodAnnotationSet(dexFile, classDef, methodAnnotation, annotationSet);
        }
    }
}
