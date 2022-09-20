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

import java.io.FilterInputStream
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class ZipDataEntry private constructor( private val zipInputStream: ZipInputStream,
                                        private val zipEntry:       ZipEntry,
                                       override val parent:         DataEntry): DataEntry {

    override val name: String
        get() = zipEntry.name

    override val fullName: String
        get() = "${parent.fullName}!$name"

    override fun getInputStream(): InputStream {
        return WrappedZipInputStream(zipInputStream)
    }

    override fun toString(): String {
        return "ZipDataEntry[name=$fullName]"
    }

    private class WrappedZipInputStream constructor(private val inputStream: ZipInputStream): FilterInputStream(inputStream) {
        override fun close() {
            inputStream.closeEntry()
        }
    }

    companion object {
        fun of(zipInputStream: ZipInputStream, zipEntry: ZipEntry, parent: DataEntry): ZipDataEntry {
            return ZipDataEntry(zipInputStream, zipEntry, parent)
        }
    }
}