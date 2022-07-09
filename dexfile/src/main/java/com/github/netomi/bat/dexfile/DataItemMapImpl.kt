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

import com.github.netomi.bat.dexfile.annotation.*
import com.github.netomi.bat.dexfile.annotation.Annotation
import com.github.netomi.bat.dexfile.debug.DebugInfo
import com.github.netomi.bat.dexfile.io.DexDataOutput
import com.github.netomi.bat.dexfile.visitor.DataItemVisitor

internal class DataItemMapImpl : DataItem.Map {
    private val dataItemMap: MutableMap<Int, MutableSet<DataItem>> = mutableMapOf()
    private val offsetMap:   MutableMap<DataItem, Int>             = mutableMapOf()

    fun collectDataItems(dexFile: DexFile) {
        dexFile.dataItemsAccept(DataSectionItemCollector(this))
    }

    fun writeDataItems(mapList: MapList, output: DexDataOutput) {
        writeDataItems(mapList, output, DexConstants.TYPE_TYPE_LIST)
        writeDataItems(mapList, output, DexConstants.TYPE_ANNOTATION_SET_REF_LIST)
        writeDataItems(mapList, output, DexConstants.TYPE_ANNOTATION_SET_ITEM)
        // write the code items before the class data as it references its offset
        // using uleb128 encoding which is a variable length encoding. If the code
        // items were written afterwards, its exact offset would not be known
        // and thus the size of the class data items may vary.
        writeDataItems(mapList, output, DexConstants.TYPE_CODE_ITEM)
        writeDataItems(mapList, output, DexConstants.TYPE_CLASS_DATA_ITEM)
        writeDataItems(mapList, output, DexConstants.TYPE_STRING_DATA_ITEM)
        writeDataItems(mapList, output, DexConstants.TYPE_DEBUG_INFO_ITEM)
        writeDataItems(mapList, output, DexConstants.TYPE_ANNOTATION_ITEM)
        writeDataItems(mapList, output, DexConstants.TYPE_ENCODED_ARRAY_ITEM)
        writeDataItems(mapList, output, DexConstants.TYPE_ANNOTATIONS_DIRECTORY_ITEM)
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

private class DataSectionItemCollector(private val dataItemMap: DataItemMapImpl) : DataItemVisitor {

    override fun visitAnyDataItem(dexFile: DexFile, dataItem: DataItem) {}

    override fun visitStringData(dexFile: DexFile, stringID: StringID, stringData: StringData) {
        dataItemMap.addDataItem(stringData)
    }

    override fun visitParameterTypes(dexFile: DexFile, protoID: ProtoID, typeList: TypeList) {
        dataItemMap.addDataItem(typeList)
    }

    override fun visitClassData(dexFile: DexFile, classDef: ClassDef, classData: ClassData) {
        dataItemMap.addDataItem(classData)
    }

    override fun visitInterfaceTypes(dexFile: DexFile, classDef: ClassDef, typeList: TypeList) {
        dataItemMap.addDataItem(typeList)
    }

    override fun visitStaticValuesArray(dexFile: DexFile, classDef: ClassDef, encodedArray: EncodedArray) {
        dataItemMap.addDataItem(encodedArray)
    }

    override fun visitAnnotationsDirectory(dexFile: DexFile, classDef: ClassDef, annotationsDirectory: AnnotationsDirectory) {
        dataItemMap.addDataItem(annotationsDirectory)
    }

    override fun visitClassAnnotations(dexFile: DexFile, annotationsDirectory: AnnotationsDirectory, annotationSet: AnnotationSet) {
        dataItemMap.addDataItem(annotationSet)
    }

    override fun visitFieldAnnotations(dexFile: DexFile, fieldAnnotation: FieldAnnotation, annotationSet: AnnotationSet) {
        dataItemMap.addDataItem(annotationSet)
    }

    override fun visitMethodAnnotations(dexFile: DexFile, methodAnnotation: MethodAnnotation, annotationSet: AnnotationSet) {
        dataItemMap.addDataItem(annotationSet)
    }

    override fun visitParameterAnnotations(dexFile: DexFile, parameterAnnotation: ParameterAnnotation, annotationSetRefList: AnnotationSetRefList) {
        dataItemMap.addDataItem(annotationSetRefList)
    }

    override fun visitAnnotationSet(dexFile: DexFile, annotationSetRef: AnnotationSetRef, annotationSet: AnnotationSet) {
        dataItemMap.addDataItem(annotationSet)
    }

    override fun visitAnnotation(dexFile: DexFile, annotationSet: AnnotationSet, index: Int, annotation: Annotation) {
        dataItemMap.addDataItem(annotation)
    }

    override fun visitCode(dexFile: DexFile, encodedMethod: EncodedMethod, code: Code) {
        dataItemMap.addDataItem(code)
    }

    override fun visitDebugInfo(dexFile: DexFile, code: Code, debugInfo: DebugInfo) {
        dataItemMap.addDataItem(debugInfo)
    }

    override fun visitCallSite(dexFile: DexFile, callSiteID: CallSiteID, callSite: CallSite) {
        dataItemMap.addDataItem(callSite)
    }
}