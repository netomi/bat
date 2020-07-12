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
import com.github.netomi.bat.dexfile.visitor.DataItemVisitor;

/**
 * The common base interface for any structure contained
 * in a dex file.
 *
 * @author Thomas Neidhart
 */
public interface DexContent
{
    /**
     * De-serializes this structure from the given input stream.
     *
     * @param input the input stream to read from.
     */
    void read(DexDataInput input);

    /**
     * Serializes this structure to the given output stream.
     *
     * @param output the output stream to write to.
     */
    void write(DexDataOutput output);

    default void readLinkedDataItems(DexDataInput input) {}

    default void updateOffsets(DataItem.Map dataItemMap) {}

    default void dataItemsAccept(DexFile dexFile, DataItemVisitor visitor) {}
}
