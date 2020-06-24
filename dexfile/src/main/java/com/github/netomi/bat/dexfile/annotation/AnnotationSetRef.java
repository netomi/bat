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

import com.github.netomi.bat.dexfile.DexContent;
import com.github.netomi.bat.dexfile.io.DexDataInput;
import com.github.netomi.bat.dexfile.io.DexDataOutput;

public class AnnotationSetRef implements DexContent
{
    public int           annotationsOffset; // uint
    public AnnotationSet annotationSet;

    public AnnotationSetRef() {
        annotationsOffset = 0;
        annotationSet     = null;
    }

    @Override
    public void read(DexDataInput input) {
        annotationsOffset = input.readInt();
    }

    @Override
    public void write(DexDataOutput output) {
        output.writeInt(annotationsOffset);
    }
}
