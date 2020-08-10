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
import com.github.netomi.bat.dexfile.MethodID;
import com.github.netomi.bat.dexfile.io.DexDataInput;
import com.github.netomi.bat.dexfile.io.DexDataOutput;
import com.github.netomi.bat.dexfile.visitor.DataItemVisitor;

import static com.github.netomi.bat.dexfile.DexConstants.NO_INDEX;

public class MethodAnnotation
extends      DexContent
{
    public  int           methodIndex;       // uint
    private int           annotationsOffset; // uint
    public  AnnotationSet annotationSet;

    public MethodAnnotation() {
        methodIndex       = NO_INDEX;
        annotationsOffset = 0;
        annotationSet     = null;
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

    @Override
    public void dataItemsAccept(DexFile dexFile, DataItemVisitor visitor) {
        visitor.visitMethodAnnotations(dexFile, this, annotationSet);
    }
}
