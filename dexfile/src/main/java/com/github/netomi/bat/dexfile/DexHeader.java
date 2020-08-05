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
import com.github.netomi.bat.util.EmptyArray;
import com.github.netomi.bat.util.Primitives;

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
    public byte[]  magic;           // ubyte[8]
    public long    checksum;        // uint
    public byte[]  signature;       // ubyte[20]

    long fileSize;        // uint
    long headerSize;      // uint
    long endianTag;       // uint

    int linkSize;         // uint
    int linkOffset;       // uint
    int mapOffset;        // uint

    int stringIDsSize;    // uint
    int stringIDsOffsets; // uint

    int typeIDsSize;      // uint
    int typeIDsOffset;    // uint

    int protoIDsSize;     // uint
    int protoIDsOffset;   // uint

    int fieldIDsSize;     // uint
    int fieldIDsOffset;   // uint

    int methodIDsSize;    // uint
    int methodIDsOffset;  // uint

    int classDefsSize;    // uint
    int classDefsOffset;  // uint

    int dataSize;         // uint
    int dataOffset;       // uint

    public DexHeader() {
        magic     = EmptyArray.BYTE;
        signature = EmptyArray.BYTE;
    }

    public long getFileSize() {
        return fileSize;
    }

    public long getHeaderSize() {
        return headerSize;
    }

    public long getEndianTag() {
        return endianTag;
    }

    public int getLinkSize() {
        return linkSize;
    }

    public int getLinkOffset() {
        return linkOffset;
    }

    public int getMapOffset() {
        return mapOffset;
    }

    public int getStringIDsSize() {
        return stringIDsSize;
    }

    public int getStringIDsOffsets() {
        return stringIDsOffsets;
    }

    public int getTypeIDsSize() {
        return typeIDsSize;
    }

    public int getTypeIDsOffset() {
        return typeIDsOffset;
    }

    public int getProtoIDsSize() {
        return protoIDsSize;
    }

    public int getProtoIDsOffset() {
        return protoIDsOffset;
    }

    public int getFieldIDsSize() {
        return fieldIDsSize;
    }

    public int getFieldIDsOffset() {
        return fieldIDsOffset;
    }

    public int getMethodIDsSize() {
        return methodIDsSize;
    }

    public int getMethodIDsOffset() {
        return methodIDsOffset;
    }

    public int getClassDefsSize() {
        return classDefsSize;
    }

    public int getClassDefsOffset() {
        return classDefsOffset;
    }

    public int getDataSize() {
        return dataSize;
    }

    public int getDataOffset() {
        return dataOffset;
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

    public void updateDataItem(int type, int count, int offset) {
        switch (type) {
            case DexConstants.TYPE_STRING_ID_ITEM:
                this.stringIDsSize    = count;
                this.stringIDsOffsets = offset;
                break;

            case DexConstants.TYPE_TYPE_ID_ITEM:
                this.typeIDsSize   = count;
                this.typeIDsOffset = offset;
                break;

            case DexConstants.TYPE_PROTO_ID_ITEM:
                this.protoIDsSize   = count;
                this.protoIDsOffset = offset;
                break;

            case DexConstants.TYPE_FIELD_ID_ITEM:
                this.fieldIDsSize   = count;
                this.fieldIDsOffset = offset;
                break;

            case DexConstants.TYPE_METHOD_ID_ITEM:
                this.methodIDsSize   = count;
                this.methodIDsOffset = offset;
                break;

            case DexConstants.TYPE_CLASS_DEF_ITEM:
                this.classDefsSize   = count;
                this.classDefsOffset = offset;
                break;

            case DexConstants.TYPE_MAP_LIST:
                this.mapOffset = offset;
                break;

            default:
                throw new IllegalArgumentException("unexpected DataItem type: " + type);
        }
    }

    public void updateLinkData(int size, int offset) {
        this.linkSize   = size;
        this.linkOffset = offset;
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

        return sb.toString();
    }
}
