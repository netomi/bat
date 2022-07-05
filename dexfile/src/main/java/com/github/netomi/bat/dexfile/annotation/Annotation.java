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

/**
 * A class representing an annotation item inside a dex file.
 *
 * @see <a href="https://source.android.com/devices/tech/dalvik/dex-format#annotation-item">annotation item @ dex format</a>
 *
 * @author Thomas Neidhart
 */
@DataItemAnn(
    type          = DexConstants.TYPE_ANNOTATION_ITEM,
    dataAlignment = 1,
    dataSection   = true
)
public class Annotation
extends      DataItem
{
    //private short                  visibility; // ubyte
    private AnnotationVisibility   visibility;
    private EncodedAnnotationValue value;

    public static Annotation of(AnnotationVisibility visibility, EncodedAnnotationValue value) {
        return new Annotation(visibility, value);
    }

    public static Annotation readContent(DexDataInput input) {
        Annotation annotation = new Annotation();
        annotation.read(input);
        return annotation;
    }

    private Annotation() {
        this(null, null);
    }

    private Annotation(AnnotationVisibility visibility, EncodedAnnotationValue value) {
        this.visibility = visibility;
        this.value      = value;
    }

    public AnnotationVisibility getVisibility() {
        return visibility;
    }

    public EncodedAnnotationValue getAnnotationValue() {
        return value;
    }

    @Override
    protected void read(DexDataInput input) {
        int visibilityValue = input.readUnsignedByte();
        visibility = AnnotationVisibility.of(visibilityValue);
        value      = EncodedValue.readAnnotationValue(input);
    }

    @Override
    protected void write(DexDataOutput output) {
        output.writeUnsignedByte(visibility.getValue());
        value.write(output);
    }

    @Override
    public String toString() {
        return String.format("Annotation[visibility='%s',value=%s]", visibility.getSimpleName(), value);
    }
}
