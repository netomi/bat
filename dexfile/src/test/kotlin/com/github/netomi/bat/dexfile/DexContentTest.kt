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

import com.github.netomi.bat.dexfile.io.ByteBufferBackedDexDataOutput
import com.github.netomi.bat.dexfile.io.DexDataInput
import com.github.netomi.bat.dexfile.io.DexDataOutput
import com.github.netomi.bat.dexfile.visitor.DataItemVisitor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.IOException
import java.util.function.Consumer
import java.util.function.Function

abstract class DexContentTest<T : DexContent> {

    protected abstract val testInstances: Array<T>
    protected abstract val factoryMethod: Function<DexDataInput, T>

    open fun getWriteMethod(data: T): Consumer<DexDataOutput> {
        return Consumer { output -> data.write(output) }
    }

    open fun getReadLinkedMethod(data: T, oldData: T): Consumer<DexDataInput> {
        return Consumer { input -> data.readLinkedDataItems(input) }
    }

    @Test
    fun readWrite() {
        val testData = testInstances
        for (data in testData) {
            serializeAndDeserialize(data)
        }
    }

    private fun serializeAndDeserialize(data: T) {
        try {
            val dataItemMap = MyDataItemMap()
            val output = ByteBufferBackedDexDataOutput(8096)

            // write a dummy byte to avoid any data item starting at offset 0.
            output.writeByte(0x0.toByte())
            val dexFile = DexFile.of(DexFormat.FORMAT_035)

            // collect all linked data items and serialize them first.
            data.dataItemsAccept(dexFile, object : DataItemVisitor {
                override fun visitAnyDataItem(dexFile: DexFile, dataItem: DataItem) {
                    dataItemMap.setOffset(dataItem, output.offset)
                    dataItem.write(output)
                }
            })

            // remember the offset of the actual item to be written.
            val startOffset: Int = output.offset
            data.updateOffsets(dataItemMap)
            getWriteMethod(data).accept(output)
            val buffer = output.toArray()
            val input = DexDataInput(ByteArrayInputStream(buffer))

            // read the item from the previously stored offset.
            input.offset = startOffset
            val result = factoryMethod.apply(input)
            // read all linked data items.
            getReadLinkedMethod(result, data).accept(input)
            assertEquals(data, result)
        } catch (ioe: IOException) {
            fail<Any>(ioe)
        }
    }

    private class MyDataItemMap : DataItem.Map {
        private val offsetMap: MutableMap<DataItem, Int> = HashMap()
        fun setOffset(item: DataItem, offset: Int) {
            offsetMap[item] = offset
        }

        override fun getOffset(item: DataItem?): Int {
            return offsetMap[item] ?: 0
        }
    }
}