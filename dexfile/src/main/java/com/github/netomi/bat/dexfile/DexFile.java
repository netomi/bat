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

import com.github.netomi.bat.dexfile.io.*;
import com.github.netomi.bat.dexfile.visitor.*;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.*;

import static com.github.netomi.bat.dexfile.DexConstants.NO_INDEX;

public class DexFile
{
    private DexHeader header;
    private MapList   mapList;

    private ArrayList<StringID>     stringIDs     = new ArrayList<>();
    private ArrayList<TypeID>       typeIDs       = new ArrayList<>();
    private ArrayList<ProtoID>      protoIDs      = new ArrayList<>();
    private ArrayList<FieldID>      fieldIDs      = new ArrayList<>();
    private ArrayList<MethodID>     methodIDs     = new ArrayList<>();
    private ArrayList<ClassDef>     classDefs     = new ArrayList<>();
    private ArrayList<CallSiteID>   callSiteIDs   = new ArrayList<>();
    private ArrayList<MethodHandle> methodHandles = new ArrayList<>();

    private byte[] linkData;

    private Map<String, Integer>  stringMap    = new HashMap<>();
    private Map<String, Integer>  typeMap      = new HashMap<>();
    private Map<ProtoID, Integer>  protoIDMap   = new HashMap<>();
    private Map<String, Integer>  classDefMap  = new HashMap<>();
    private Map<FieldID, Integer> fieldIDMap   = new HashMap<>();
    private Map<MethodID, Integer> methodIDMap = new HashMap<>();

    public static DexFile of(DexFormat dexFormat) {
        return new DexFile(dexFormat);
    }

    public DexFile() {
        this.header = new DexHeader();
    }

    public DexFile(DexFormat dexFormat) {
        this.header = new DexHeader(dexFormat);
    }

    public DexHeader getHeader() {
        return header;
    }

    /**
     * Returns the MapList instance associated with this DexFile if
     * it is read from an existing dex file.
     */
    public Optional<MapList> getMapList() {
        return Optional.ofNullable(mapList);
    }

    public DexFormat getDexFormat() {
        return DexFormat.fromPattern(header.magic, 4, 8);
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
        return index == NO_INDEX ?
            null :
            getStringID(index).getStringValue();
    }

    public int getStringIDIndex(String string) {
        Integer index = stringMap.get(string);
        return index == null ? NO_INDEX : index;
    }

    public int addOrGetStringIDIndex(String string) {
        Integer index = stringMap.get(string);
        if (index == null) {
            stringIDs.add(StringID.of(string));
            index = stringIDs.size() - 1;
            stringMap.put(string, index);
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
        return index == NO_INDEX ?
            null :
            getTypeID(index).getType(this);
    }

    public int getTypeIDIndex(String type) {
        Integer index = typeMap.get(type);
        return index == null ? NO_INDEX : index;
    }

    public int addOrGetTypeIDIndex(String type) {
        Integer index = typeMap.get(type);
        if (index == null) {
            typeIDs.add(TypeID.of(addOrGetStringIDIndex(type)));
            index = typeIDs.size() - 1;
            typeMap.put(type, index);
        }
        return index;
    }

    public int getProtoIDCount() {
        return protoIDs.size();
    }

    public Iterable<ProtoID> getProtoIDs() {
        return protoIDs;
    }

    public ProtoID getProtoID(int protoIndex) {
        return protoIDs.get(protoIndex);
    }

    public int addOrGetProtoID(String shorty, String returnType, String... parameterTypes) {
        ProtoID protoID = null;

        if (parameterTypes.length > 0) {
            int[] parameterTypeIndices = new int[parameterTypes.length];
            for (int i = 0; i < parameterTypes.length; i++) {
                parameterTypeIndices[i] = addOrGetTypeIDIndex(parameterTypes[i]);
            }

            protoID =
                ProtoID.of(addOrGetStringIDIndex(shorty),
                           addOrGetTypeIDIndex(returnType),
                           parameterTypeIndices);

        } else {
            protoID =
                ProtoID.of(addOrGetStringIDIndex(shorty),
                           addOrGetTypeIDIndex(returnType));
        }

        Integer index = protoIDMap.get(protoID);
        if (index == null) {
            protoIDs.add(protoID);
            index = protoIDs.size() - 1;
            protoIDMap.put(protoID, index);
        }
        return index;
    }

    public int getFieldIDCount() {
        return fieldIDs.size();
    }

    public Iterable<FieldID> getFieldIDs() {
        return fieldIDs;
    }

    public FieldID getFieldID(int fieldIndex) {
        return fieldIDs.get(fieldIndex);
    }

    public int addOrGetFieldID(String classType, String name, String type) {
        FieldID fieldID =
            FieldID.of(addOrGetTypeIDIndex(classType),
                       addOrGetStringIDIndex(name),
                       addOrGetTypeIDIndex(type));

        Integer index = fieldIDMap.get(fieldID);
        if (index == null) {
            fieldIDs.add(fieldID);
            index = fieldIDs.size() - 1;
            fieldIDMap.put(fieldID, index);
        }
        return index;
    }

    public int getMethodIDCount() {
        return methodIDs.size();
    }

    public Iterable<MethodID> getMethodIDs() {
        return methodIDs;
    }

    public MethodID getMethodID(int methodIndex) {
        return methodIDs.get(methodIndex);
    }

    public int addOrGetMethodID(String classType, String name, String shorty, String returnType, String... parameterTypes) {
        MethodID methodID =
            MethodID.of(addOrGetTypeIDIndex(classType),
                        addOrGetProtoID(shorty, returnType, parameterTypes),
                        addOrGetStringIDIndex(name));

        Integer index = methodIDMap.get(methodID);
        if (index == null) {
            methodIDs.add(methodID);
            index = methodIDs.size() - 1;
            methodIDMap.put(methodID, index);
        }
        return index;
    }

    public int getClassDefCount() {
        return classDefs.size();
    }

    public Iterable<ClassDef> getClassDefs() {
        return classDefs;
    }

    public ClassDef getClassDef(String className) {
        Integer index = classDefMap.get(className);
        return index == null ? null : classDefs.get(index);
    }

    public ClassDef getClassDef(int classDefIndex) {
        return classDefs.get(classDefIndex);
    }

    public void addClassDef(ClassDef classDef) {
        classDefs.add(classDef);
        classDefMap.put(classDef.getClassName(this), classDefs.size() - 1);
    }

    public int getCallSiteIDCount() {
        return callSiteIDs.size();
    }

    public Iterable<CallSiteID> getCallSiteIDs() {
        return callSiteIDs;
    }

    public CallSiteID getCallSiteID(int callSiteIndex) {
        return callSiteIDs.get(callSiteIndex);
    }

    public int getMethodHandleCount() {
        return methodHandles.size();
    }

    public Iterable<MethodHandle> getMethodHandles() {
        return methodHandles;
    }

    public MethodHandle getMethodHandle(int methodHandleIndex) {
        return methodHandles.get(methodHandleIndex);
    }

    public byte[] getLinkData() {
        return linkData;
    }

    public void accept(DexFileVisitor visitor) {
        visitor.visitDexFile(this);
    }

    public void headerAccept(DexHeaderVisitor visitor) {
        visitor.visitHeader(this, header);
    }

    public void classDefsAccept(ClassDefVisitor visitor) {
        ListIterator<ClassDef> classDefListIterator = classDefs.listIterator();
        while (classDefListIterator.hasNext()) {
            int index = classDefListIterator.nextIndex();
            visitor.visitClassDef(this, index, classDefListIterator.next());
        }
    }

    public void methodHandlesAccept(MethodHandleVisitor visitor) {
        ListIterator<MethodHandle> methodHandleListIterator = methodHandles.listIterator();
        while (methodHandleListIterator.hasNext()) {
            int index = methodHandleListIterator.nextIndex();
            visitor.visitMethodHandle(this, index, methodHandleListIterator.next());
        }
    }

    public void callSiteIDsAccept(CallSiteIDVisitor visitor) {
        ListIterator<CallSiteID> callSiteIDListIterator = callSiteIDs.listIterator();
        while (callSiteIDListIterator.hasNext()) {
            int index = callSiteIDListIterator.nextIndex();
            visitor.visitCallSiteID(this, index, callSiteIDListIterator.next());
        }
    }

    public void dataItemsAccept(DataItemVisitor visitor) {
        visitor.visitHeader(this, header);

        if (mapList != null) {
            visitor.visitMapList(this, mapList);
        }

        for (StringID stringIDItem : stringIDs) {
            visitor.visitStringID(this, stringIDItem);
            stringIDItem.dataItemsAccept(this, visitor);
        }

        for (TypeID typeIDItem : typeIDs) {
            visitor.visitTypeID(this, typeIDItem);
        }

        for (ProtoID protoIDItem : protoIDs) {
            visitor.visitProtoID(this, protoIDItem);
            protoIDItem.dataItemsAccept(this, visitor);
        }

        for (FieldID fieldIDItem : fieldIDs) {
            visitor.visitFieldID(this, fieldIDItem);
        }

        for (MethodID methodIDItem : methodIDs) {
            visitor.visitMethodID(this, methodIDItem);
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

    public void read(InputStream is) throws IOException {
        try (DexDataInput in = new DexDataInput(is)) {
            read(in);
        }
    }

    public void read(DexDataInput in) {
        new Reader(in).read();

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

        protoIDMap = new HashMap<>(getProtoIDCount());
        ListIterator<ProtoID> protoIDIterator = protoIDs.listIterator();
        while (protoIDIterator.hasNext()) {
            int index = protoIDIterator.nextIndex();
            protoIDMap.put(protoIDIterator.next(), index);
        }

        classDefMap = new HashMap<>(getClassDefCount());
        ListIterator<ClassDef> classDefIterator = classDefs.listIterator();
        while (classDefIterator.hasNext()) {
            int index = classDefIterator.nextIndex();
            classDefMap.put(classDefIterator.next().getClassName(this), index);
        }

        fieldIDMap = new HashMap<>(getFieldIDCount());
        ListIterator<FieldID> fieldIDIterator = fieldIDs.listIterator();
        while (fieldIDIterator.hasNext()) {
            int index = fieldIDIterator.nextIndex();
            fieldIDMap.put(fieldIDIterator.next(), index);
        }

        methodIDMap = new HashMap<>(getMethodIDCount());
        ListIterator<MethodID> methodIDIterator = methodIDs.listIterator();
        while (methodIDIterator.hasNext()) {
            int index = methodIDIterator.nextIndex();
            methodIDMap.put(methodIDIterator.next(), index);
        }
    }

    public void write(OutputStream os) throws IOException {
        new Writer(os).write();
    }

    @Override
    public String toString() {
        // TODO: implement a proper version.

        StringBuilder sb = new StringBuilder();

        sb.append(header);

        return sb.toString();
    }

    // helper classes for IO operations.

    private final class Reader {
        private final DexDataInput input;

        public Reader(DexDataInput in) {
            this.input = in;
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
            mapList = MapList.readContent(input);
        }

        private void readStringIDs() {
            input.setOffset(header.stringIDsOffsets);

            stringIDs.clear();
            stringIDs.ensureCapacity(header.stringIDsSize);
            for (int i = 0; i < header.stringIDsSize; i++) {
                StringID stringIDItem = StringID.readContent(input);
                stringIDs.add(i, stringIDItem);
            }
        }

        private void readTypeIDs() {
            input.setOffset(header.typeIDsOffset);

            typeIDs.clear();
            typeIDs.ensureCapacity(header.typeIDsSize);
            for (int i = 0; i < header.typeIDsSize; i++) {
                TypeID typeIDItem = TypeID.readContent(input);
                typeIDs.add(i, typeIDItem);
            }
        }

        private void readProtoIDs() {
            input.setOffset(header.protoIDsOffset);

            protoIDs.clear();
            protoIDs.ensureCapacity(header.protoIDsSize);
            for (int i = 0; i < header.protoIDsSize; i++) {
                ProtoID protoIDItem = ProtoID.readContent(input);
                protoIDs.add(i, protoIDItem);
            }
        }

        private void readFieldIDs() {
            input.setOffset(header.fieldIDsOffset);

            fieldIDs.clear();
            fieldIDs.ensureCapacity(header.fieldIDsSize);
            for (int i = 0; i < header.fieldIDsSize; i++) {
                FieldID fieldIDItem = FieldID.readContent(input);
                fieldIDs.add(i, fieldIDItem);
            }
        }

        private void readMethodIDs() {
            input.setOffset(header.methodIDsOffset);

            methodIDs.clear();
            methodIDs.ensureCapacity(header.methodIDsSize);
            for (int i = 0; i < header.methodIDsSize; i++) {
                MethodID methodIDItem = MethodID.readContent(input);
                methodIDs.add(i, methodIDItem);
            }
        }

        private void readClassDefs() {
            input.setOffset(header.classDefsOffset);

            classDefs.clear();
            classDefs.ensureCapacity(header.classDefsSize);
            for (int i = 0; i < header.classDefsSize; i++) {
                ClassDef classDefItem = ClassDef.readContent(input);
                classDefs.add(i, classDefItem);
            }
        }

        private void readCallSiteIDs() {
            MapItem mapItem = mapList.getMapItemByType(DexConstants.TYPE_CALL_SITE_ID_ITEM);
            if (mapItem != null) {
                input.setOffset(mapItem.getOffset());

                callSiteIDs.clear();
                callSiteIDs.ensureCapacity(mapItem.getSize());
                for (int i = 0; i < mapItem.getSize(); i++) {
                    CallSiteID callSiteIDItem = CallSiteID.readContent(input);
                    callSiteIDs.add(i, callSiteIDItem);
                }
            }
        }

        private void readMethodHandles() {
            MapItem mapItem = mapList.getMapItemByType(DexConstants.TYPE_METHOD_HANDLE_ITEM);
            if (mapItem != null) {
                input.setOffset(mapItem.getOffset());

                methodHandles.clear();
                methodHandles.ensureCapacity(mapItem.getSize());
                for (int i = 0; i < mapItem.getSize(); i++) {
                    MethodHandle methodHandleItem = MethodHandle.readContent(input);
                    methodHandles.add(i, methodHandleItem);
                }
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

    private final class Writer {
        private final OutputStream    os;
        private final DataItemMapImpl dataItemMap;

        private int dataOffset;
        private int dataSize;

        Writer(OutputStream os) {
            this.os          = os;
            this.dataItemMap = new DataItemMapImpl();
        }

        public void write() throws IOException {
            dataItemMap.collectDataItems(DexFile.this);

            dataOffset = 0;
            dataSize   = 0;

            // In the first pass we update all the offsets.
            DexDataOutput countingOutput = new CountingDexDataOutput();
            writeDexFile(countingOutput);

            int size = countingOutput.getOffset();
            header.fileSize   = size;
            header.dataOffset = dataOffset;
            header.dataSize   = dataSize;

            ByteBufferBackedDexDataOutput output = new ByteBufferBackedDexDataOutput(size);
            MapList newMapList = writeDexFile(output);

            ByteBuffer byteBuffer = output.getByteBuffer();

            Hasher sha1Signature = Hashing.sha1().newHasher();
            byteBuffer.position(32);
            sha1Signature.putBytes(byteBuffer);
            header.signature = sha1Signature.hash().asBytes();
            byteBuffer.position(12);
            byteBuffer.put(header.signature);

            Hasher adlerChecksum = Hashing.adler32().newHasher();
            byteBuffer.position(12);
            adlerChecksum.putBytes(byteBuffer);
            header.checksum = adlerChecksum.hash().asInt();
            byteBuffer.position(8);
            byteBuffer.putInt((int) header.checksum);

            // set the newly created MapList.
            mapList = newMapList;

            output.copy(os);
        }

        private MapList writeDexFile(DexDataOutput output) {
            MapList mapList = MapList.empty();

            writeHeader(mapList, output);

            output.writePadding((int) Math.max(0, header.headerSize - output.getOffset()));

            writeStringIDs(mapList, output);
            writeTypeIDs  (mapList, output);
            writeProtoIDs (mapList, output);
            writeFieldIDs (mapList, output);
            writeMethodIDs(mapList, output);
            writeClassDefs(mapList, output);

            writeCallSiteIDs  (mapList, output);
            writeMethodHandles(mapList, output);

            writeDataSection(mapList, output);

            writeMapList (mapList, output);
            writeLinkData(output);

            return mapList;
        }

        private void writePadding(Class<? extends DataItem> clazz, DexDataOutput output) {
            output.writeAlignmentPadding(clazz.getAnnotation(DataItemAnn.class).dataAlignment());
        }

        private void writeHeader(MapList mapList, DexDataOutput output) {
            mapList.updateMapItem(DexConstants.TYPE_HEADER_ITEM, 1, output.getOffset());
            header.write(output);
        }

        private void writeStringIDs(MapList mapList, DexDataOutput output) {
            writePadding(StringID.class, output);
            header.updateDataItem(DexConstants.TYPE_STRING_ID_ITEM, getStringIDCount(), output.getOffset());
            mapList.updateMapItem(DexConstants.TYPE_STRING_ID_ITEM, getStringIDCount(), output.getOffset());

            for (StringID stringID : getStringIDs()) {
                stringID.write(output);
            }
        }

        private void writeTypeIDs(MapList mapList, DexDataOutput output) {
            writePadding(TypeID.class, output);
            header.updateDataItem(DexConstants.TYPE_TYPE_ID_ITEM, getTypeIDCount(), output.getOffset());
            mapList.updateMapItem(DexConstants.TYPE_TYPE_ID_ITEM, getTypeIDCount(), output.getOffset());

            for (TypeID typeID : getTypeIDs()) {
                typeID.write(output);
            }
        }

        private void writeProtoIDs(MapList mapList, DexDataOutput output) {
            writePadding(ProtoID.class, output);
            header.updateDataItem(DexConstants.TYPE_PROTO_ID_ITEM, getProtoIDCount(), output.getOffset());
            mapList.updateMapItem(DexConstants.TYPE_PROTO_ID_ITEM, getProtoIDCount(), output.getOffset());

            for (ProtoID protoID : getProtoIDs()) {
                protoID.write(output);
            }
        }

        private void writeFieldIDs(MapList mapList, DexDataOutput output) {
            writePadding(FieldID.class, output);
            header.updateDataItem(DexConstants.TYPE_FIELD_ID_ITEM, getFieldIDCount(), output.getOffset());
            mapList.updateMapItem(DexConstants.TYPE_FIELD_ID_ITEM, getFieldIDCount(), output.getOffset());

            for (FieldID fieldID : getFieldIDs()) {
                fieldID.write(output);
            }
        }

        private void writeMethodIDs(MapList mapList, DexDataOutput output) {
            writePadding(MethodID.class, output);
            header.updateDataItem(DexConstants.TYPE_METHOD_ID_ITEM, getMethodIDCount(), output.getOffset());
            mapList.updateMapItem(DexConstants.TYPE_METHOD_ID_ITEM, getMethodIDCount(), output.getOffset());

            for (MethodID methodID : getMethodIDs()) {
                methodID.write(output);
            }
        }

        private void writeClassDefs(MapList mapList, DexDataOutput output) {
            writePadding(ClassDef.class, output);
            header.updateDataItem(DexConstants.TYPE_CLASS_DEF_ITEM, getClassDefCount(), output.getOffset());
            mapList.updateMapItem(DexConstants.TYPE_CLASS_DEF_ITEM, getClassDefCount(), output.getOffset());

            for (ClassDef classDef : getClassDefs()) {
                classDef.write(output);
            }
        }

        private void writeCallSiteIDs(MapList mapList, DexDataOutput output) {
            if (getCallSiteIDCount() > 0) {
                writePadding(CallSiteID.class, output);
                mapList.updateMapItem(DexConstants.TYPE_CALL_SITE_ID_ITEM, getCallSiteIDCount(), output.getOffset());

                for (CallSiteID callSiteID : getCallSiteIDs()) {
                    callSiteID.write(output);
                }
            }
        }

        private void writeMethodHandles(MapList mapList, DexDataOutput output) {
            if (getMethodHandleCount() > 0) {
                writePadding(MethodHandle.class, output);
                mapList.updateMapItem(DexConstants.TYPE_METHOD_HANDLE_ITEM, getMethodHandleCount(), output.getOffset());

                for (MethodHandle methodHandle : getMethodHandles()) {
                    methodHandle.write(output);
                }
            }
        }

        private void writeDataSection(MapList mapList, DexDataOutput output) {
            dataOffset = output.getOffset();

            // Collect all DataItems that reside in the data section.
            dataItemMap.writeDataItems(mapList, output);
            dataItemMap.updateOffsets(DexFile.this);

            dataSize = output.getOffset() - dataOffset;
        }

        private void writeMapList(MapList mapList, DexDataOutput output) {
            writePadding(MapList.class, output);
            header.updateDataItem(DexConstants.TYPE_MAP_LIST, 0, output.getOffset());
            mapList.updateMapItem(DexConstants.TYPE_MAP_LIST, 1, output.getOffset());
            mapList.write(output);
        }

        private void writeLinkData(DexDataOutput output) {
            byte[] linkData = getLinkData();
            if (linkData != null) {
                header.updateLinkData(linkData.length, output.getOffset());
                output.writeBytes(linkData);
            } else {
                header.updateLinkData(0, 0);
            }
        }
    }
}
