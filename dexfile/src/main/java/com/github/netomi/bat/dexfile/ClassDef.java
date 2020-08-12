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

import com.github.netomi.bat.dexfile.annotation.AnnotationsDirectory;
import com.github.netomi.bat.dexfile.util.DexClasses;
import com.github.netomi.bat.dexfile.visitor.*;
import com.github.netomi.bat.dexfile.io.DexDataInput;
import com.github.netomi.bat.dexfile.io.DexDataOutput;

import static com.github.netomi.bat.dexfile.DexConstants.NO_INDEX;

@DataItemAnn(
    type          = DexConstants.TYPE_CLASS_DEF_ITEM,
    dataAlignment = 4,
    dataSection   = false
)
public class ClassDef
extends      DataItem
{
    private int classIndex;         // uint
    private int accessFlags;        // uint
    private int superClassIndex;    // uint
    private int interfacesOffset;   // uint
    private int sourceFileIndex;    // uint
    private int annotationsOffset;  // uint
    private int classDataOffset;    // uint
    private int staticValuesOffset; // uint

    public TypeList             interfaces;
    public AnnotationsDirectory annotationsDirectory;
    public ClassData            classData;
    public EncodedArray         staticValues;

    public static ClassDef readContent(DexDataInput input) {
        ClassDef classDef = new ClassDef();
        classDef.read(input);
        return classDef;
    }

    private ClassDef() {
        classIndex         = NO_INDEX;
        accessFlags        = 0;
        superClassIndex    = NO_INDEX;
        interfacesOffset   = 0;
        sourceFileIndex    = NO_INDEX;
        annotationsOffset  = 0;
        classDataOffset    = 0;
        staticValuesOffset = 0;

        interfaces           = null;
        annotationsDirectory = null;
        classData            = null;
        staticValues         = null;
    }

    public int getInterfacesOffset() {
        return interfacesOffset;
    }

    public int getAnnotationsOffset() {
        return annotationsOffset;
    }

    public int getClassDataOffset() {
        return classDataOffset;
    }

    public int getStaticValuesOffset() {
        return staticValuesOffset;
    }

    public int getClassIndex() {
        return classIndex;
    }

    public String getClassName(DexFile dexFile) {
        return DexClasses.internalClassNameFromType(getType(dexFile));
    }

    public String getType(DexFile dexFile) {
        return dexFile.getTypeID(classIndex).getType(dexFile);
    }

    public int getAccessFlags() {
        return accessFlags;
    }

    public int getSuperClassIndex() {
        return superClassIndex;
    }

    public String getSuperClassName(DexFile dexFile) {
        return DexClasses.internalClassNameFromType(getSuperClassType(dexFile));
    }

    public String getSuperClassType(DexFile dexFile) {
        return dexFile.getTypeID(superClassIndex).getType(dexFile);
    }

    public int getSourceFileIndex() {
        return sourceFileIndex;
    }

    public String getSourceFile(DexFile dexFile) {
        return sourceFileIndex == NO_INDEX ?
            "unknown" :
            dexFile.getStringID(sourceFileIndex).getStringValue();
    }

    @Override
    public void read(DexDataInput input) {
        input.skipAlignmentPadding(getDataAlignment());
        classIndex         = input.readInt();
        accessFlags        = input.readInt();
        superClassIndex    = input.readInt();
        interfacesOffset   = input.readInt();
        sourceFileIndex    = input.readInt();
        annotationsOffset  = input.readInt();
        classDataOffset    = input.readInt();
        staticValuesOffset = input.readInt();
    }

    @Override
    public void readLinkedDataItems(DexDataInput input) {
        if (interfacesOffset != 0) {
            input.setOffset(interfacesOffset);
            interfaces = TypeList.readContent(input);
        }

        if (annotationsOffset != 0) {
            input.setOffset(annotationsOffset);
            annotationsDirectory = AnnotationsDirectory.readContent(input);
        }

        if (classDataOffset != 0) {
            input.setOffset(classDataOffset);
            classData = ClassData.readContent(input);
        }

        if (staticValuesOffset != 0) {
            input.setOffset(staticValuesOffset);
            staticValues = EncodedArray.readContent(input);
        }
    }

    @Override
    public void updateOffsets(DataItem.Map dataItemMap) {
        interfacesOffset   = dataItemMap.getOffset(interfaces);
        annotationsOffset  = dataItemMap.getOffset(annotationsDirectory);
        classDataOffset    = dataItemMap.getOffset(classData);
        staticValuesOffset = dataItemMap.getOffset(staticValues);
    }

    @Override
    public void write(DexDataOutput output) {
        output.writeAlignmentPadding(getDataAlignment());
        output.writeInt(classIndex);
        output.writeInt(accessFlags);
        output.writeInt(superClassIndex);
        output.writeInt(interfacesOffset);
        output.writeInt(sourceFileIndex);
        output.writeInt(annotationsOffset);
        output.writeInt(classDataOffset);
        output.writeInt(staticValuesOffset);
    }

    public void interfacesAccept(DexFile dexFile, TypeListVisitor visitor) {
        if (interfaces != null) {
            visitor.visitInterfaces(dexFile, this, interfaces);
        }
    }

    public void classDataAccept(DexFile dexFile, ClassDataVisitor visitor) {
        if (classData != null) {
            visitor.visitClassData(dexFile, this, classData);
        }
    }

    public void annotationSetsAccept(DexFile dexFile, AnnotationSetVisitor visitor) {
        if (annotationsDirectory != null) {
            annotationsDirectory.accept(dexFile, this, visitor);
        }
    }

    public void staticValueAccept(DexFile dexFile, int index, EncodedValueVisitor visitor) {
        if (staticValues != null) {
            staticValues.accept(dexFile, index, visitor);
        }
    }

    @Override
    public void dataItemsAccept(DexFile dexFile, DataItemVisitor visitor) {
        if (interfaces != null) {
            visitor.visitInterfaceTypes(dexFile, this, interfaces);
        }

        if (annotationsDirectory != null) {
            visitor.visitAnnotationsDirectory(dexFile, this, annotationsDirectory);
            annotationsDirectory.dataItemsAccept(dexFile, visitor);
        }

        if (classData != null) {
            visitor.visitClassData(dexFile, this, classData);
            classData.dataItemsAccept(dexFile, visitor);
        }

        if (staticValues != null) {
            visitor.visitStaticValuesArray(dexFile, this, staticValues);
        }
    }

}
