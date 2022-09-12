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

import com.github.netomi.bat.dexfile.io.DexDataInput
import com.github.netomi.bat.dexfile.io.DexDataOutput
import com.github.netomi.bat.dexfile.visitor.DataItemVisitor

/**
 * The common base interface for any structure contained in a dex file.
 */
abstract class DexContent {
    /**
     * De-serializes this structure from the given input stream.
     *
     * @param input the input stream to read from.
     */
    protected abstract fun read(input: DexDataInput)

    /**
     * Serializes this structure to the given output stream.
     *
     * @param output the output stream to write to.
     */
    internal abstract fun write(output: DexDataOutput)

    internal open fun readLinkedDataItems(input: DexDataInput) {}

    internal open fun updateOffsets(dataItemMap: DataItem.Map) {}

    /**
     * Applies the given [DataItemVisitor] to all [DataItem]s
     * contained in this [DexContent].
     *
     * @param dexFile the [DexFile] associated with this instance.
     * @param visitor the visitor to apply.
     */
    internal open fun dataItemsAccept(dexFile: DexFile, visitor: DataItemVisitor) {}
}