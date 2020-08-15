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

import java.util.Objects;

import static com.github.netomi.bat.dexfile.DexConstants.NO_INDEX;

/**
 * A class representing a method annotation format inside a dex file.
 *
 * @see <a href="https://source.android.com/devices/tech/dalvik/dex-format#method-annotation">method annotation format @ dex format</a>
 *
 * @author Thomas Neidhart
 */
public class MethodAnnotation
extends      DexContent
{
    private int           methodIndex;       // uint
    private int           annotationsOffset; // uint
    private AnnotationSet annotationSet;

    public static MethodAnnotation of(int methodIndex, AnnotationSet annotationSet) {
        return new MethodAnnotation(methodIndex, annotationSet);
    }

    public static MethodAnnotation readContent(DexDataInput input) {
        MethodAnnotation methodAnnotation = new MethodAnnotation();
        methodAnnotation.read(input);
        return methodAnnotation;
    }

    private MethodAnnotation() {
        this(NO_INDEX, null);
    }

    private MethodAnnotation(int methodIndex, AnnotationSet annotationSet) {
        this.methodIndex = methodIndex;
        this.annotationSet = annotationSet;
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
    protected void readLinkedDataItems(DexDataInput input) {
        input.setOffset(annotationsOffset);
        annotationSet = new AnnotationSet();
        annotationSet.read(input);
    }

    @Override
    protected void updateOffsets(DataItem.Map dataItemMap) {
        annotationsOffset = dataItemMap.getOffset(annotationSet);
    }

    @Override
    protected void write(DexDataOutput output) {
        output.writeInt(methodIndex);
        output.writeInt(annotationsOffset);
    }

    public void accept(DexFile dexFile, ClassDef classDef, AnnotationSetVisitor visitor) {
        visitor.visitMethodAnnotationSet(dexFile, classDef, this, annotationSet);
    }

    @Override
    public void dataItemsAccept(DexFile dexFile, DataItemVisitor visitor) {
        visitor.visitMethodAnnotations(dexFile, this, annotationSet);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MethodAnnotation other = (MethodAnnotation) o;
        return methodIndex == other.methodIndex &&
               Objects.equals(annotationSet, other.annotationSet);
    }

    @Override
    public int hashCode() {
        return Objects.hash(methodIndex, annotationSet);
    }

    @Override
    public String toString() {
        return String.format("MethodAnnotation[methodIdx=%d]", methodIndex);
    }
}
