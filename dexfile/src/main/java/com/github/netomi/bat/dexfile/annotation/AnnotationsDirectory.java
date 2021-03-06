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
package com.github.netomi.bat.dexfile.annotation;

import com.github.netomi.bat.dexfile.*;
import com.github.netomi.bat.dexfile.io.DexDataInput;
import com.github.netomi.bat.dexfile.io.DexDataOutput;
import com.github.netomi.bat.dexfile.visitor.AnnotationSetVisitor;
import com.github.netomi.bat.dexfile.visitor.DataItemVisitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@DataItemAnn(
    type          = DexConstants.TYPE_ANNOTATIONS_DIRECTORY_ITEM,
    dataAlignment = 4,
    dataSection   = true
)
public class AnnotationsDirectory
extends      DataItem
{
    private int classAnnotationsOffset;  // uint
    //public int fieldsSize;              // uint
    //public int annotatedMethodsSize;    // uint
    //public int annotatedParametersSize; // uint

    public AnnotationSet             classAnnotations;
    public List<FieldAnnotation>     fieldAnnotations;
    public List<MethodAnnotation>    methodAnnotations;
    public List<ParameterAnnotation> parameterAnnotations;

    public static AnnotationsDirectory empty() {
        return new AnnotationsDirectory();
    }

    public static AnnotationsDirectory readContent(DexDataInput input) {
        AnnotationsDirectory annotationsDirectory = new AnnotationsDirectory();
        annotationsDirectory.read(input);
        return annotationsDirectory;
    }

    private AnnotationsDirectory() {
        classAnnotations     = null;
        fieldAnnotations     = Collections.emptyList();
        methodAnnotations    = Collections.emptyList();
        parameterAnnotations = Collections.emptyList();
    }

    public int getClassAnnotationsOffset() {
        return classAnnotationsOffset;
    }

    @Override
    protected void read(DexDataInput input) {
        input.skipAlignmentPadding(getDataAlignment());

        classAnnotationsOffset      = input.readInt();
        int fieldsSize              = input.readInt();
        int annotatedMethodsSize    = input.readInt();
        int annotatedParametersSize = input.readInt();

        fieldAnnotations = new ArrayList<>(fieldsSize);
        for (int i = 0; i < fieldsSize; i++) {
            FieldAnnotation fieldAnnotation = FieldAnnotation.readContent(input);
            fieldAnnotations.add(fieldAnnotation);
        }

        methodAnnotations = new ArrayList<>(annotatedMethodsSize);
        for (int i = 0; i < annotatedMethodsSize; i++) {
            MethodAnnotation methodAnnotation = MethodAnnotation.readContent(input);
            methodAnnotations.add(methodAnnotation);
        }

        parameterAnnotations = new ArrayList<>(annotatedParametersSize);
        for (int i = 0; i < annotatedParametersSize; i++) {
            ParameterAnnotation parameterAnnotation = ParameterAnnotation.readContent(input);
            parameterAnnotations.add(parameterAnnotation);
        }
    }

    @Override
    protected void readLinkedDataItems(DexDataInput input) {
        if (classAnnotationsOffset != 0) {
            input.setOffset(classAnnotationsOffset);
            classAnnotations = AnnotationSet.readContent(input);
        }

        for (FieldAnnotation fieldAnnotation : fieldAnnotations) {
            fieldAnnotation.readLinkedDataItems(input);
        }

        for (MethodAnnotation methodAnnotation : methodAnnotations) {
            methodAnnotation.readLinkedDataItems(input);
        }

        for (ParameterAnnotation parameterAnnotation : parameterAnnotations) {
            parameterAnnotation.readLinkedDataItems(input);
        }
    }

    @Override
    protected void updateOffsets(DataItem.Map dataItemMap) {
        classAnnotationsOffset = dataItemMap.getOffset(classAnnotations);

        for (FieldAnnotation fieldAnnotation : fieldAnnotations) {
            fieldAnnotation.updateOffsets(dataItemMap);
        }

        for (MethodAnnotation methodAnnotation : methodAnnotations) {
            methodAnnotation.updateOffsets(dataItemMap);
        }

        for (ParameterAnnotation parameterAnnotation : parameterAnnotations) {
            parameterAnnotation.updateOffsets(dataItemMap);
        }
    }

    @Override
    protected void write(DexDataOutput output) {
        output.writeAlignmentPadding(getDataAlignment());

        output.writeInt(classAnnotationsOffset);
        output.writeInt(fieldAnnotations.size());
        output.writeInt(methodAnnotations.size());
        output.writeInt(parameterAnnotations.size());

        for (FieldAnnotation fieldAnnotation : fieldAnnotations) {
            fieldAnnotation.write(output);
        }

        for (MethodAnnotation methodAnnotation : methodAnnotations) {
            methodAnnotation.write(output);
        }

        for (ParameterAnnotation parameterAnnotation : parameterAnnotations) {
            parameterAnnotation.write(output);
        }
    }

    public void accept(DexFile dexFile, ClassDef classDef, AnnotationSetVisitor visitor) {
        classAnnotationSetAccept(dexFile, classDef, visitor);

        for (FieldAnnotation fieldAnnotation : fieldAnnotations) {
            fieldAnnotation.accept(dexFile, classDef, visitor);
        }

        for (MethodAnnotation methodAnnotation : methodAnnotations) {
            methodAnnotation.accept(dexFile, classDef, visitor);
        }

        for (ParameterAnnotation parameterAnnotation : parameterAnnotations) {
            parameterAnnotation.accept(dexFile, classDef, visitor);
        }
    }

    public void classAnnotationSetAccept(DexFile dexFile, ClassDef classDef, AnnotationSetVisitor visitor) {
        if (classAnnotations != null) {
            visitor.visitClassAnnotationSet(dexFile, classDef, classAnnotations);
        }
    }

    public void fieldAnnotationSetAccept(DexFile dexFile, ClassDef classDef, EncodedField field, AnnotationSetVisitor visitor) {
        for (FieldAnnotation fieldAnnotation : fieldAnnotations) {
            if (fieldAnnotation.getFieldIndex() == field.getFieldIndex()) {
                fieldAnnotation.accept(dexFile, classDef, visitor);
                break;
            }
        }
    }

    public void methodAnnotationSetAccept(DexFile dexFile, ClassDef classDef, EncodedMethod method, AnnotationSetVisitor visitor) {
        for (MethodAnnotation methodAnnotation : methodAnnotations) {
            if (methodAnnotation.getMethodIndex() == method.getMethodIndex()) {
                methodAnnotation.accept(dexFile, classDef, visitor);
                break;
            }
        }
    }

    public void parameterAnnotationSetAccept(DexFile dexFile, ClassDef classDef, EncodedMethod method, AnnotationSetVisitor visitor) {
        for (ParameterAnnotation parameterAnnotation : parameterAnnotations) {
            if (parameterAnnotation.getMethodIndex() == method.getMethodIndex()) {
                parameterAnnotation.accept(dexFile, classDef, visitor);
                break;
            }
        }
    }

    @Override
    public void dataItemsAccept(DexFile dexFile, DataItemVisitor visitor) {
        if (classAnnotations != null) {
            visitor.visitClassAnnotations(dexFile, this, classAnnotations);
            classAnnotations.dataItemsAccept(dexFile, visitor);
        }

        for (FieldAnnotation fieldAnnotation : fieldAnnotations) {
            fieldAnnotation.dataItemsAccept(dexFile, visitor);
        }

        for (MethodAnnotation methodAnnotation : methodAnnotations) {
            methodAnnotation.dataItemsAccept(dexFile, visitor);
        }

        for (ParameterAnnotation parameterAnnotation : parameterAnnotations) {
            parameterAnnotation.dataItemsAccept(dexFile, visitor);
        }
    }
}
