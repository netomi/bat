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
import com.github.netomi.bat.dexfile.visitor.DataItemVisitor;
import com.github.netomi.bat.dexfile.visitor.DexFileVisitor;
import com.github.netomi.bat.util.Primitives;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.Adler32;

public class DexFileReader
implements   DexFileVisitor
{
    private final DexDataInput input;
    private final boolean      strictParsing;

    public DexFileReader(InputStream in) throws IOException {
        this(in, true);
    }

    public DexFileReader(InputStream in, boolean strict) throws IOException {
        input         = new DexDataInput(in);
        strictParsing = strict;
    }

    @Override
    public void visitDexFile(DexFile dexFile) {
        readHeader(dexFile);
        readMapList(dexFile);

        readStringIDs(dexFile);
        readTypeIDs(dexFile);
        readProtoIDs(dexFile);
        readFieldIDs(dexFile);
        readMethodIDs(dexFile);
        readClassDefs(dexFile);

        readCallSiteIDs(dexFile);
        readMethodHandles(dexFile);

        readLinkedDataItems(dexFile);

        readLinkData(dexFile);

        if (strictParsing) {
            verifyChecksum(dexFile);
        }
    }

    private void readHeader(DexFile dexFile) {
        dexFile.header.read(input);
        // also read the MapList now as it is needed for other DataItems.
        dexFile.header.readLinkedDataItems(input);
    }

    private void readMapList(DexFile dexFile) {
        input.setOffset(dexFile.header.mapOffset);
        input.skipAlignmentPadding(4);

        dexFile.mapList = new MapList();
        dexFile.mapList.read(input);
    }

    private void readStringIDs(DexFile dexFile) {
        input.setOffset(dexFile.header.stringIDsOffsets);

        dexFile.stringIDs = new StringID[dexFile.header.stringIDsSize];
        for (int i = 0; i < dexFile.header.stringIDsSize; i++) {
            StringID stringIDItem = new StringID();
            stringIDItem.read(input);
            dexFile.stringIDs[i] = stringIDItem;
        }
    }

    private void readTypeIDs(DexFile dexFile) {
        input.setOffset(dexFile.header.typeIDsOffset);

        dexFile.typeIDs = new TypeID[dexFile.header.typeIDsSize];
        for (int i = 0; i < dexFile.header.typeIDsSize; i++) {
            TypeID typeIDItem = new TypeID();
            typeIDItem.read(input);
            dexFile.typeIDs[i] = typeIDItem;
        }
    }

    private void readProtoIDs(DexFile dexFile) {
        input.setOffset(dexFile.header.protoIDsOffset);

        dexFile.protoIDs = new ProtoID[dexFile.header.protoIDsSize];
        for (int i = 0; i < dexFile.header.protoIDsSize; i++) {
            ProtoID protoIDItem = new ProtoID();
            protoIDItem.read(input);
            dexFile.protoIDs[i] = protoIDItem;
        }
    }

    private void readFieldIDs(DexFile dexFile) {
        input.setOffset(dexFile.header.fieldIDsOffset);

        dexFile.fieldIDs = new FieldID[dexFile.header.fieldIDsSize];
        for (int i = 0; i < dexFile.header.fieldIDsSize; i++) {
            FieldID fieldIDItem = new FieldID();
            fieldIDItem.read(input);
            dexFile.fieldIDs[i] = fieldIDItem;
        }
    }

    private void readMethodIDs(DexFile dexFile) {
        input.setOffset(dexFile.header.methodIDsOffset);

        dexFile.methodIDs = new MethodID[dexFile.header.methodIDsSize];
        for (int i = 0; i < dexFile.header.methodIDsSize; i++) {
            MethodID methodIDItem = new MethodID();
            methodIDItem.read(input);
            dexFile.methodIDs[i] = methodIDItem;
        }
    }

    private void readClassDefs(DexFile dexFile) {
        input.setOffset(dexFile.header.classDefsOffset);

        dexFile.classDefs = new ClassDef[dexFile.header.classDefsSize];
        for (int i = 0; i < dexFile.header.classDefsSize; i++) {
            ClassDef classDefItem = new ClassDef();
            classDefItem.read(input);
            dexFile.classDefs[i] = classDefItem;
        }
    }

    private void readCallSiteIDs(DexFile dexFile) {
        MapItem mapItem = dexFile.mapList.getMapItem(DexConstants.TYPE_CALL_SITE_ID_ITEM);
        if (mapItem != null) {
            input.setOffset(mapItem.getOffset());

            dexFile.callSiteIDs = new CallSiteID[mapItem.getSize()];
            for (int i = 0; i < mapItem.getSize(); i++) {
                CallSiteID callSiteIDItem = new CallSiteID();
                callSiteIDItem.read(input);
                dexFile.callSiteIDs[i] = callSiteIDItem;
            }
        } else {
            dexFile.callSiteIDs = new CallSiteID[0];
        }
    }

    private void readMethodHandles(DexFile dexFile) {
        MapItem mapItem = dexFile.mapList.getMapItem(DexConstants.TYPE_METHOD_HANDLE_ITEM);
        if (mapItem != null) {
            input.setOffset(mapItem.getOffset());

            dexFile.methodHandles = new MethodHandle[mapItem.getSize()];
            for (int i = 0; i < mapItem.getSize(); i++) {
                MethodHandle methodHandleItem = new MethodHandle();
                methodHandleItem.read(input);
                dexFile.methodHandles[i] = methodHandleItem;
            }
        } else {
            dexFile.methodHandles = new MethodHandle[0];
        }
    }

    private void readLinkedDataItems(DexFile dexFile) {
        dexFile.dataItemsAccept(new DataItemVisitor() {
            @Override
            public void visitAnyDataItem(DexFile dexFile, DataItem dataItem) {
                dataItem.readLinkedDataItems(input);
            }
        });
    }

    private void readLinkData(DexFile dexFile) {
        dexFile.linkData = new byte[dexFile.header.linkSize];
        input.setOffset(dexFile.header.linkOffset);
        input.readFully(dexFile.linkData);
    }

    private void verifyChecksum(DexFile dexFile) {
        input.setOffset(12);

        Adler32 adler32 = new Adler32();
        input.update(adler32);

        long checksum = adler32.getValue();
        if (checksum != dexFile.header.checksum) {
            throw new DexFormatException(String.format("Calculated checksum [%s] does not match [%s].",
                                                       Primitives.toHexString(checksum),
                                                       Primitives.toHexString(dexFile.header.checksum)));
        }
    }
}
