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
package com.github.netomi.bat.dexfile.io;

import com.github.netomi.bat.dexfile.*;
import com.github.netomi.bat.dexfile.visitor.DexFileVisitor;

import java.io.IOException;
import java.io.OutputStream;

public class DexFileWriter
implements DexFileVisitor
{
    private final OutputStream    outputStream;
    private final DataItemMapImpl dataItemMap;

    public DexFileWriter(OutputStream out) {
        this.outputStream = out;
        this.dataItemMap  = new DataItemMapImpl();
    }

    @Override
    public void visitDexFile(DexFile dexFile) {
        collectDataItemsForDataSection(dexFile);

        // In the first pass we update all the offsets.
        DexDataOutput output = new CountingDexDataOutput();
        writeDexFile(dexFile, output);
        // TODO: sort maplist based on offset

        System.out.println("Last offset: " + output.getOffset());

        int size = output.getOffset();
        System.out.println(dexFile);


        ByteBufferBackedDexDataOutput realOutput = new ByteBufferBackedDexDataOutput(size);
        writeDexFile(dexFile, realOutput);

        try {
            realOutput.copy(outputStream);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to write dex file: " + ex);
        }
    }

    private void writeDexFile(DexFile dexFile, DexDataOutput output) {
        writeHeader(dexFile, output);

        output.writePadding((int) dexFile.header.headerSize - output.getOffset());

        writeStringIDs(dexFile, output);
        writeTypeIDs(dexFile, output);
        writeProtoIDs(dexFile, output);
        writeFieldIDs(dexFile, output);
        writeMethodIDs(dexFile, output);
        writeClassDefs(dexFile, output);

        writeCallSiteIDs(dexFile, output);
        writeMethodHandles(dexFile, output);

        writeDataSection(dexFile, output);
        
        writeMapList(dexFile, output);
        writeLinkData(dexFile, output);
    }

    private void writePadding(Class<? extends DataItem> clazz, DexDataOutput output) {
        output.writeAlignmentPadding(clazz.getAnnotation(DataItemAnn.class).dataAlignment());
    }

    private void writeHeader(DexFile dexFile, DexDataOutput output) {
        updateMapList(dexFile, DexConstants.TYPE_HEADER_ITEM, 1, output.getOffset());
        dexFile.header.write(output);
    }

    private void writeStringIDs(DexFile dexFile, DexDataOutput output) {
        writePadding(StringID.class, output);
        dexFile.header.stringIDsSize    = dexFile.stringIDs.length;
        dexFile.header.stringIDsOffsets = output.getOffset();
        updateMapList(dexFile, DexConstants.TYPE_STRING_ID_ITEM, dexFile.stringIDs.length, output.getOffset());

        for (StringID stringID : dexFile.stringIDs) {
            stringID.write(output);
        }
    }

    private void writeTypeIDs(DexFile dexFile, DexDataOutput output) {
        writePadding(TypeID.class, output);
        dexFile.header.typeIDsSize   = dexFile.typeIDs.length;
        dexFile.header.typeIDsOffset = output.getOffset();
        updateMapList(dexFile, DexConstants.TYPE_TYPE_ID_ITEM, dexFile.typeIDs.length, output.getOffset());

        for (TypeID typeID : dexFile.typeIDs) {
            typeID.write(output);
        }
    }

    private void writeProtoIDs(DexFile dexFile, DexDataOutput output) {
        writePadding(ProtoID.class, output);
        dexFile.header.protoIDsSize   = dexFile.protoIDs.length;
        dexFile.header.protoIDsOffset = output.getOffset();
        updateMapList(dexFile, DexConstants.TYPE_PROTO_ID_ITEM, dexFile.protoIDs.length, output.getOffset());

        for (ProtoID protoID : dexFile.protoIDs) {
            protoID.write(output);
        }
    }

    private void writeFieldIDs(DexFile dexFile, DexDataOutput output) {
        writePadding(FieldID.class, output);
        dexFile.header.fieldIDsSize   = dexFile.fieldIDs.length;
        dexFile.header.fieldIDsOffset = output.getOffset();
        updateMapList(dexFile, DexConstants.TYPE_FIELD_ID_ITEM, dexFile.fieldIDs.length, output.getOffset());

        for (FieldID fieldID : dexFile.fieldIDs) {
            fieldID.write(output);
        }
    }

    private void writeMethodIDs(DexFile dexFile, DexDataOutput output) {
        writePadding(MethodID.class, output);
        dexFile.header.methodIDsSize   = dexFile.methodIDs.length;
        dexFile.header.methodIDsOffset = output.getOffset();
        updateMapList(dexFile, DexConstants.TYPE_METHOD_ID_ITEM, dexFile.methodIDs.length, output.getOffset());

        for (MethodID methodID : dexFile.methodIDs) {
            methodID.write(output);
        }
    }

    private void writeClassDefs(DexFile dexFile, DexDataOutput output) {
        writePadding(ClassDef.class, output);
        dexFile.header.classDefsSize   = dexFile.classDefs.length;
        dexFile.header.classDefsOffset = output.getOffset();
        updateMapList(dexFile, DexConstants.TYPE_CLASS_DEF_ITEM, dexFile.classDefs.length, output.getOffset());

        for (ClassDef classDef : dexFile.classDefs) {
            classDef.write(output);
        }
    }

    private void writeCallSiteIDs(DexFile dexFile, DexDataOutput output) {
        if (dexFile.callSiteIDs.length > 0) {
            writePadding(CallSiteID.class, output);
            updateMapList(dexFile, DexConstants.TYPE_CALL_SITE_ID_ITEM, dexFile.callSiteIDs.length, output.getOffset());

            for (CallSiteID callSiteID : dexFile.callSiteIDs) {
                callSiteID.write(output);
            }
        }
    }

    private void writeMethodHandles(DexFile dexFile, DexDataOutput output) {
        if (dexFile.methodHandles.length > 0) {
            writePadding(MethodHandle.class, output);
            updateMapList(dexFile, DexConstants.TYPE_METHOD_HANDLE_ITEM, dexFile.callSiteIDs.length, output.getOffset());

            for (MethodHandle methodHandle : dexFile.methodHandles) {
                methodHandle.write(output);
            }
        }
    }

    private void writeDataSection(DexFile dexFile, DexDataOutput output) {
        // Collect all DataItems that reside in the data section.
        dataItemMap.writeDataItems(dexFile, output);
        dataItemMap.updateOffsets(dexFile);
    }

    private void collectDataItemsForDataSection(DexFile dexFile) {
        dataItemMap.collectDataItems(dexFile);
    }

    private void updateMapList(DexFile dexFile, int type, int size, int offset) {
        MapList mapList = dexFile.mapList;
        mapList.updateMapItem(type, size, offset);
    }

    private void writeMapList(DexFile dexFile, DexDataOutput output) {
        dexFile.header.mapOffset = output.getOffset();
        dexFile.mapList.write(output);
    }

    private void writeLinkData(DexFile dexFile, DexDataOutput output) {
        dexFile.header.linkSize   = dexFile.linkData.length;
        dexFile.header.linkOffset = output.getOffset();
        output.writeBytes(dexFile.linkData);
    }
}
