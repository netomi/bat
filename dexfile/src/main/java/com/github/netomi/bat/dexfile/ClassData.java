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
import com.github.netomi.bat.dexfile.visitor.DataItemVisitor;
import com.github.netomi.bat.dexfile.visitor.EncodedFieldVisitor;
import com.github.netomi.bat.dexfile.visitor.EncodedMemberVisitor;
import com.github.netomi.bat.dexfile.visitor.EncodedMethodVisitor;

import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Objects;

@DataItemAnn(
    type          = DexConstants.TYPE_CLASS_DATA_ITEM,
    dataAlignment = 1,
    dataSection   = true
)
public class ClassData
extends      DataItem
{
    private ArrayList<EncodedField>  staticFields   = new ArrayList<>(0);
    private ArrayList<EncodedField>  instanceFields = new ArrayList<>(0);
    private ArrayList<EncodedMethod> directMethods  = new ArrayList<>(0);
    private ArrayList<EncodedMethod> virtualMethods = new ArrayList<>(0);

    public static ClassData empty() {
        return new ClassData();
    }

    public static ClassData readContent(DexDataInput input) {
        ClassData classData = new ClassData();
        classData.read(input);
        return classData;
    }

    private ClassData() {}

    public void addField(EncodedField field) {
        if (field.isStatic()) {
            staticFields.add(field);
        } else {
            instanceFields.add(field);
        }
    }

    public void addMethod(EncodedMethod method) {
        if (method.isDirectMethod()) {
            directMethods.add(method);
        } else {
            virtualMethods.add(method);
        }
    }

    public int getStaticFieldCount() {
        return staticFields.size();
    }

    public EncodedField getStaticField(int index) {
        return staticFields.get(index);
    }

    public int getInstanceFieldCount() {
        return instanceFields.size();
    }

    public EncodedField getInstanceField(int index) {
        return instanceFields.get(index);
    }

    public int getDirectMethodCount() {
        return directMethods.size();
    }

    public EncodedMethod getDirectMethod(int index) {
        return directMethods.get(index);
    }

    public int getVirtualMethodCount() {
        return virtualMethods.size();
    }

    public EncodedMethod getVirtualMethod(int index) {
        return virtualMethods.get(index);
    }

    @Override
    protected void read(DexDataInput input) {
        // field/method sizes are not stored explicitly,
        // use the size() method of the corresponding list instead.
        int staticFieldsSize   = input.readUleb128();
        int instanceFieldsSize = input.readUleb128();
        int directMethodsSize  = input.readUleb128();
        int virtualMethodsSize = input.readUleb128();

        int lastIndex = 0;
        staticFields.clear();
        staticFields.ensureCapacity(staticFieldsSize);
        for (int i = 0; i < staticFieldsSize; i++) {
            EncodedField encodedField = EncodedField.readContent(input, lastIndex);
            lastIndex = encodedField.getFieldIndex();
            staticFields.add(encodedField);
        }

        lastIndex = 0;
        instanceFields.clear();
        instanceFields.ensureCapacity(instanceFieldsSize);
        for (int i = 0; i < instanceFieldsSize; i++) {
            EncodedField encodedField = EncodedField.readContent(input, lastIndex);
            lastIndex = encodedField.getFieldIndex();
            instanceFields.add(encodedField);
        }

        lastIndex = 0;
        directMethods.clear();
        directMethods.ensureCapacity(directMethodsSize);
        for (int i = 0; i < directMethodsSize; i++) {
            EncodedMethod encodedMethod = EncodedMethod.readContent(input, lastIndex);
            lastIndex = encodedMethod.getMethodIndex();
            directMethods.add(encodedMethod);
        }

        lastIndex = 0;
        virtualMethods.clear();
        virtualMethods.ensureCapacity(virtualMethodsSize);
        for (int i = 0; i < virtualMethodsSize; i++) {
            EncodedMethod encodedMethod = EncodedMethod.readContent(input, lastIndex);
            lastIndex = encodedMethod.getMethodIndex();
            virtualMethods.add(encodedMethod);
        }
    }

    @Override
    protected void readLinkedDataItems(DexDataInput input) {
        for (EncodedMethod method : directMethods) {
            method.readLinkedDataItems(input);
        }

        for (EncodedMethod method : virtualMethods) {
            method.readLinkedDataItems(input);
        }
    }

    @Override
    protected void updateOffsets(DataItem.Map dataItemMap) {
        for (EncodedMethod method : directMethods) {
            method.updateOffsets(dataItemMap);
        }

        for (EncodedMethod method : virtualMethods) {
            method.updateOffsets(dataItemMap);
        }
    }

    @Override
    protected void write(DexDataOutput output) {
        output.writeUleb128(staticFields.size());
        output.writeUleb128(instanceFields.size());
        output.writeUleb128(directMethods.size());
        output.writeUleb128(virtualMethods.size());

        int lastIndex = 0;
        for (EncodedField field : staticFields) {
            lastIndex = field.write(output, lastIndex);
        }

        lastIndex = 0;
        for (EncodedField field : instanceFields) {
            lastIndex = field.write(output, lastIndex);
        }

        lastIndex = 0;
        for (EncodedMethod method : directMethods) {
            lastIndex = method.write(output, lastIndex);
        }

        lastIndex = 0;
        for (EncodedMethod method : virtualMethods) {
            lastIndex = method.write(output, lastIndex);
        }
    }

    public void fieldsAccept(DexFile dexFile, ClassDef classDef, EncodedFieldVisitor visitor) {
        staticFieldsAccept(dexFile, classDef, visitor);
        instanceFieldsAccept(dexFile, classDef, visitor);
    }

    public void staticFieldsAccept(DexFile dexFile, ClassDef classDef, EncodedFieldVisitor visitor) {
        ListIterator<EncodedField> it = staticFields.listIterator();
        while (it.hasNext()) {
            visitor.visitStaticField(dexFile, classDef, it.nextIndex(), it.next());
        }
    }

    public void instanceFieldsAccept(DexFile dexFile, ClassDef classDef, EncodedFieldVisitor visitor) {
        ListIterator<EncodedField> it = instanceFields.listIterator();
        while (it.hasNext()) {
            visitor.visitInstanceField(dexFile, classDef, it.nextIndex(), it.next());
        }
    }

    public void methodsAccept(DexFile dexFile, ClassDef classDef, EncodedMethodVisitor visitor) {
        directMethodsAccept(dexFile, classDef, visitor);
        virtualMethodsAccept(dexFile, classDef, visitor);
    }

    public void directMethodsAccept(DexFile dexFile, ClassDef classDef, EncodedMethodVisitor visitor) {
        ListIterator<EncodedMethod> it = directMethods.listIterator();
        while (it.hasNext()) {
            visitor.visitDirectMethod(dexFile, classDef, it.nextIndex(), it.next());
        }
    }

    public void virtualMethodsAccept(DexFile dexFile, ClassDef classDef, EncodedMethodVisitor visitor) {
        ListIterator<EncodedMethod> it = virtualMethods.listIterator();
        while (it.hasNext()) {
            visitor.visitVirtualMethod(dexFile, classDef, it.nextIndex(), it.next());
        }
    }

    public void membersAccept(DexFile dexFile, ClassDef classDef, EncodedMemberVisitor visitor) {
        fieldsAccept(dexFile, classDef, visitor);
        methodsAccept(dexFile, classDef, visitor);
    }

    @Override
    public void dataItemsAccept(DexFile dexFile, DataItemVisitor visitor) {
        for (EncodedMethod method : directMethods) {
            method.dataItemsAccept(dexFile, visitor);
        }

        for (EncodedMethod method : virtualMethods) {
            method.dataItemsAccept(dexFile, visitor);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClassData other = (ClassData) o;
        return Objects.equals(staticFields,   other.staticFields)   &&
               Objects.equals(instanceFields, other.instanceFields) &&
               Objects.equals(directMethods,  other.directMethods)  &&
               Objects.equals(virtualMethods, other.virtualMethods);
    }

    @Override
    public int hashCode() {
        return Objects.hash(staticFields, instanceFields, directMethods, virtualMethods);
    }

    @Override
    public String toString() {
        return String.format("ClassData[staticFields=%d,instanceFields=%d,directMethods=%d,virtualMethods=%d]",
                             getStaticFieldCount(),
                             getInstanceFieldCount(),
                             getDirectMethodCount(),
                             getVirtualMethodCount());
    }
}
