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
package com.github.netomi.bat.dexfile.io

import com.github.netomi.bat.dexfile.*
import com.github.netomi.bat.dexfile.DataItemMapImpl
import com.github.netomi.bat.dexfile.visitor.DexFileVisitor
import com.google.common.hash.Hashing
import java.io.OutputStream

class DexFileWriter(private val outputStream: OutputStream) : DexFileVisitor {

    private lateinit var dataItemMap: DataItemMapImpl

    private var dataOffset = 0
    private var dataSize   = 0

    override fun visitDexFile(dexFile: DexFile) {
        dataItemMap = DataItemMapImpl()
        dataItemMap.collectDataItems(dexFile)

        dataOffset = 0
        dataSize   = 0

        assert(dexFile.dexFormat != null)
        val header = DexHeader.of(dexFile.dexFormat!!)

        // In the first pass we update all the offsets.
        val countingOutput: DexDataOutput = CountingDexDataOutput()
        writeDexFile(dexFile, header, countingOutput)

        // update data that we can already determine after first pass.
        val size = countingOutput.offset
        header.fileSize   = size.toLong()
        header.dataOffset = dataOffset
        header.dataSize   = dataSize

        val output = ByteBufferBackedDexDataOutput(size)

        // second pass, writing the actual bytes
        writeDexFile(dexFile, header, output)

        // after the second pass, update the checksum and signature fields
        val byteBuffer = output.byteBuffer

        @Suppress("DEPRECATION")
        val sha1Signature = Hashing.sha1().newHasher()
        byteBuffer.position(32)
        sha1Signature.putBytes(byteBuffer)
        header.signature = sha1Signature.hash().asBytes()

        val adlerChecksum = Hashing.adler32().newHasher()
        byteBuffer.position(12)
        byteBuffer.put(header.signature)
        byteBuffer.position(12)
        adlerChecksum.putBytes(byteBuffer)
        header.checksum = adlerChecksum.hash().asInt().toLong().toUInt()
        byteBuffer.position(8)
        byteBuffer.putInt(header.checksum.toInt())

        output.copy(outputStream)
        outputStream.flush()
    }

    private fun writeDexFile(dexFile: DexFile, header: DexHeader, output: DexDataOutput): MapList {
        val mapList = MapList.empty()

        writeHeader(header, mapList, output)

        output.writePadding((header.headerSize - output.offset).toInt().coerceAtLeast(0))

        writeStringIDs(dexFile, header, mapList, output)
        writeTypeIDs(dexFile, header, mapList, output)
        writeProtoIDs(dexFile, header, mapList, output)
        writeFieldIDs(dexFile, header, mapList, output)
        writeMethodIDs(dexFile, header, mapList, output)
        writeClassDefs(dexFile, header, mapList, output)
        writeCallSiteIDs(dexFile, mapList, output)
        writeMethodHandles(dexFile, mapList, output)
        writeDataSection(dexFile, mapList, output)
        writeMapList(header, mapList, output)
        writeLinkData(dexFile, header, output)

        return mapList
    }

    private fun writePadding(clazz: Class<out DataItem>, output: DexDataOutput) {
        output.writeAlignmentPadding(clazz.getAnnotation(DataItemAnn::class.java).dataAlignment)
    }

    private fun writeHeader(header: DexHeader, mapList: MapList, output: DexDataOutput) {
        mapList.updateMapItem(DexConstants.TYPE_HEADER_ITEM, 1, output.offset)
        header.write(output)
    }

    private fun writeStringIDs(dexFile: DexFile, header: DexHeader, mapList: MapList, output: DexDataOutput) {
        writePadding(StringID::class.java, output)
        header.updateDataItem(DexConstants.TYPE_STRING_ID_ITEM, dexFile.stringIDCount, output.offset)
        mapList.updateMapItem(DexConstants.TYPE_STRING_ID_ITEM, dexFile.stringIDCount, output.offset)
        for (stringID in dexFile.getStringIDs()) {
            stringID.write(output)
        }
    }

    private fun writeTypeIDs(dexFile: DexFile, header: DexHeader, mapList: MapList, output: DexDataOutput) {
        writePadding(TypeID::class.java, output)
        header.updateDataItem(DexConstants.TYPE_TYPE_ID_ITEM, dexFile.typeIDCount, output.offset)
        mapList.updateMapItem(DexConstants.TYPE_TYPE_ID_ITEM, dexFile.typeIDCount, output.offset)
        for (typeID in dexFile.getTypeIDs()) {
           typeID.write(output)
        }
    }

    private fun writeProtoIDs(dexFile: DexFile, header: DexHeader, mapList: MapList, output: DexDataOutput) {
        writePadding(ProtoID::class.java, output)
        header.updateDataItem(DexConstants.TYPE_PROTO_ID_ITEM, dexFile.protoIDCount, output.offset)
        mapList.updateMapItem(DexConstants.TYPE_PROTO_ID_ITEM, dexFile.protoIDCount, output.offset)
        for (protoID in dexFile.getProtoIDs()) {
            protoID.write(output)
        }
    }

    private fun writeFieldIDs(dexFile: DexFile, header: DexHeader, mapList: MapList, output: DexDataOutput) {
        writePadding(FieldID::class.java, output)
        header.updateDataItem(DexConstants.TYPE_FIELD_ID_ITEM, dexFile.fieldIDCount, output.offset)
        mapList.updateMapItem(DexConstants.TYPE_FIELD_ID_ITEM, dexFile.fieldIDCount, output.offset)
        for (fieldID in dexFile.getFieldIDs()) {
            fieldID.write(output)
        }
    }

    private fun writeMethodIDs(dexFile: DexFile, header: DexHeader, mapList: MapList, output: DexDataOutput) {
        writePadding(MethodID::class.java, output)
        header.updateDataItem(DexConstants.TYPE_METHOD_ID_ITEM, dexFile.methodIDCount, output.offset)
        mapList.updateMapItem(DexConstants.TYPE_METHOD_ID_ITEM, dexFile.methodIDCount, output.offset)
        for (methodID in dexFile.getMethodIDs()) {
            methodID.write(output)
        }
    }

    private fun writeClassDefs(dexFile: DexFile, header: DexHeader, mapList: MapList, output: DexDataOutput) {
        writePadding(ClassDef::class.java, output)
        header.updateDataItem(DexConstants.TYPE_CLASS_DEF_ITEM, dexFile.classDefCount, output.offset)
        mapList.updateMapItem(DexConstants.TYPE_CLASS_DEF_ITEM, dexFile.classDefCount, output.offset)
        for (classDef in dexFile.getClassDefs()) {
            classDef.write(output)
        }
    }

    private fun writeCallSiteIDs(dexFile: DexFile, mapList: MapList, output: DexDataOutput) {
        if (dexFile.callSiteIDCount > 0) {
            writePadding(CallSiteID::class.java, output)
            mapList.updateMapItem(DexConstants.TYPE_CALL_SITE_ID_ITEM, dexFile.callSiteIDCount, output.offset)
            for (callSiteID in dexFile.getCallSiteIDs()) {
                callSiteID.write(output)
            }
        }
    }

    private fun writeMethodHandles(dexFile: DexFile, mapList: MapList, output: DexDataOutput) {
        if (dexFile.methodHandleCount > 0) {
            writePadding(MethodHandle::class.java, output)
            mapList.updateMapItem(DexConstants.TYPE_METHOD_HANDLE_ITEM, dexFile.methodHandleCount, output.offset)
            for (methodHandle in dexFile.getMethodHandles()) {
                methodHandle.write(output)
            }
        }
    }

    private fun writeDataSection(dexFile: DexFile, mapList: MapList, output: DexDataOutput) {
        dataOffset = output.offset

        // Collect all DataItems that reside in the data section.
        dataItemMap.writeDataItems(mapList, output)
        dataItemMap.updateOffsets(dexFile)
        dataSize = output.offset - dataOffset
    }

    private fun writeMapList(header: DexHeader, mapList: MapList, output: DexDataOutput) {
        writePadding(MapList::class.java, output)
        header.updateDataItem(DexConstants.TYPE_MAP_LIST, 0, output.offset)
        mapList.updateMapItem(DexConstants.TYPE_MAP_LIST, 1, output.offset)
        mapList.write(output)
    }

    private fun writeLinkData(dexFile: DexFile, header: DexHeader, output: DexDataOutput) {
        val linkData: ByteArray? = dexFile.linkData
        if (linkData != null) {
            header.updateLinkData(linkData.size, output.offset)
            output.writeBytes(linkData)
        } else {
            header.updateLinkData(0, 0)
        }
    }
}