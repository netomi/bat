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
import java.nio.file.attribute.FileTime

class TransformedDataEntry
    private constructor(private val newName:       String,
                        private val originalEntry: DataEntry): DataEntry {

    override val name: String
        get() = newName

    override val fullName: String
        get() = "${originalEntry.fullName}->${name}"

    override val parent: DataEntry?
        get() = originalEntry.parent

    override fun getInputStream(): InputStream {
        return originalEntry.getInputStream()
    }

    override val lastModifiedTime: FileTime
        get() = originalEntry.lastModifiedTime

    override val size: Long
        get() = originalEntry.size

    companion object {
        fun of(transformedName: String, originalEntry: DataEntry): TransformedDataEntry {
            return TransformedDataEntry(transformedName, originalEntry)
        }
    }
}