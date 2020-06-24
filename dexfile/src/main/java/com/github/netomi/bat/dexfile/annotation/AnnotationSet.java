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

import com.github.netomi.bat.dexfile.DataItem;
import com.github.netomi.bat.dexfile.DataItemAnn;
import com.github.netomi.bat.dexfile.DexConstants;
import com.github.netomi.bat.dexfile.DexFile;
import com.github.netomi.bat.dexfile.io.DexDataInput;
import com.github.netomi.bat.dexfile.io.DexDataOutput;
import com.github.netomi.bat.dexfile.visitor.DataItemVisitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

@DataItemAnn(
    type          = DexConstants.TYPE_ANNOTATION_SET_ITEM,
    dataAlignment = 4,
    dataSection   = true
)
public class AnnotationSet extends DataItem
{
    private static final int[] EMPTY_ENTRIES = new int[0];

    public int              size;                    // uint
    public int[]            annotationOffsetEntries; // uint[]
    public List<Annotation> annotations;

    public AnnotationSet() {
        annotationOffsetEntries = EMPTY_ENTRIES;
        annotations             = Collections.emptyList();
    }

    @Override
    public void read(DexDataInput input) {
        input.skipAlignmentPadding(getDataAlignment());

        size = input.readInt();

        annotationOffsetEntries = new int[size];
        for (int i = 0; i < size; i++) {
            annotationOffsetEntries[i] = input.readInt();
        }
    }

    @Override
    public void readLinkedDataItems(DexDataInput input) {
        annotations = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            Annotation annotation = new Annotation();

            input.setOffset(annotationOffsetEntries[i]);

            annotation.read(input);
            annotations.add(annotation);
        }
    }

    @Override
    public void write(DexDataOutput output) {
        output.writeAlignmentPadding(getDataAlignment());

        output.writeInt(size);
        for (int annotationOffset : annotationOffsetEntries) {
            output.writeInt(annotationOffset);
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
}
