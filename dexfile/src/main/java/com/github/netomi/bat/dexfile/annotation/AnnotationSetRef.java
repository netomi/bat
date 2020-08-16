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

import com.github.netomi.bat.dexfile.ClassDef;
import com.github.netomi.bat.dexfile.DataItem;
import com.github.netomi.bat.dexfile.DexContent;
import com.github.netomi.bat.dexfile.DexFile;
import com.github.netomi.bat.dexfile.io.DexDataInput;
import com.github.netomi.bat.dexfile.io.DexDataOutput;
import com.github.netomi.bat.dexfile.visitor.AnnotationVisitor;

import java.util.ListIterator;
import java.util.Objects;

/**
 * A class representing an annotation set ref item inside a dex file.
 *
 * @see <a href="https://source.android.com/devices/tech/dalvik/dex-format#set-ref-item">annotation set ref item @ dex format</a>
 *
 * @author Thomas Neidhart
 */
public class AnnotationSetRef
extends      DexContent
{
    private int           annotationsOffset; // uint
    private AnnotationSet annotationSet;

    public static AnnotationSetRef readContent(DexDataInput input) {
        AnnotationSetRef annotationSetRef = new AnnotationSetRef();
        annotationSetRef.read(input);
        return annotationSetRef;
    }

    private AnnotationSetRef() {
        annotationSet = null;
    }

    public AnnotationSet getAnnotationSet() {
        return annotationSet;
    }

    public int getAnnotationsOffset() {
        return annotationsOffset;
    }

    @Override
    protected void read(DexDataInput input) {
        annotationsOffset = input.readInt();
    }

    @Override
    protected void readLinkedDataItems(DexDataInput input) {
        if (annotationsOffset != 0) {
            input.setOffset(annotationsOffset);
            annotationSet = new AnnotationSet();
            annotationSet.read(input);
            annotationSet.readLinkedDataItems(input);
        }
    }

    @Override
    protected void updateOffsets(DataItem.Map dataItemMap) {
        annotationsOffset = dataItemMap.getOffset(annotationSet);
    }

    @Override
    protected void write(DexDataOutput output) {
        output.writeInt(annotationsOffset);
    }

    public void accept(DexFile dexFile, ClassDef classDef, AnnotationVisitor visitor) {
        annotationSet.accept(dexFile, classDef, visitor);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnnotationSetRef other = (AnnotationSetRef) o;
        return Objects.equals(annotationSet, other.annotationSet);
    }

    @Override
    public int hashCode() {
        return Objects.hash(annotationSet);
    }

    @Override
    public String toString() {
        return String.format("AnnotationSetRef[annotationSet=%s]", annotationSet);
    }
}
