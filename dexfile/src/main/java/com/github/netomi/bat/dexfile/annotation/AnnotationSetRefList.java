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

@DataItemAnn(
    type          = DexConstants.TYPE_ANNOTATION_SET_REF_LIST,
    dataAlignment = 4,
    dataSection   = true
)
public class AnnotationSetRefList extends DataItem
{
    //public int                  size; // uint, use annotationSetRefs.size()
    public List<AnnotationSetRef> annotationSetRefs;

    public AnnotationSetRefList() {
        annotationSetRefs = Collections.emptyList();
    }

    @Override
    public void read(DexDataInput input) {
        input.skipAlignmentPadding(getDataAlignment());

        int size = input.readInt();

        annotationSetRefs = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            AnnotationSetRef annotationSetRef = new AnnotationSetRef();
            annotationSetRef.read(input);
            annotationSetRefs.add(annotationSetRef);
        }
    }

    @Override
    public void readLinkedDataItems(DexDataInput input) {
        for (AnnotationSetRef annotationSetRef : annotationSetRefs) {
            if (annotationSetRef.annotationsOffset != 0) {
                input.setOffset(annotationSetRef.annotationsOffset);
                annotationSetRef.annotationSet = new AnnotationSet();
                annotationSetRef.annotationSet.read(input);
            }
        }
    }

    @Override
    public void write(DexDataOutput output) {
        output.writeAlignmentPadding(getDataAlignment());

        output.writeInt(annotationSetRefs.size());
        for (AnnotationSetRef annotationSetRef : annotationSetRefs) {
            annotationSetRef.write(output);
        }
    }

    @Override
    public void dataItemsAccept(DexFile dexFile, DataItemVisitor visitor) {
        for (AnnotationSetRef annotationSetRef : annotationSetRefs) {
            visitor.visitAnnotationSet(dexFile, annotationSetRef, annotationSetRef.annotationSet);
        }
    }
}
