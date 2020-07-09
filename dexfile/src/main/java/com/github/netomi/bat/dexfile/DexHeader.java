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
import com.github.netomi.bat.dexfile.io.DexFormatException;
import com.github.netomi.bat.dexfile.util.Primitives;
import com.github.netomi.bat.dexfile.visitor.DataItemVisitor;

import java.nio.ByteOrder;
import java.util.Arrays;

@DataItemAnn(
        type          = DexConstants.TYPE_HEADER_ITEM,
        dataAlignment = 4,
        dataSection   = false
)
public class DexHeader
implements   DataItem
{
    private static final byte[] EMPTY_ARRAY = new byte[0];

    public byte[]  magic;           // ubyte[8]
    public long    checksum;        // uint
    public byte[]  signature;       // ubyte[20]
    public long    fileSize;        // uint
    public long    headerSize;      // uint
    public long    endianTag;       // uint

    public int     linkSize;         // uint
    public int     linkOffset;       // uint
    public int     mapOffset;        // uint

    public int     stringIDsSize;    // uint
    public int     stringIDsOffsets; // uint

    public int     typeIDsSize;      // uint
    public int     typeIDsOffset;    // uint

    public int     protoIDsSize;     // uint
    public int     protoIDsOffset;   // uint

    public int     fieldIDsSize;     // uint
    public int     fieldIDsOffset;   // uint

    public int     methodIDsSize;    // uint
    public int     methodIDsOffset;  // uint

    public int     classDefsSize;    // uint
    public int     classDefsOffset;  // uint

    public int     dataSize;         // uint
    public int     dataOffset;       // uint

    public MapList mapList;

    public DexHeader() {
        magic     = EMPTY_ARRAY;
        signature = EMPTY_ARRAY;
        mapList   = null;
    }

    @Override
    public void read(DexDataInput input) {
        magic = new byte[8];
        input.readFully(magic);

        if (!Arrays.equals(DexConstants.DEX_FILE_MAGIC, Arrays.copyOf(magic, 4))) {
            throw new DexFormatException("Not a valid dex file: file magic does not match.");
        }

        DexFormat format = DexFormat.fromPattern(magic, 4, 8);
        if (format == null) {
            throw new DexFormatException("Unsupported dex format: " +
                                         Arrays.toString(Arrays.copyOfRange(magic, 4, 8)));
        }

        input.skipBytes(32);

        endianTag = input.readUnsignedInt();
        if (endianTag == DexConstants.REVERSE_ENDIAN_CONSTANT) {
            input.order(ByteOrder.LITTLE_ENDIAN);
        }

        input.setOffset(8);

        checksum = input.readUnsignedInt();

        signature = new byte[20];
        input.readFully(signature);

        fileSize   = input.readUnsignedInt();
        headerSize = input.readUnsignedInt();

        // skip the endian tag, already read.
        input.readUnsignedInt();

        linkSize   = input.readInt();
        linkOffset = input.readInt();
        mapOffset  = input.readInt();

        stringIDsSize    = input.readInt();
        stringIDsOffsets = input.readInt();

        typeIDsSize   = input.readInt();
        typeIDsOffset = input.readInt();

        protoIDsSize   = input.readInt();
        protoIDsOffset = input.readInt();

        fieldIDsSize   = input.readInt();
        fieldIDsOffset = input.readInt();

        methodIDsSize   = input.readInt();
        methodIDsOffset = input.readInt();

        classDefsSize   = input.readInt();
        classDefsOffset = input.readInt();

        dataSize   = input.readInt();
        dataOffset = input.readInt();
    }

    @Override
    public void readLinkedDataItems(DexDataInput input) {
        // Prevent reading the MapList twice.
        if (mapList == null) {
            int offset = input.getOffset();

            input.setOffset(mapOffset);
            input.skipAlignmentPadding(4);

            mapList = new MapList();
            mapList.read(input);

            input.setOffset(offset);
        }
    }

    @Override
    public void write(DexDataOutput output) {
        output.writeBytes(magic);
        output.writeUnsignedInt(checksum);
        output.writeBytes(signature);
        output.writeUnsignedInt(fileSize);
        output.writeUnsignedInt(headerSize);

        if (output.order() == ByteOrder.LITTLE_ENDIAN) {
            output.writeUnsignedInt(DexConstants.ENDIAN_CONSTANT);
        } else {
            output.writeUnsignedInt(DexConstants.REVERSE_ENDIAN_CONSTANT);
        }

        output.writeUnsignedInt(linkSize);
        output.writeUnsignedInt(linkOffset);
        output.writeUnsignedInt(mapOffset);
        output.writeUnsignedInt(stringIDsSize);
        output.writeUnsignedInt(stringIDsOffsets);
        output.writeUnsignedInt(typeIDsSize);
        output.writeUnsignedInt(typeIDsOffset);
        output.writeUnsignedInt(protoIDsSize);
        output.writeUnsignedInt(protoIDsOffset);
        output.writeUnsignedInt(fieldIDsSize);
        output.writeUnsignedInt(fieldIDsOffset);
        output.writeUnsignedInt(methodIDsSize);
        output.writeUnsignedInt(methodIDsOffset);
        output.writeUnsignedInt(classDefsSize);
        output.writeUnsignedInt(classDefsOffset);
        output.writeUnsignedInt(dataSize);
        output.writeUnsignedInt(dataOffset);
    }

    @Override
    public void dataItemsAccept(DexFile dexFile, DataItemVisitor visitor) {
        visitor.visitMapList(dexFile, this, mapList);
        mapList.dataItemsAccept(dexFile, visitor);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("DexHeader: \n");

        sb.append("  magic           : " + Primitives.toHexString(magic) + "\n");
        sb.append("  checksum        : " + Primitives.toHexString(checksum) + "\n");
        sb.append("  signature       : " + Primitives.toHexString(signature) + "\n");
        sb.append("  fileSize        : " + fileSize + "\n");
        sb.append("  headerSize      : " + headerSize + "\n");
        sb.append("  endianTag       : " + Primitives.toHexString(endianTag) + "\n");

        sb.append("  linkSize        : " + linkSize + "\n");
        sb.append("  linkOffset      : " + linkOffset + "\n");
        sb.append("  mapOffset       : " + mapOffset + "\n");

        sb.append("  stringIDsSize   : " + stringIDsSize + "\n");
        sb.append("  stringIDsOffsets: " + stringIDsOffsets + "\n");

        sb.append("  typeIDsSize     : " + typeIDsSize + "\n");
        sb.append("  typeIDsOffset   : " + typeIDsOffset + "\n");

        sb.append("  protoIDsSize    : " + protoIDsSize + "\n");
        sb.append("  protoIDsOffset  : " + protoIDsOffset + "\n");

        sb.append("  fieldIDsSize    : " + fieldIDsSize + "\n");
        sb.append("  fieldIDsOffset  : " + fieldIDsOffset + "\n");

        sb.append("  methodIDsSize   : " + methodIDsSize + "\n");
        sb.append("  methodIDsOffset : " + methodIDsOffset + "\n");

        sb.append("  classDefsSize   : " + classDefsSize + "\n");
        sb.append("  classDefsOffset : " + classDefsOffset + "\n");

        sb.append("  dataSize        : " + dataSize + "\n");
        sb.append("  dataOffset      : " + dataOffset + "\n");

        if (mapList != null) {
            sb.append(mapList.toString());
        }
        return sb.toString();
    }
}
