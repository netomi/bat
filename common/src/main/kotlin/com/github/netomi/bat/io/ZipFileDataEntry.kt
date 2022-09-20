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

import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

class ZipFileDataEntry private constructor( private val zipFile:  ZipFile,
                                            private val zipEntry: ZipEntry,
                                           override val parent:   DataEntry): DataEntry {

    override val name: String
        get() = zipEntry.name


    override val fullName: String
        get() = "${parent.fullName}!$name"

    override fun getInputStream(): InputStream {
        return zipFile.getInputStream(zipEntry)
    }

    override fun toString(): String {
        return "ZipFileDataEntry[name=$fullName]"
    }

    companion object {
        fun of(zipFile: ZipFile, zipEntry: ZipEntry, parent: DataEntry): ZipFileDataEntry {
            return ZipFileDataEntry(zipFile, zipEntry, parent)
        }
    }
}