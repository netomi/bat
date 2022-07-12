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
import com.github.netomi.bat.util.IntArray;

import java.util.ArrayList;
import java.util.ListIterator;

import static com.github.netomi.bat.dexfile.DexConstantsKt.TYPE_ANNOTATION_SET_ITEM;

/**
 * A class representing an annotation set item inside a dex file.
 *
 * @see <a href="https://source.android.com/devices/tech/dalvik/dex-format#annotation-set-item">annotation set item @ dex format</a>
 */
@DataItemAnn(
    type          = TYPE_ANNOTATION_SET_ITEM,
    dataAlignment = 4,
    dataSection   = true
)
public class AnnotationSet
extends      DataItem
{
    //private int                 size;                    // uint
    private IntArray              annotationOffsetEntries; // uint[]
    private ArrayList<Annotation> annotations;

    public static AnnotationSet readContent(DexDataInput input) {
        AnnotationSet annotationSet = new AnnotationSet();
        annotationSet.read(input);
        return annotationSet;
    }

    public static AnnotationSet empty() {
        return new AnnotationSet();
    }

    private AnnotationSet() {
        annotationOffsetEntries = new IntArray(0);
        annotations             = new ArrayList<>(0);
    }

    public boolean isEmpty() {
        return annotations.isEmpty();
    }

    public int getAnnotationCount() {
        return annotations.size();
    }

    public Annotation getAnnotation(int index) {
        return annotations.get(index);
    }

    public void addAnnotation(Annotation annotation) {
        annotations.add(annotation);
    }

    @Override
    protected void read(DexDataInput input) {
        input.skipAlignmentPadding(getDataAlignment());

        int size = input.readInt();
        annotationOffsetEntries.clear();
        annotationOffsetEntries.resize(size);
        for (int i = 0; i < size; i++) {
            annotationOffsetEntries.set(i, input.readInt());
        }
    }

    @Override
    public void readLinkedDataItems(DexDataInput input) {
        annotations.clear();
        annotations.ensureCapacity(annotationOffsetEntries.size());
        for (int i = 0; i < annotationOffsetEntries.size(); i++) {
            input.setOffset(annotationOffsetEntries.get(i));

            Annotation annotation = Annotation.readContent(input);
            annotations.add(i, annotation);
        }
    }

    @Override
    public void updateOffsets(Map dataItemMap) {
        annotationOffsetEntries.clear();
        annotationOffsetEntries.resize(annotations.size());
        for (int i = 0; i < annotations.size(); i++) {
            annotationOffsetEntries.set(i, dataItemMap.getOffset(annotations.get(i)));
        }
    }

    @Override
    public void write(DexDataOutput output) {
        output.writeAlignmentPadding(getDataAlignment());

        output.writeInt(annotationOffsetEntries.size());
        for (int i = 0; i < annotationOffsetEntries.size(); i++) {
            output.writeInt(annotationOffsetEntries.get(i));
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
