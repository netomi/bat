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
package com.github.netomi.bat.dexfile.value;

import com.github.netomi.bat.dexfile.DexContent;
import com.github.netomi.bat.dexfile.DexFile;
import com.github.netomi.bat.dexfile.io.DexDataInput;
import com.github.netomi.bat.dexfile.io.DexDataOutput;
import com.github.netomi.bat.dexfile.visitor.AnnotationElementVisitor;

import static com.github.netomi.bat.dexfile.DexConstants.NO_INDEX;

public class AnnotationElement
extends      DexContent
{
    private int          nameIndex; // uleb128
    private EncodedValue value;

    public AnnotationElement() {
        nameIndex = NO_INDEX;
        value     = null;
    }

    public int getNameIndex() {
        return nameIndex;
    }

    public String getName(DexFile dexFile) {
        return dexFile.getStringID(nameIndex).getStringValue();
    }

    public EncodedValue getValue() {
        return value;
    }

    @Override
    protected void read(DexDataInput input) {
        nameIndex = input.readUleb128();
        value     = EncodedValue.read(input);
    }

    @Override
    protected void write(DexDataOutput output) {
        output.writeUleb128(nameIndex);
        value.write(output);
    }

    public void accept(DexFile dexFile, AnnotationElementVisitor visitor) {
        visitor.visitAnnotationElement(dexFile, this);
    }

    @Override
    public String toString() {
        return String.format("AnnotationElement[nameIndex=%d,value=%s]", nameIndex, value);
    }
}
