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
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

@DataItemAnn(
    type          = DexConstants.TYPE_CLASS_DATA_ITEM,
    dataAlignment = 1,
    dataSection   = true
)
public class ClassData
implements   DataItem
{
    public List<EncodedField>  staticFields;
    public List<EncodedField>  instanceFields;
    public List<EncodedMethod> directMethods;
    public List<EncodedMethod> virtualMethods;

    public ClassData() {
        staticFields   = Collections.emptyList();
        instanceFields = Collections.emptyList();
        directMethods  = Collections.emptyList();
        virtualMethods = Collections.emptyList();
    }

    @Override
    public void read(DexDataInput input) {
        // field/method sizes are not stored explicitly,
        // use the size() method of the corresponding list instead.
        int staticFieldsSize   = input.readUleb128();
        int instanceFieldsSize = input.readUleb128();
        int directMethodsSize  = input.readUleb128();
        int virtualMethodsSize = input.readUleb128();

        staticFields = new ArrayList<>(staticFieldsSize);
        input.setLastMemberIndex(0);
        for (int i = 0; i < staticFieldsSize; i++) {
            EncodedField encodedField = new EncodedField();
            encodedField.read(input);
            input.setLastMemberIndex(encodedField.fieldIndex);
            staticFields.add(encodedField);
        }

        instanceFields = new ArrayList<>(instanceFieldsSize);
        input.setLastMemberIndex(0);
        for (int i = 0; i < instanceFieldsSize; i++) {
            EncodedField encodedField = new EncodedField();
            encodedField.read(input);
            input.setLastMemberIndex(encodedField.fieldIndex);
            instanceFields.add(encodedField);
        }

        directMethods = new ArrayList<>(directMethodsSize);
        input.setLastMemberIndex(0);
        for (int i = 0; i < directMethodsSize; i++) {
            EncodedMethod encodedMethod = new EncodedMethod();
            encodedMethod.read(input);
            input.setLastMemberIndex(encodedMethod.methodIndex);
            directMethods.add(encodedMethod);
        }

        virtualMethods = new ArrayList<>(virtualMethodsSize);
        input.setLastMemberIndex(0);
        for (int i = 0; i < virtualMethodsSize; i++) {
            EncodedMethod encodedMethod = new EncodedMethod();
            encodedMethod.read(input);
            input.setLastMemberIndex(encodedMethod.methodIndex);
            virtualMethods.add(encodedMethod);
        }
    }

    @Override
    public void readLinkedDataItems(DexDataInput input) {
        for (EncodedMethod method : directMethods) {
            method.readLinkedDataItems(input);
        }

        for (EncodedMethod method : virtualMethods) {
            method.readLinkedDataItems(input);
        }
    }

    @Override
    public void updateOffsets(DataItem.Map dataItemMap) {
        for (EncodedMethod method : directMethods) {
            method.updateOffsets(dataItemMap);
        }

        for (EncodedMethod method : virtualMethods) {
            method.updateOffsets(dataItemMap);
        }
    }

    @Override
    public void write(DexDataOutput output) {
        output.writeUleb128(staticFields.size());
        output.writeUleb128(instanceFields.size());
        output.writeUleb128(directMethods.size());
        output.writeUleb128(virtualMethods.size());

        output.setLastMemberIndex(0);
        for (EncodedField field : staticFields) {
            field.write(output);
            output.setLastMemberIndex(field.fieldIndex);
        }

        output.setLastMemberIndex(0);
        for (EncodedField field : instanceFields) {
            field.write(output);
            output.setLastMemberIndex(field.fieldIndex);
        }

        output.setLastMemberIndex(0);
        for (EncodedMethod method : directMethods) {
            method.write(output);
            output.setLastMemberIndex(method.methodIndex);
        }

        output.setLastMemberIndex(0);
        for (EncodedMethod method : virtualMethods) {
            method.write(output);
            output.setLastMemberIndex(method.methodIndex);
        }
    }

    public void fieldsAccept(DexFile dexFile, ClassDef classDef, EncodedFieldVisitor visitor) {
        staticFieldsAccept(dexFile, classDef, visitor);
        instanceFieldsAccept(dexFile, classDef, visitor);
    }

    public void staticFieldsAccept(DexFile dexFile, ClassDef classDef, EncodedFieldVisitor visitor) {
        ListIterator<EncodedField> it = staticFields.listIterator();
        while (it.hasNext()) {
            visitor.visitStaticField(dexFile, classDef, this, it.nextIndex(), it.next());
        }
    }

    public void instanceFieldsAccept(DexFile dexFile, ClassDef classDef, EncodedFieldVisitor visitor) {
        ListIterator<EncodedField> it = instanceFields.listIterator();
        while (it.hasNext()) {
            visitor.visitInstanceField(dexFile, classDef, this, it.nextIndex(), it.next());
        }
    }

    public void methodsAccept(DexFile dexFile, ClassDef classDef, EncodedMethodVisitor visitor) {
        directMethodsAccept(dexFile, classDef, visitor);
        virtualMethodsAccept(dexFile, classDef, visitor);
    }

    public void directMethodsAccept(DexFile dexFile, ClassDef classDef, EncodedMethodVisitor visitor) {
        ListIterator<EncodedMethod> it = directMethods.listIterator();
        while (it.hasNext()) {
            visitor.visitDirectMethod(dexFile, classDef, this, it.nextIndex(), it.next());
        }
    }

    public void virtualMethodsAccept(DexFile dexFile, ClassDef classDef, EncodedMethodVisitor visitor) {
        ListIterator<EncodedMethod> it = virtualMethods.listIterator();
        while (it.hasNext()) {
            visitor.visitVirtualMethod(dexFile, classDef, this, it.nextIndex(), it.next());
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
}
