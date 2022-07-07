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
 * A class representing a field annotation format inside a dex file.
 *
 * @see <a href="https://source.android.com/devices/tech/dalvik/dex-format#field-annotation">field annotation format @ dex format</a>
 *
 * @author Thomas Neidhart
 */
public class FieldAnnotation
extends      DexContent
{
    private int           fieldIndex;        // uint
    private int           annotationsOffset; // uint
    private AnnotationSet annotationSet;

    public static FieldAnnotation of(int fieldIndex, AnnotationSet annotationSet) {
        return new FieldAnnotation(fieldIndex, annotationSet);
    }

    public static FieldAnnotation readContent(DexDataInput input) {
        FieldAnnotation fieldAnnotation = new FieldAnnotation();
        fieldAnnotation.read(input);
        return fieldAnnotation;
    }

    private FieldAnnotation() {
        this(NO_INDEX, null);
    }

    private FieldAnnotation(int fieldIndex, AnnotationSet annotationSet) {
        this.fieldIndex        = fieldIndex;
        this.annotationSet     = annotationSet;
    }

    public int getFieldIndex() {
        return fieldIndex;
    }

    public int getAnnotationsOffset() {
        return annotationsOffset;
    }

    public FieldID getFieldID(DexFile dexFile) {
        return dexFile.getFieldID(fieldIndex);
    }

    @Override
    protected void read(DexDataInput input) {
        fieldIndex        = input.readInt();
        annotationsOffset = input.readInt();
    }

    @Override
    public void readLinkedDataItems(DexDataInput input) {
        input.setOffset(annotationsOffset);
        annotationSet = AnnotationSet.readContent(input);
    }

    @Override
    public void updateOffsets(DataItem.Map dataItemMap) {
        annotationsOffset = dataItemMap.getOffset(annotationSet);
    }

    @Override
    public void write(DexDataOutput output) {
        output.writeInt(fieldIndex);
        output.writeInt(annotationsOffset);
    }

    public void accept(DexFile dexFile, ClassDef classDef, AnnotationSetVisitor visitor) {
        visitor.visitFieldAnnotationSet(dexFile, classDef, this, annotationSet);
    }

    @Override
    public void dataItemsAccept(DexFile dexFile, DataItemVisitor visitor) {
        visitor.visitFieldAnnotations(dexFile, this, annotationSet);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FieldAnnotation other = (FieldAnnotation) o;
        return fieldIndex == other.fieldIndex &&
               Objects.equals(annotationSet, other.annotationSet);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fieldIndex, annotationSet);
    }

    @Override
    public String toString() {
        return String.format("FieldAnnotation[fieldIdx=%d]", fieldIndex);
    }
}
