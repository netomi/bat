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
import com.github.netomi.bat.dexfile.DexContent;
import com.github.netomi.bat.dexfile.DexFile;
import com.github.netomi.bat.dexfile.FieldID;
import com.github.netomi.bat.dexfile.io.DexDataInput;
import com.github.netomi.bat.dexfile.io.DexDataOutput;
import com.github.netomi.bat.dexfile.visitor.DataItemVisitor;

import static com.github.netomi.bat.dexfile.DexConstants.NO_INDEX;

public class FieldAnnotation
implements   DexContent
{
    public  int           fieldIndex;        // uint
    private int           annotationsOffset; // uint
    public  AnnotationSet annotationSet;

    public FieldAnnotation() {
        fieldIndex        = NO_INDEX;
        annotationsOffset = 0;
        annotationSet     = null;
    }

    public int getAnnotationsOffset() {
        return annotationsOffset;
    }

    public FieldID getFieldID(DexFile dexFile) {
        return dexFile.getFieldID(fieldIndex);
    }

    @Override
    public void read(DexDataInput input) {
        fieldIndex        = input.readInt();
        annotationsOffset = input.readInt();
    }

    @Override
    public void readLinkedDataItems(DexDataInput input) {
        input.setOffset(annotationsOffset);
        annotationSet = new AnnotationSet();
        annotationSet.read(input);
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

    @Override
    public void dataItemsAccept(DexFile dexFile, DataItemVisitor visitor) {
        visitor.visitFieldAnnotations(dexFile, this, annotationSet);
    }
}
