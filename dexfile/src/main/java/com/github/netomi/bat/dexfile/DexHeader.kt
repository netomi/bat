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
package com.github.netomi.bat.dexfile

import com.github.netomi.bat.dexfile.DexFormat.Companion.fromPattern
import com.github.netomi.bat.dexfile.io.DexDataInput
import com.github.netomi.bat.dexfile.io.DexDataOutput
import com.github.netomi.bat.dexfile.io.DexFormatException
import com.github.netomi.bat.util.contentToHexString
import com.github.netomi.bat.util.toHexStringWithPrefix
import com.google.common.primitives.Bytes
import java.nio.ByteOrder

@DataItemAnn(
    type          = TYPE_HEADER_ITEM,
    dataAlignment = 4,
    dataSection   = false)
class DexHeader private constructor() : DataItem() {

    private constructor(format: DexFormat): this() {
        magic = Bytes.concat(DEX_FILE_MAGIC, format.pattern)
    }

    var magic: ByteArray = ByteArray(8) // ubyte[8]
        internal set
    var checksum: UInt = 0u // uint
        internal set
    var signature: ByteArray = ByteArray(20) // ubyte[20]
        internal set
    var fileSize: Long = 0 // uint
        internal set
    var headerSize: Long = 0x70 // uint
        internal set
    var endianTag: Long = 0 // uint
        internal set
    var linkSize = 0 // uint
        internal set
    var linkOffset = 0 // uint
        internal set
    var mapOffset = 0 // uint
        internal set
    var stringIDsSize = 0 // uint
        internal set
    var stringIDsOffsets = 0 // uint
        internal set
    var typeIDsSize = 0 // uint
        internal set
    var typeIDsOffset = 0 // uint
        internal set
    var protoIDsSize = 0 // uint
        internal set
    var protoIDsOffset = 0 // uint
        internal set
    var fieldIDsSize = 0 // uint
        internal set
    var fieldIDsOffset = 0 // uint
        internal set
    var methodIDsSize = 0 // uint
        internal set
    var methodIDsOffset = 0 // uint
        internal set
    var classDefsSize = 0 // uint
        internal set
    var classDefsOffset = 0 // uint
        internal set
    var dataSize = 0 // uint
        internal set
    var dataOffset = 0 // uint
        internal set

    val dexFormat: DexFormat?
        get() = fromPattern(magic, 4, 8)

    override val isEmpty: Boolean
        get() = false

    public override fun read(input: DexDataInput) {
        input.readFully(magic)

        if (!DEX_FILE_MAGIC.contentEquals(magic.copyOf(4))) {
            throw DexFormatException("Invalid dex file: unexpected magic ${magic.copyOf(4).contentToHexString()}")
        }

        fromPattern(magic, 4, 8) ?:
            throw DexFormatException("Unsupported dex format: ${magic.copyOfRange(4, 8).contentToHexString()}")

        input.skipBytes(32)
        endianTag = input.readUnsignedInt()
        if (endianTag == REVERSE_ENDIAN_CONSTANT) {
            input.order(ByteOrder.BIG_ENDIAN)
        }

        input.offset = 8
        checksum = input.readUnsignedInt().toUInt()
        input.readFully(signature)

        fileSize = input.readUnsignedInt()
        headerSize = input.readUnsignedInt()

        // skip the endian tag, already read.
        input.readUnsignedInt()

        linkSize = input.readInt()
        linkOffset = input.readInt()
        mapOffset = input.readInt()
        stringIDsSize = input.readInt()
        stringIDsOffsets = input.readInt()
        typeIDsSize = input.readInt()
        typeIDsOffset = input.readInt()
        protoIDsSize = input.readInt()
        protoIDsOffset = input.readInt()
        fieldIDsSize = input.readInt()
        fieldIDsOffset = input.readInt()
        methodIDsSize = input.readInt()
        methodIDsOffset = input.readInt()
        classDefsSize = input.readInt()
        classDefsOffset = input.readInt()
        dataSize = input.readInt()
        dataOffset = input.readInt()
    }

    override fun write(output: DexDataOutput) {
        output.apply {
            writeBytes(magic)
            writeUnsignedInt(checksum.toLong())
            writeBytes(signature)
            writeUnsignedInt(fileSize)
            writeUnsignedInt(headerSize)

            if (output.order() == ByteOrder.LITTLE_ENDIAN) {
                writeUnsignedInt(ENDIAN_CONSTANT)
            } else {
                writeUnsignedInt(REVERSE_ENDIAN_CONSTANT)
            }

            writeUnsignedInt(linkSize.toLong())
            writeUnsignedInt(linkOffset.toLong())
            writeUnsignedInt(mapOffset.toLong())
            writeUnsignedInt(stringIDsSize.toLong())
            writeUnsignedInt(stringIDsOffsets.toLong())
            writeUnsignedInt(typeIDsSize.toLong())
            writeUnsignedInt(typeIDsOffset.toLong())
            writeUnsignedInt(protoIDsSize.toLong())
            writeUnsignedInt(protoIDsOffset.toLong())
            writeUnsignedInt(fieldIDsSize.toLong())
            writeUnsignedInt(fieldIDsOffset.toLong())
            writeUnsignedInt(methodIDsSize.toLong())
            writeUnsignedInt(methodIDsOffset.toLong())
            writeUnsignedInt(classDefsSize.toLong())
            writeUnsignedInt(classDefsOffset.toLong())
            writeUnsignedInt(dataSize.toLong())
            writeUnsignedInt(dataOffset.toLong())
        }
    }

    internal fun updateDataItem(type: Int, count: Int, offset: Int) {
        when (type) {
            TYPE_STRING_ID_ITEM -> {
                stringIDsSize = count
                stringIDsOffsets = if (count > 0) offset else 0
            }
            TYPE_TYPE_ID_ITEM -> {
                typeIDsSize = count
                typeIDsOffset = if (count > 0) offset else 0
            }
            TYPE_PROTO_ID_ITEM -> {
                protoIDsSize = count
                protoIDsOffset = if (count > 0) offset else 0
            }
            TYPE_FIELD_ID_ITEM -> {
                fieldIDsSize = count
                fieldIDsOffset = if (count > 0) offset else 0
            }
            TYPE_METHOD_ID_ITEM -> {
                methodIDsSize = count
                methodIDsOffset = if (count > 0) offset else 0
            }
            TYPE_CLASS_DEF_ITEM -> {
                classDefsSize = count
                classDefsOffset = if (count > 0) offset else 0
            }
            TYPE_MAP_LIST -> mapOffset = offset
            else -> throw IllegalArgumentException("unexpected DataItem type: $type")
        }
    }

    internal fun updateLinkData(size: Int, offset: Int) {
        linkSize = size
        linkOffset = offset
    }

    override fun toString(): String {
        return buildString {
            appendLine("DexHeader[")
            appendLine("  magic           : ${magic.contentToHexString()}")
            appendLine("  checksum        : ${toHexStringWithPrefix(checksum.toLong())}")
            appendLine("  signature       : ${signature.contentToHexString()}")
            appendLine("  fileSize        : $fileSize")
            appendLine("  headerSize      : $headerSize")
            appendLine("  endianTag       : ${toHexStringWithPrefix(endianTag)}")
            appendLine("  linkSize        : $linkSize")
            appendLine("  linkOffset      : $linkOffset")
            appendLine("  mapOffset       : $mapOffset")
            appendLine("  stringIDsSize   : $stringIDsSize")
            appendLine("  stringIDsOffsets: $stringIDsOffsets")
            appendLine("  typeIDsSize     : $typeIDsSize")
            appendLine("  typeIDsOffset   : $typeIDsOffset")
            appendLine("  protoIDsSize    : $protoIDsSize")
            appendLine("  protoIDsOffset  : $protoIDsOffset")
            appendLine("  fieldIDsSize    : $fieldIDsSize")
            appendLine("  fieldIDsOffset  : $fieldIDsOffset")
            appendLine("  methodIDsSize   : $methodIDsSize")
            appendLine("  methodIDsOffset : $methodIDsOffset")
            appendLine("  classDefsSize   : $classDefsSize")
            appendLine("  classDefsOffset : $classDefsOffset")
            appendLine("  dataSize        : $dataSize")
            appendLine("  dataOffset      : $dataOffset]")
        }
    }

    companion object {
        internal fun empty(): DexHeader {
            return DexHeader()
        }

        internal fun of(format: DexFormat): DexHeader {
            return DexHeader(format)
        }

        internal fun read(input: DexDataInput): DexHeader {
            val header = DexHeader()
            header.read(input)
            return header
        }
    }
}