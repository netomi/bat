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
import com.github.netomi.bat.dexfile.visitor.ClassDefVisitor;
import com.github.netomi.bat.dexfile.visitor.DataItemVisitor;
import com.github.netomi.bat.dexfile.visitor.DexFileVisitor;
import com.github.netomi.bat.dexfile.visitor.DexHeaderVisitor;

import java.util.*;

public class DexFile
{
    public DexHeader      header;
    public MapList        mapList;

    private List<StringID> stringIDs;
    private List<TypeID>   typeIDs;

    public ProtoID[]      protoIDs;
    public FieldID[]      fieldIDs;
    public MethodID[]     methodIDs;
    public ClassDef[]     classDefs;
    public CallSiteID[]   callSiteIDs;
    public MethodHandle[] methodHandles;

    public byte[]         linkData;

    private Map<String, Integer> stringMap;
    private Map<String, Integer> typeMap;

    public DexFile() {
        this.header = new DexHeader();

        this.stringMap = new HashMap<>();
        this.typeMap   = new HashMap<>();
    }

    public int getStringIDCount() {
        return stringIDs.size();
    }

    public Iterable<StringID> getStringIDs() {
        return stringIDs;
    }

    public StringID getStringID(int index) {
        return stringIDs.get(index);
    }

    public String getString(int index) {
        return index == DexConstants.NO_INDEX ?
            null :
            getStringID(index).getStringValue();
    }

    public int addOrGetStringID(String string) {
        Integer index = stringMap.get(string);
        if (index == null) {
            stringIDs.add(StringID.of(string));
            index = stringIDs.size() - 1;
        }
        return index;
    }

    public int getTypeIDCount() {
        return typeIDs.size();
    }

    public Iterable<TypeID> getTypeIDs() {
        return typeIDs;
    }

    public TypeID getTypeID(int index) {
        return typeIDs.get(index);
    }

    public String getType(int index) {
        return index == DexConstants.NO_INDEX ?
            null :
            getTypeID(index).getType(this);
    }

    public int addOrGetTypeID(String type) {
        Integer index = typeMap.get(type);
        if (index == null) {
            typeIDs.add(TypeID.of(addOrGetStringID(type)));
            index = typeIDs.size() - 1;
        }
        return index;
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

    public void read(DexDataInput input) {
        new IO(input).read();

        // update caches:
        stringMap = new HashMap<>(getStringIDCount());
        ListIterator<StringID> stringIterator = stringIDs.listIterator();
        while (stringIterator.hasNext()) {
            int index = stringIterator.nextIndex();
            stringMap.put(stringIterator.next().getStringValue(), index);
        }

        typeMap = new HashMap<>(getTypeIDCount());
        ListIterator<TypeID> typeIterator = typeIDs.listIterator();
        while (typeIterator.hasNext()) {
            int index = typeIterator.nextIndex();
            typeMap.put(typeIterator.next().getType(this), index);
        }
    }

    @Override
    public String toString() {
        // TODO: implement a proper version.

        StringBuilder sb = new StringBuilder();

        sb.append(header);

        return sb.toString();
    }

    // helper class for IO operations.

    private class IO {
        private final DexDataInput input;

        public IO(DexDataInput input) {
            this.input = input;
        }

        public void read() {
            readHeader();
            readMapList();

            readStringIDs();
            readTypeIDs();
            readProtoIDs();
            readFieldIDs();
            readMethodIDs();
            readClassDefs();

            readCallSiteIDs();
            readMethodHandles();

            readLinkedDataItems();

            readLinkData();
        }

        private void readHeader() {
            header.read(input);
            // also read the MapList now as it is needed for other DataItems.
            header.readLinkedDataItems(input);
        }

        private void readMapList() {
            input.setOffset(header.mapOffset);
            input.skipAlignmentPadding(4);

            mapList = new MapList();
            mapList.read(input);
        }

        private void readStringIDs() {
            input.setOffset(header.stringIDsOffsets);

            stringIDs = new ArrayList<>(header.stringIDsSize);
            for (int i = 0; i < header.stringIDsSize; i++) {
                StringID stringIDItem = StringID.empty();
                stringIDItem.read(input);
                stringIDs.add(i, stringIDItem);
            }
        }

        private void readTypeIDs() {
            input.setOffset(header.typeIDsOffset);

            typeIDs = new ArrayList<>(header.typeIDsSize);
            for (int i = 0; i < header.typeIDsSize; i++) {
                TypeID typeIDItem = TypeID.empty();
                typeIDItem.read(input);
                typeIDs.add(i, typeIDItem);
            }
        }

        private void readProtoIDs() {
            input.setOffset(header.protoIDsOffset);

            protoIDs = new ProtoID[header.protoIDsSize];
            for (int i = 0; i < header.protoIDsSize; i++) {
                ProtoID protoIDItem = new ProtoID();
                protoIDItem.read(input);
                protoIDs[i] = protoIDItem;
            }
        }

        private void readFieldIDs() {
            input.setOffset(header.fieldIDsOffset);

            fieldIDs = new FieldID[header.fieldIDsSize];
            for (int i = 0; i < header.fieldIDsSize; i++) {
                FieldID fieldIDItem = new FieldID();
                fieldIDItem.read(input);
                fieldIDs[i] = fieldIDItem;
            }
        }

        private void readMethodIDs() {
            input.setOffset(header.methodIDsOffset);

            methodIDs = new MethodID[header.methodIDsSize];
            for (int i = 0; i < header.methodIDsSize; i++) {
                MethodID methodIDItem = new MethodID();
                methodIDItem.read(input);
                methodIDs[i] = methodIDItem;
            }
        }

        private void readClassDefs() {
            input.setOffset(header.classDefsOffset);

            classDefs = new ClassDef[header.classDefsSize];
            for (int i = 0; i < header.classDefsSize; i++) {
                ClassDef classDefItem = new ClassDef();
                classDefItem.read(input);
                classDefs[i] = classDefItem;
            }
        }

        private void readCallSiteIDs() {
            MapItem mapItem = mapList.getMapItem(DexConstants.TYPE_CALL_SITE_ID_ITEM);
            if (mapItem != null) {
                input.setOffset(mapItem.getOffset());

                callSiteIDs = new CallSiteID[mapItem.getSize()];
                for (int i = 0; i < mapItem.getSize(); i++) {
                    CallSiteID callSiteIDItem = new CallSiteID();
                    callSiteIDItem.read(input);
                    callSiteIDs[i] = callSiteIDItem;
                }
            } else {
                callSiteIDs = new CallSiteID[0];
            }
        }

        private void readMethodHandles() {
            MapItem mapItem = mapList.getMapItem(DexConstants.TYPE_METHOD_HANDLE_ITEM);
            if (mapItem != null) {
                input.setOffset(mapItem.getOffset());

                methodHandles = new MethodHandle[mapItem.getSize()];
                for (int i = 0; i < mapItem.getSize(); i++) {
                    MethodHandle methodHandleItem = new MethodHandle();
                    methodHandleItem.read(input);
                    methodHandles[i] = methodHandleItem;
                }
            } else {
                methodHandles = new MethodHandle[0];
            }
        }

        private void readLinkedDataItems() {
            dataItemsAccept(new DataItemVisitor() {
                @Override
                public void visitAnyDataItem(DexFile dexFile, DataItem dataItem) {
                    dataItem.readLinkedDataItems(input);
                }
            });
        }

        private void readLinkData() {
            linkData = new byte[header.linkSize];
            input.setOffset(header.linkOffset);
            input.readFully(linkData);
        }
    }
}
