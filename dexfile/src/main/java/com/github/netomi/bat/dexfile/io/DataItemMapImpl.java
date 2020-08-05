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

import com.github.netomi.bat.dexfile.DataItem;
import com.github.netomi.bat.dexfile.DexConstants;
import com.github.netomi.bat.dexfile.DexFile;
import com.github.netomi.bat.dexfile.MapList;
import com.github.netomi.bat.dexfile.visitor.DataItemVisitor;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class DataItemMapImpl
implements   DataItem.Map
{
    private final Map<Integer, Set<DataItem>> dataItemMap = new HashMap<>();
    private final Map<DataItem, Integer>      offsetMap   = new HashMap<>();

    public void collectDataItems(DexFile dexFile) {
        dexFile.dataItemsAccept(new DataSectionItemCollector(this));
    }

    public void writeDataItems(MapList mapList, DexDataOutput output) {
        writeDataItems(mapList, output, DexConstants.TYPE_TYPE_LIST);
        writeDataItems(mapList, output, DexConstants.TYPE_ANNOTATION_SET_REF_LIST);
        writeDataItems(mapList, output, DexConstants.TYPE_ANNOTATION_SET_ITEM);
        writeDataItems(mapList, output, DexConstants.TYPE_CLASS_DATA_ITEM);
        writeDataItems(mapList, output, DexConstants.TYPE_CODE_ITEM);
        writeDataItems(mapList, output, DexConstants.TYPE_STRING_DATA_ITEM);
        writeDataItems(mapList, output, DexConstants.TYPE_DEBUG_INFO_ITEM);
        writeDataItems(mapList, output, DexConstants.TYPE_ANNOTATION_ITEM);
        writeDataItems(mapList, output, DexConstants.TYPE_ENCODED_ARRAY_ITEM);
        writeDataItems(mapList, output, DexConstants.TYPE_ANNOTATIONS_DIRECTORY_ITEM);
    }

    public void updateOffsets(DexFile dexFile) {
        dexFile.dataItemsAccept(new DataItemVisitor() {
            @Override
            public void visitAnyDataItem(DexFile dexFile, DataItem dataItem) {
            dataItem.updateOffsets(DataItemMapImpl.this);
            }
        });
    }

    void addDataItem(DataItem dataItem) {
        Set<DataItem> dataItemSet = getDataItems(dataItem.getItemType());
        dataItemSet.add(dataItem);
    }

    Set<DataItem> getDataItems(int type) {
        Set<DataItem> dataItemSet = dataItemMap.getOrDefault(type, new LinkedHashSet<>());
        dataItemMap.putIfAbsent(type, dataItemSet);
        return dataItemSet;
    }

    private void writeDataItems(MapList mapList, DexDataOutput output, int type) {
        Set<DataItem> dataItemSet = dataItemMap.get(type);
        if (dataItemSet != null && !dataItemSet.isEmpty()) {
            int align = dataItemSet.iterator().next().getDataAlignment();
            output.writeAlignmentPadding(align);
            mapList.updateMapItem(type, dataItemSet.size(), output.getOffset());
            for (DataItem dataItem : dataItemSet) {
                int dataItemOffset = output.getOffset();
                dataItem.write(output);

                offsetMap.put(dataItem, dataItemOffset);
            }
        }
    }

    public int getOffset(DataItem dataItem) {
        Integer offset = offsetMap.get(dataItem);
        return offset != null ? offset.intValue() : 0;
    }
}
