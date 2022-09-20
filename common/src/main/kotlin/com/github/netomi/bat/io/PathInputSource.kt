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

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.attribute.BasicFileAttributes
import java.util.function.BiPredicate
import kotlin.io.path.*

class PathInputSource private constructor(val path: Path, private val sorted: Boolean): InputSource {

    override fun pumpDataEntries(reader: DataEntryReader) {
        if (path.isRegularFile()) {
            reader.read(FileDataEntry.of(path))
        } else {
            val baseDir = path
            val processEntry = { path: Path -> reader.read(FileDataEntry.of(baseDir, path.relativeTo(baseDir))) }

            val inputFiles = Files.find(baseDir, Int.MAX_VALUE, REGULAR_FILE)
            inputFiles.use {
                if (sorted) {
                    it.sorted()
                      .forEach(processEntry)
                } else {
                    it.forEach(processEntry)
                }
            }
        }
    }

    override fun toString(): String {
        return "PathInputSource[path=$path,sorted=$sorted]"
    }

    companion object {
        private val REGULAR_FILE = BiPredicate { _: Path, attr: BasicFileAttributes -> attr.isRegularFile }

        fun of(path: String, sorted: Boolean = false): PathInputSource {
            return of(Paths.get(path), sorted)
        }

        fun of(path: Path, sorted: Boolean = false): PathInputSource {
            return PathInputSource(path, sorted)
        }
    }
}
