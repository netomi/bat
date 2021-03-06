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

import com.github.netomi.bat.dexfile.DexFile;
import com.github.netomi.bat.dexfile.io.DexDataInput;
import com.github.netomi.bat.dexfile.io.DexDataOutput;
import com.github.netomi.bat.dexfile.visitor.AnnotationElementVisitor;
import com.github.netomi.bat.dexfile.visitor.EncodedValueVisitor;

import java.util.*;

import static com.github.netomi.bat.dexfile.DexConstants.NO_INDEX;

/**
 * An class representing an annotation value (TypeID + AnnotationElements) inside a dex file.
 *
 * @author Thomas Neidhart
 */
public class EncodedAnnotationValue
extends      EncodedValue
{
    private int typeIndex; // uleb128
    //public int size;      // uleb128

    private ArrayList<AnnotationElement> elements;

    public static EncodedAnnotationValue of(int typeIndex, AnnotationElement... elements) {
        return new EncodedAnnotationValue(typeIndex, elements);
    }

    EncodedAnnotationValue() {
        this.typeIndex = NO_INDEX;
        this.elements  = new ArrayList<>(0);
    }

    private EncodedAnnotationValue(int typeIndex, AnnotationElement... elements) {
        this.typeIndex = typeIndex;
        this.elements  = new ArrayList<>(elements.length);
        this.elements.addAll(Arrays.asList(elements));
    }

    public int getTypeIndex() {
        return typeIndex;
    }

    public String getType(DexFile dexFile) {
        return dexFile.getTypeID(typeIndex).getType(dexFile);
    }

    public int getAnnotationElementCount() {
        return elements.size();
    }

    public AnnotationElement getAnnotationElement(int index) {
        return elements.get(index);
    }

    public Iterable<AnnotationElement> getAnnotationElements() {
        return elements;
    }

    @Override
    public int getValueType() {
        return VALUE_ANNOTATION;
    }

    @Override
    public void readValue(DexDataInput input, int valueArg) {
        typeIndex = input.readUleb128();
        int size  = input.readUleb128();

        elements.clear();
        elements.ensureCapacity(size);
        for (int i = 0; i < size; i++) {
            AnnotationElement element = AnnotationElement.readContent(input);
            elements.add(element);
        }
    }

    @Override
    protected int writeType(DexDataOutput output) {
        return writeType(output, 0);
    }

    @Override
    public void writeValue(DexDataOutput output, int valueArg) {
        output.writeUleb128(typeIndex);
        output.writeUleb128(elements.size());
        for (AnnotationElement element : elements) {
            element.write(output);
        }
    }

    @Override
    public void accept(DexFile dexFile, EncodedValueVisitor visitor) {
        visitor.visitAnnotationValue(dexFile, this);
    }

    public void annotationElementsAccept(DexFile dexFile, AnnotationElementVisitor visitor) {
        for (AnnotationElement element : elements) {
            element.accept(dexFile, visitor);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EncodedAnnotationValue other = (EncodedAnnotationValue) o;
        return typeIndex == other.typeIndex &&
               Objects.equals(elements, other.elements);
    }

    @Override
    public int hashCode() {
        return Objects.hash(typeIndex, elements);
    }

    @Override
    public String toString() {
        return String.format("EncodedAnnotationValue[typeIndex=%d,elements=%s]", typeIndex, elements);
    }
}
