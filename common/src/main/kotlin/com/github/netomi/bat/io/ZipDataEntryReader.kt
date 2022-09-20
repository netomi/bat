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

import com.github.netomi.bat.util.fileNameMatcher
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream

fun unwrapArchives(reader: DataEntryReader): DataEntryReader {
    return filterDataEntriesBy(fileNameMatcher("**.zip,**.jar,**.jmod"), ZipDataEntryReader(reader), reader)
}

class ZipDataEntryReader constructor(private val delegateReader: DataEntryReader): DataEntryReader {

    private val jmodReader = transformDataEntriesWith({ it.removePrefix("classes/") }, delegateReader)

    private fun readEntry(entry: DataEntry, isJmod: Boolean) {
        if (isJmod) {
            jmodReader.read(entry)
        } else {
            delegateReader.read(entry)
        }
    }

    override fun read(entry: DataEntry) {
        val isJmod = entry.name.endsWith(".jmod")

        if (entry is FileDataEntry) {
            ZipFile(entry.path.toFile()).use { zipFile ->
                zipFile.entries()
                    .asSequence()
                    .forEach { zipEntry -> readEntry(ZipFileDataEntry.of(zipFile, zipEntry, entry), isJmod) }
            }
        } else {
            ZipInputStream(entry.getInputStream()).use { zis ->
                // if it's a jmod file, eat the magic bytes at the start
                // ZipFile already handles this gracefully.
                if (isJmod) {
                    zis.skip(4)
                }

                generateSequence { zis.nextEntry }
                    .forEach { zipEntry -> readEntry(ZipDataEntry.of(zis, zipEntry, entry), isJmod) }
            }
        }
    }
}