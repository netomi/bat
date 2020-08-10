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
package com.github.netomi.bat.dexfile;

import com.github.netomi.bat.dexfile.io.DexDataInput;
import com.github.netomi.bat.dexfile.io.DexDataOutput;
import com.github.netomi.bat.dexfile.visitor.EncodedValueVisitor;

import static com.github.netomi.bat.dexfile.DexConstants.NO_INDEX;

public class EncodedField
implements   DexContent
{
    private int deltaFieldIndex; // uleb128
    private int fieldIndex;
    private int accessFlags;     // uleb128

    public static EncodedField readItem(DexDataInput input, int lastIndex) {
        EncodedField encodedField = new EncodedField();
        encodedField.read(input);
        encodedField.updateFieldIndex(lastIndex);
        return encodedField;
    }

    private EncodedField() {
        fieldIndex  = NO_INDEX;
        accessFlags = 0;
    }

    public int getFieldIndex() {
        return fieldIndex;
    }

    public int getAccessFlags() {
        return accessFlags;
    }

    public FieldID getFieldID(DexFile dexFile) {
        return dexFile.getFieldID(fieldIndex);
    }

    public String getName(DexFile dexFile) {
        return getFieldID(dexFile).getName(dexFile);
    }

    public String getType(DexFile dexFile) {
        return getFieldID(dexFile).getType(dexFile);
    }

    public boolean isStatic() {
        return (accessFlags & DexConstants.ACC_STATIC) != 0;
    }

    @Override
    public void read(DexDataInput input) {
        deltaFieldIndex = input.readUleb128();
        accessFlags     = input.readUleb128();
    }

    private void updateFieldIndex(int lastIndex) {
        fieldIndex = deltaFieldIndex + lastIndex;
    }

    private void updateDeltaFieldIndex(int lastIndex) {
        deltaFieldIndex = fieldIndex - lastIndex;
    }

    public int write(DexDataOutput output, int lastIndex) {
        updateDeltaFieldIndex(lastIndex);
        write(output);
        return fieldIndex;
    }

    @Override
    public void write(DexDataOutput output) {
        output.writeUleb128(deltaFieldIndex);
        output.writeUleb128(accessFlags);
    }

    public void staticValueAccept(DexFile dexFile, ClassDef classDef, int index, EncodedValueVisitor visitor) {
        if (isStatic()) {
            classDef.staticValueAccept(dexFile, index, visitor);
        }
    }
}
