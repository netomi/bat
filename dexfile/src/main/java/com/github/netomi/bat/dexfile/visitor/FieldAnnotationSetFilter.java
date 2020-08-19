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
import com.github.netomi.bat.dexfile.EncodedField;
import com.github.netomi.bat.dexfile.annotation.*;

/**
 * This {@code AnnotationsDirectoryVisitor} and {@code AnnotationSetVisitor}
 * will apply the given {@code AnnotationSetVisitor} on matching field annotations
 * of visited annotation directories.
 *
 * @author Thomas Neidhart
 */
public class FieldAnnotationSetFilter
implements   AnnotationsDirectoryVisitor,
             AnnotationSetVisitor
{
    private final EncodedField         field;
    private final AnnotationSetVisitor visitor;

    public FieldAnnotationSetFilter(EncodedField         field,
                                    AnnotationSetVisitor visitor) {
        this.field   = field;
        this.visitor = visitor;
    }

    @Override
    public void visitAnnotationsDirectory(DexFile dexFile, ClassDef classDef, AnnotationsDirectory annotationsDirectory) {
        annotationsDirectory.fieldAnnotationSetAccept(dexFile, classDef, field, visitor);
    }

    @Override
    public void visitAnyAnnotationSet(DexFile dexFile, ClassDef classDef, AnnotationSet annotationSet) {}

    @Override
    public void visitParameterAnnotationSet(DexFile dexFile, ClassDef classDef, ParameterAnnotation parameterAnnotation, AnnotationSetRefList annotationSetRefList) {}

    @Override
    public void visitFieldAnnotationSet(DexFile dexFile, ClassDef classDef, FieldAnnotation fieldAnnotation, AnnotationSet annotationSet) {
        if (fieldAnnotation.getFieldIndex() == field.getFieldIndex()) {
            visitor.visitFieldAnnotationSet(dexFile, classDef, fieldAnnotation, annotationSet);
        }
    }
}
