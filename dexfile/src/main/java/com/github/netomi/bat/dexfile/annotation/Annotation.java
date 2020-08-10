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
import com.github.netomi.bat.dexfile.io.DexDataInput;
import com.github.netomi.bat.dexfile.io.DexDataOutput;
import com.github.netomi.bat.dexfile.value.EncodedAnnotationValue;
import com.github.netomi.bat.dexfile.value.EncodedValue;

@DataItemAnn(
    type          = DexConstants.TYPE_ANNOTATION_ITEM,
    dataAlignment = 1,
    dataSection   = true
)
public class Annotation
extends      DataItem
{
    private short                  visibility; // ubyte
    private EncodedAnnotationValue annotation;

    public static Annotation readContent(DexDataInput input) {
        Annotation annotation = new Annotation();
        annotation.read(input);
        return annotation;
    }

    private Annotation() {
        visibility = 0;
        annotation = null;
    }

    public AnnotationVisibility getVisibility() {
        return AnnotationVisibility.of(visibility);
    }

    public EncodedAnnotationValue getAnnotationValue() {
        return annotation;
    }

    @Override
    protected void read(DexDataInput input) {
        visibility = input.readUnsignedByte();
        annotation = EncodedValue.readAnnotationValue(input);
    }

    @Override
    protected void write(DexDataOutput output) {
        output.writeUnsignedByte(visibility);
        annotation.write(output);
    }

    @Override
    public String toString() {
        return String.format("Annotation[visibility=%d,value=%s]", visibility, annotation);
    }
}
