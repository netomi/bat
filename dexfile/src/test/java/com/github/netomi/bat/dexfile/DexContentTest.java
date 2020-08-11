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

import com.github.netomi.bat.dexfile.io.ByteBufferBackedDexDataOutput;
import com.github.netomi.bat.dexfile.io.DexDataInput;
import com.github.netomi.bat.dexfile.visitor.DataItemVisitor;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public abstract class DexContentTest<T extends DexContent>
{
    public abstract T[] getTestInstances();

    public abstract Function<DexDataInput, T> getFactoryMethod();

    @Test
    public void readWrite() {
        T[] testData = getTestInstances();

        for (T data : testData) {
            serializeAndDeserialize(data);
        }
    }

    private void serializeAndDeserialize(T data) {
        try {
            MyDataItemMap dataItemMap            = new MyDataItemMap();
            ByteBufferBackedDexDataOutput output = new ByteBufferBackedDexDataOutput(8096);

            // write a dummy byte to avoid any data item starting at offset 0.
            output.writeByte((byte) 0x0);

            // collect all linked data items and serialize them first.
            data.dataItemsAccept(null, new DataItemVisitor() {
                @Override
                public void visitAnyDataItem(DexFile dexFile, DataItem dataItem) {
                    dataItemMap.setOffset(dataItem, output.getOffset());
                    dataItem.write(output);
                }
            });

            // remember the offset of the actual item to be written.
            int startOffset = output.getOffset();
            data.updateOffsets(dataItemMap);
            data.write(output);

            byte[] buffer = output.toArray();

            DexDataInput input = new DexDataInput(new ByteArrayInputStream(buffer));

            // read the item from the previously stored offset.
            input.setOffset(startOffset);
            T result = getFactoryMethod().apply(input);
            // read all linked data items.
            result.readLinkedDataItems(input);

            assertEquals(data, result);
        } catch (IOException ioe) {
            fail(ioe);
        }
    }

    private static class MyDataItemMap
    implements DataItem.Map
    {
        private final Map<DataItem, Integer> offsetMap = new HashMap<>();

        public void setOffset(DataItem item, int offset) {
            offsetMap.put(item, offset);
        }

        @Override
        public int getOffset(DataItem item) {
            return offsetMap.get(item);
        }
    }
}
