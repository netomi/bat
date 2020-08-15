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
import com.github.netomi.bat.dexfile.visitor.AnnotationVisitor;
import com.github.netomi.bat.dexfile.visitor.DataItemVisitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.function.BiConsumer;

@DataItemAnn(
    type          = DexConstants.TYPE_ANNOTATION_SET_ITEM,
    dataAlignment = 4,
    dataSection   = true
)
public class AnnotationSet
extends      DataItem
{
    private static final int[] EMPTY_ENTRIES = new int[0];

    //private int              size;                    // uint
    public int[]            annotationOffsetEntries; // uint[]
    public List<Annotation> annotations;

    public AnnotationSet() {
        annotationOffsetEntries = EMPTY_ENTRIES;
        annotations             = Collections.emptyList();
    }

    public int getAnnotationCount() {
        return annotations.size();
    }

    @Override
    protected void read(DexDataInput input) {
        input.skipAlignmentPadding(getDataAlignment());

        int size = input.readInt();

        annotationOffsetEntries = new int[size];
        for (int i = 0; i < size; i++) {
            annotationOffsetEntries[i] = input.readInt();
        }
    }

    @Override
    protected void readLinkedDataItems(DexDataInput input) {
        annotations = new ArrayList<>(annotationOffsetEntries.length);
        for (int i = 0; i < annotationOffsetEntries.length; i++) {
            input.setOffset(annotationOffsetEntries[i]);

            Annotation annotation = Annotation.readContent(input);
            annotations.add(annotation);
        }
    }

    @Override
    protected void write(DexDataOutput output) {
        output.writeAlignmentPadding(getDataAlignment());

        output.writeInt(annotationOffsetEntries.length);
        for (int annotationOffset : annotationOffsetEntries) {
            output.writeInt(annotationOffset);
        }
    }

    public void accept(DexFile dexFile, ClassDef classDef, AnnotationVisitor visitor) {
        ListIterator<Annotation> it = annotations.listIterator();
        while (it.hasNext()) {
            int        index      = it.nextIndex();
            Annotation annotation = it.next();
            visitor.visitAnnotation(dexFile, classDef, this, index, annotation);
        }
    }

    @Override
    public void dataItemsAccept(DexFile dexFile, DataItemVisitor visitor) {
        ListIterator<Annotation> it = annotations.listIterator();
        while (it.hasNext()) {
            int        index      = it.nextIndex();
            Annotation annotation = it.next();
            visitor.visitAnnotation(dexFile, this, index, annotation);
            annotation.dataItemsAccept(dexFile, visitor);
        }
    }

    @Override
    public String toString() {
        return String.format("AnnotationSet[annotations=%s]", annotations);
    }
}
