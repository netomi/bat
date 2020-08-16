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

/**
 * A class representing an annotation set ref list inside a dex file.
 *
 * @see <a href="https://source.android.com/devices/tech/dalvik/dex-format#set-ref-list">annotation set ref list @ dex format</a>
 *
 * @author Thomas Neidhart
 */
@DataItemAnn(
    type          = DexConstants.TYPE_ANNOTATION_SET_REF_LIST,
    dataAlignment = 4,
    dataSection   = true
)
public class AnnotationSetRefList
extends      DataItem
{
    //public int                  size; // uint, use annotationSetRefs.size()
    private ArrayList<AnnotationSetRef> annotationSetRefs = new ArrayList<>(0);

    public static AnnotationSetRefList readContent(DexDataInput input) {
        AnnotationSetRefList annotationSetRefList = new AnnotationSetRefList();
        annotationSetRefList.read(input);
        return annotationSetRefList;
    }

    private AnnotationSetRefList() {}

    public int getAnnotationSetRefCount() {
        return annotationSetRefs.size();
    }

    public AnnotationSetRef getAnnotationSetRef(int index) {
        return annotationSetRefs.get(index);
    }

    @Override
    protected void read(DexDataInput input) {
        input.skipAlignmentPadding(getDataAlignment());

        int size = input.readInt();
        annotationSetRefs.ensureCapacity(size);
        for (int i = 0; i < size; i++) {
            AnnotationSetRef annotationSetRef = AnnotationSetRef.readContent(input);
            annotationSetRefs.add(annotationSetRef);
        }
    }

    @Override
    protected void readLinkedDataItems(DexDataInput input) {
        for (AnnotationSetRef annotationSetRef : annotationSetRefs) {
            annotationSetRef.readLinkedDataItems(input);
        }
    }

    @Override
    protected void updateOffsets(DataItem.Map dataItemMap) {
        for (AnnotationSetRef annotationSetRef : annotationSetRefs) {
            annotationSetRef.updateOffsets(dataItemMap);
        }
    }

    @Override
    protected void write(DexDataOutput output) {
        output.writeAlignmentPadding(getDataAlignment());

        output.writeInt(annotationSetRefs.size());
        for (AnnotationSetRef annotationSetRef : annotationSetRefs) {
            annotationSetRef.write(output);
        }
    }

    public void accept(DexFile dexFile, ClassDef classDef, int index, AnnotationVisitor visitor) {
        if (index >= 0 && index < getAnnotationSetRefCount()) {
            AnnotationSetRef annotationSetRef = annotationSetRefs.get(index);
            if (annotationSetRef != null) {
                annotationSetRef.accept(dexFile, classDef, visitor);
            }
        }
    }

    @Override
    public void dataItemsAccept(DexFile dexFile, DataItemVisitor visitor) {
        for (AnnotationSetRef annotationSetRef : annotationSetRefs) {
            visitor.visitAnnotationSet(dexFile, annotationSetRef, annotationSetRef.getAnnotationSet());
        }
    }
}
