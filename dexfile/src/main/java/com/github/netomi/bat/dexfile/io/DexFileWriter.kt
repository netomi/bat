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
import com.github.netomi.bat.dexfile.annotation.*
import com.github.netomi.bat.dexfile.annotation.Annotation
import com.github.netomi.bat.dexfile.debug.DebugInfo
import com.github.netomi.bat.dexfile.editor.DexSorter
import com.github.netomi.bat.dexfile.visitor.ClassDefVisitor
import com.github.netomi.bat.dexfile.visitor.DataItemVisitor
import com.github.netomi.bat.dexfile.visitor.DexFileVisitor
import com.github.netomi.bat.dexfile.visitor.IDAccessor
import com.github.netomi.bat.dexfile.visitor.ReferencedIDVisitor
import com.google.common.hash.Hashing
import java.io.OutputStream

class DexFileWriter(private val outputStream: OutputStream) : DexFileVisitor {

    private lateinit var dataItemMap: DataItemMapImpl

    private var dataOffset = 0
    private var dataSize   = 0

    @Suppress("UnstableApiUsage")
    override fun visitDexFile(dexFile: DexFile) {
        dataItemMap = DataItemMapImpl()
        dataItemMap.collectDataItems(dexFile)

        dataOffset = 0
        dataSize   = 0

        DexSorter().visitDexFile(dexFile)

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
        mapList.updateMapItem(TYPE_HEADER_ITEM, 1, output.offset)
        header.write(output)
    }

    private fun writeStringIDs(dexFile: DexFile, header: DexHeader, mapList: MapList, output: DexDataOutput) {
        writePadding(StringID::class.java, output)
        header.updateDataItem(TYPE_STRING_ID_ITEM, dexFile.stringIDCount, output.offset)
        mapList.updateMapItem(TYPE_STRING_ID_ITEM, dexFile.stringIDCount, output.offset)
        for (stringID in dexFile.getStringIDs()) {
            stringID.write(output)
        }
    }

    private fun writeTypeIDs(dexFile: DexFile, header: DexHeader, mapList: MapList, output: DexDataOutput) {
        writePadding(TypeID::class.java, output)
        header.updateDataItem(TYPE_TYPE_ID_ITEM, dexFile.typeIDCount, output.offset)
        mapList.updateMapItem(TYPE_TYPE_ID_ITEM, dexFile.typeIDCount, output.offset)
        for (typeID in dexFile.getTypeIDs()) {
           typeID.write(output)
        }
    }

    private fun writeProtoIDs(dexFile: DexFile, header: DexHeader, mapList: MapList, output: DexDataOutput) {
        writePadding(ProtoID::class.java, output)
        header.updateDataItem(TYPE_PROTO_ID_ITEM, dexFile.protoIDCount, output.offset)
        mapList.updateMapItem(TYPE_PROTO_ID_ITEM, dexFile.protoIDCount, output.offset)
        for (protoID in dexFile.getProtoIDs()) {
            protoID.write(output)
        }
    }

    private fun writeFieldIDs(dexFile: DexFile, header: DexHeader, mapList: MapList, output: DexDataOutput) {
        writePadding(FieldID::class.java, output)
        header.updateDataItem(TYPE_FIELD_ID_ITEM, dexFile.fieldIDCount, output.offset)
        mapList.updateMapItem(TYPE_FIELD_ID_ITEM, dexFile.fieldIDCount, output.offset)
        for (fieldID in dexFile.getFieldIDs()) {
            fieldID.write(output)
        }
    }

    private fun writeMethodIDs(dexFile: DexFile, header: DexHeader, mapList: MapList, output: DexDataOutput) {
        writePadding(MethodID::class.java, output)
        header.updateDataItem(TYPE_METHOD_ID_ITEM, dexFile.methodIDCount, output.offset)
        mapList.updateMapItem(TYPE_METHOD_ID_ITEM, dexFile.methodIDCount, output.offset)
        for (methodID in dexFile.getMethodIDs()) {
            methodID.write(output)
        }
    }

    private fun writeClassDefs(dexFile: DexFile, header: DexHeader, mapList: MapList, output: DexDataOutput) {
        writePadding(ClassDef::class.java, output)
        header.updateDataItem(TYPE_CLASS_DEF_ITEM, dexFile.classDefCount, output.offset)
        mapList.updateMapItem(TYPE_CLASS_DEF_ITEM, dexFile.classDefCount, output.offset)
        for (classDef in dexFile.getClassDefs()) {
            classDef.write(output)
        }
    }

    private fun writeCallSiteIDs(dexFile: DexFile, mapList: MapList, output: DexDataOutput) {
        if (dexFile.callSiteIDCount > 0) {
            writePadding(CallSiteID::class.java, output)
            mapList.updateMapItem(TYPE_CALL_SITE_ID_ITEM, dexFile.callSiteIDCount, output.offset)
            for (callSiteID in dexFile.getCallSiteIDs()) {
                callSiteID.write(output)
            }
        }
    }

    private fun writeMethodHandles(dexFile: DexFile, mapList: MapList, output: DexDataOutput) {
        if (dexFile.methodHandleCount > 0) {
            writePadding(MethodHandle::class.java, output)
            mapList.updateMapItem(TYPE_METHOD_HANDLE_ITEM, dexFile.methodHandleCount, output.offset)
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
        header.updateDataItem(TYPE_MAP_LIST, 0, output.offset)
        mapList.updateMapItem(TYPE_MAP_LIST, 1, output.offset)
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

private class DataItemMapImpl : DataItem.Map {
    private val dataItemMap: MutableMap<Int, MutableSet<DataItem>> = mutableMapOf()
    private val offsetMap:   MutableMap<DataItem, Int>             = mutableMapOf()

    fun collectDataItems(dexFile: DexFile) {
        // collect all non-empty data items
        dexFile.dataItemsAccept(DataSectionItemCollector({ dataItem -> !dataItem.isEmpty }, this))
    }

    fun writeDataItems(mapList: MapList, output: DexDataOutput) {
        writeDataItems(mapList, output, TYPE_TYPE_LIST)
        writeDataItems(mapList, output, TYPE_ANNOTATION_SET_REF_LIST)
        writeDataItems(mapList, output, TYPE_ANNOTATION_SET_ITEM)
        // write the code items before the class data as it references its offset
        // using uleb128 encoding which is a variable length encoding. If the code
        // items were written afterwards, its exact offset would not be known
        // and thus the size of the class data items may vary.
        writeDataItems(mapList, output, TYPE_CODE_ITEM)
        writeDataItems(mapList, output, TYPE_CLASS_DATA_ITEM)
        writeDataItems(mapList, output, TYPE_STRING_DATA_ITEM)
        writeDataItems(mapList, output, TYPE_DEBUG_INFO_ITEM)
        writeDataItems(mapList, output, TYPE_ANNOTATION_ITEM)
        writeDataItems(mapList, output, TYPE_ENCODED_ARRAY_ITEM)
        writeDataItems(mapList, output, TYPE_ANNOTATIONS_DIRECTORY_ITEM)
    }

    fun updateOffsets(dexFile: DexFile) {
        dexFile.dataItemsAccept(object : DataItemVisitor {
            override fun visitAnyDataItem(dexFile: DexFile, dataItem: DataItem) {
                dataItem.updateOffsets(this@DataItemMapImpl)
            }
        })
    }

    fun addDataItem(dataItem: DataItem) {
        val dataItemSet = getDataItems(dataItem.itemType)
        dataItemSet.add(dataItem)
    }

    fun getDataItems(type: Int): MutableSet<DataItem> {
        val dataItemSet = dataItemMap.getOrDefault(type, LinkedHashSet())
        dataItemMap.putIfAbsent(type, dataItemSet)
        return dataItemSet
    }

    private fun writeDataItems(mapList: MapList, output: DexDataOutput, type: Int) {
        val dataItemSet: Set<DataItem>? = dataItemMap[type]
        if (dataItemSet != null && dataItemSet.isNotEmpty()) {
            val align = dataItemSet.iterator().next().dataAlignment
            output.writeAlignmentPadding(align)
            mapList.updateMapItem(type, dataItemSet.size, output.offset)
            for (dataItem in dataItemSet) {
                val dataItemOffset = output.offset
                dataItem.updateOffsets(this)
                dataItem.write(output)
                offsetMap[dataItem] = dataItemOffset
            }
        }
    }

    override fun getOffset(item: DataItem?): Int {
        val offset = offsetMap[item]
        return offset ?: 0
    }
}

private class DataSectionItemCollector(private val predicate: (DataItem) -> Boolean,
                                       private val dataItemMap: DataItemMapImpl) : DataItemVisitor {

    // ignore other data items
    override fun visitAnyDataItem(dexFile: DexFile, dataItem: DataItem) {}

    private fun visitDataItem(dataItem: DataItem) {
        if (predicate.invoke(dataItem)) {
            dataItemMap.addDataItem(dataItem)
        }
    }

    override fun visitStringData(dexFile: DexFile, stringID: StringID, stringData: StringData) {
        visitDataItem(stringData)
    }

    override fun visitParameterTypes(dexFile: DexFile, protoID: ProtoID, typeList: TypeList) {
        visitDataItem(typeList)
    }

    override fun visitClassData(dexFile: DexFile, classDef: ClassDef, classData: ClassData) {
        visitDataItem(classData)
    }

    override fun visitInterfaceTypes(dexFile: DexFile, classDef: ClassDef, typeList: TypeList) {
        visitDataItem(typeList)
    }

    override fun visitStaticValuesArray(dexFile: DexFile, classDef: ClassDef, encodedArray: EncodedArray) {
        visitDataItem(encodedArray)
    }

    override fun visitAnnotationsDirectory(dexFile: DexFile, classDef: ClassDef, annotationsDirectory: AnnotationsDirectory) {
        visitDataItem(annotationsDirectory)
    }

    override fun visitClassAnnotations(dexFile: DexFile, annotationsDirectory: AnnotationsDirectory, annotationSet: AnnotationSet) {
        visitDataItem(annotationSet)
    }

    override fun visitFieldAnnotations(dexFile: DexFile, fieldAnnotation: FieldAnnotation, annotationSet: AnnotationSet) {
        visitDataItem(annotationSet)
    }

    override fun visitMethodAnnotations(dexFile: DexFile, methodAnnotation: MethodAnnotation, annotationSet: AnnotationSet) {
        visitDataItem(annotationSet)
    }

    override fun visitParameterAnnotations(dexFile: DexFile, parameterAnnotation: ParameterAnnotation, annotationSetRefList: AnnotationSetRefList) {
        visitDataItem(annotationSetRefList)
    }

    override fun visitAnnotationSet(dexFile: DexFile, annotationSetRef: AnnotationSetRef, annotationSet: AnnotationSet) {
        visitDataItem(annotationSet)
    }

    override fun visitAnnotation(dexFile: DexFile, annotationSet: AnnotationSet, index: Int, annotation: Annotation) {
        visitDataItem(annotation)
    }

    override fun visitCode(dexFile: DexFile, encodedMethod: EncodedMethod, code: Code) {
        visitDataItem(code)
    }

    override fun visitDebugInfo(dexFile: DexFile, code: Code, debugInfo: DebugInfo) {
        visitDataItem(debugInfo)
    }

    override fun visitCallSite(dexFile: DexFile, callSiteID: CallSiteID, callSite: CallSite) {
        visitDataItem(callSite)
    }
}