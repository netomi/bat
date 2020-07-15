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

import com.github.netomi.bat.dexfile.visitor.ClassDefVisitor;
import com.github.netomi.bat.dexfile.visitor.DataItemVisitor;
import com.github.netomi.bat.dexfile.visitor.DexFileVisitor;
import com.github.netomi.bat.dexfile.visitor.DexHeaderVisitor;

public class DexFile
{
    public DexHeader      header;
    public MapList        mapList;

    public StringID[]     stringIDs;
    public TypeID[]       typeIDs;
    public ProtoID[]      protoIDs;
    public FieldID[]      fieldIDs;
    public MethodID[]     methodIDs;
    public ClassDef[]     classDefs;
    public CallSiteID[]   callSiteIDs;
    public MethodHandle[] methodHandles;

    public byte[]         linkData;

    public DexFile() {
        this.header   = new DexHeader();
    }

    public StringID getStringID(int index) {
        return stringIDs[index];
    }

    public String getString(int index) {
        return index == DexConstants.NO_INDEX ?
            null :
            getStringID(index).getStringValue();
    }

    public TypeID getTypeID(int index) {
        return typeIDs[index];
    }

    public String getType(int index) {
        return index == DexConstants.NO_INDEX ?
            null :
            getTypeID(index).getType(this);
    }

    public ProtoID getProtoID(int index) {
        return protoIDs[index];
    }

    public ClassDef getClassDef(int index) {
        return classDefs[index];
    }

    public FieldID getFieldID(int index) {
        return fieldIDs[index];
    }

    public MethodID getMethodID(int index) {
        return methodIDs[index];
    }

    public MethodHandle getMethodHandle(int index) {
        return methodHandles[index];
    }

    public DexFormat getDexFormat() {
        return DexFormat.fromPattern(header.magic, 4, 8);
    }

    public void accept(DexFileVisitor visitor) {
        visitor.visitDexFile(this);
    }

    public void headerAccept(DexHeaderVisitor visitor) {
        visitor.visitHeader(this, header);
    }

    public void classDefsAccept(ClassDefVisitor visitor) {
        for (int i = 0; i < classDefs.length; i++) {
            visitor.visitClassDef(this, i, classDefs[i]);
        }
    }

    public void dataItemsAccept(DataItemVisitor visitor) {
        visitor.visitHeader(this, header);
        header.dataItemsAccept(this, visitor);

        visitor.visitMapList(this, mapList);
        mapList.dataItemsAccept(this, visitor);

        for (StringID stringIDItem : stringIDs) {
            visitor.visitStringID(this, stringIDItem);
            stringIDItem.dataItemsAccept(this, visitor);
        }

        for (TypeID typeIDItem : typeIDs) {
            visitor.visitTypeID(this, typeIDItem);
            typeIDItem.dataItemsAccept(this, visitor);
        }

        for (ProtoID protoIDItem : protoIDs) {
            visitor.visitProtoID(this, protoIDItem);
            protoIDItem.dataItemsAccept(this, visitor);
        }

        for (FieldID fieldIDItem : fieldIDs) {
            visitor.visitFieldID(this, fieldIDItem);
            fieldIDItem.dataItemsAccept(this, visitor);
        }

        for (MethodID methodIDItem : methodIDs) {
            visitor.visitMethodID(this, methodIDItem);
            methodIDItem.dataItemsAccept(this, visitor);
        }

        for (ClassDef classDefItem : classDefs) {
            visitor.visitClassDef(this, classDefItem);
            classDefItem.dataItemsAccept(this, visitor);
        }

        for (CallSiteID callSiteIDItem : callSiteIDs) {
            visitor.visitCallSiteID(this, callSiteIDItem);
            callSiteIDItem.dataItemsAccept(this, visitor);
        }

        for (MethodHandle methodHandleItem : methodHandles) {
            visitor.visitMethodHandle(this, methodHandleItem);
            methodHandleItem.dataItemsAccept(this, visitor);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(header);

        return sb.toString();
    }
}
