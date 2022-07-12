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
import com.github.netomi.bat.dexfile.visitor.AnnotationVisitor;
import com.github.netomi.bat.dexfile.visitor.DataItemVisitor;

import java.util.Objects;

import static com.github.netomi.bat.dexfile.DexConstantsKt.NO_INDEX;

/**
 * A class representing a parameter annotation format inside a dex file.
 *
 * @see <a href="https://source.android.com/devices/tech/dalvik/dex-format#parameter-annotation">parameter annotation format @ dex format</a>
 */
public class ParameterAnnotation
extends      DexContent
{
    private int methodIndex;       // uint
    private int annotationsOffset; // uint

    private AnnotationSetRefList annotationSetRefList;

    public static ParameterAnnotation of(int methodIndex, AnnotationSetRefList annotationSetRefList) {
        return new ParameterAnnotation(methodIndex, annotationSetRefList);
    }

    public static ParameterAnnotation readContent(DexDataInput input) {
        ParameterAnnotation parameterAnnotation = new ParameterAnnotation();
        parameterAnnotation.read(input);
        return parameterAnnotation;
    }

    private ParameterAnnotation() {
        this(NO_INDEX, null);
    }

    private ParameterAnnotation(int methodIndex, AnnotationSetRefList annotationSetRefList) {
        this.methodIndex          = methodIndex;
        this.annotationSetRefList = annotationSetRefList;
    }

    public int getMethodIndex() {
        return methodIndex;
    }

    public int getAnnotationsOffset() {
        return annotationsOffset;
    }

    public MethodID getMethodID(DexFile dexFile) {
        return dexFile.getMethodID(methodIndex);
    }

    @Override
    protected void read(DexDataInput input) {
        methodIndex       = input.readInt();
        annotationsOffset = input.readInt();
    }

    @Override
    public void readLinkedDataItems(DexDataInput input) {
        input.setOffset(annotationsOffset);
        annotationSetRefList = AnnotationSetRefList.readContent(input);
    }

    @Override
    public void updateOffsets(DataItem.Map dataItemMap) {
        annotationsOffset = dataItemMap.getOffset(annotationSetRefList);
    }

    @Override
    public void write(DexDataOutput output) {
        output.writeInt(methodIndex);
        output.writeInt(annotationsOffset);
    }

    public void accept(DexFile dexFile, ClassDef classDef, AnnotationSetVisitor visitor) {
        visitor.visitParameterAnnotationSet(dexFile, classDef, this, annotationSetRefList);
    }

    public void accept(DexFile dexFile, ClassDef classDef, int parameterIndex, AnnotationVisitor visitor) {
        if (parameterIndex >= 0 && parameterIndex < annotationSetRefList.getAnnotationSetRefCount()) {
            AnnotationSetRef annotationSetRef = annotationSetRefList.getAnnotationSetRef(parameterIndex);
            if (annotationSetRef != null) {
                annotationSetRef.accept(dexFile, classDef, visitor);
            }
        }
    }

    @Override
    public void dataItemsAccept(DexFile dexFile, DataItemVisitor visitor) {
        visitor.visitParameterAnnotations(dexFile, this, annotationSetRefList);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParameterAnnotation other = (ParameterAnnotation) o;
        return methodIndex == other.methodIndex &&
               Objects.equals(annotationSetRefList, other.annotationSetRefList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(methodIndex, annotationSetRefList);
    }

    @Override
    public String toString() {
        return String.format("ParameterAnnotation[methodIndex=%d]", methodIndex);
    }
}
