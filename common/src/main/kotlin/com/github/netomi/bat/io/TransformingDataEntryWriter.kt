/*
 *  Copyright (c) 2020-2022 Thomas Neidhart.
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

package com.github.netomi.bat.io

import java.io.OutputStream

fun transformOutputDataEntriesWith(transform: (String) -> String, delegateWriter: DataEntryWriter): DataEntryWriter {
    return TransformingDataEntryWriter(transform, delegateWriter)
}

private class TransformingDataEntryWriter constructor(private val transform: (String) -> String,
                                                      private val writer:    DataEntryWriter): DataEntryWriter {

    override fun createOutputStream(entry: DataEntry): OutputStream {
        return writer.createOutputStream(TransformedDataEntry.of(transform(entry.name), entry))
    }

    override fun close() {
        writer.close()
    }
}