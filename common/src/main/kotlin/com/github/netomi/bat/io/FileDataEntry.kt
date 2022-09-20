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
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.inputStream
import kotlin.io.path.pathString
import kotlin.io.path.relativeTo

class FileDataEntry private constructor(private val baseDir:      Path,
                                        private val relativePath: Path): DataEntry {

    override val name: String
        get() = relativePath.pathString

    override val fullName: String
        get() = path.pathString

    override val parent: DataEntry?
        get() = null

    val path: Path
        get() = baseDir.resolve(relativePath)

    override fun getInputStream(): InputStream {
        return path.inputStream()
    }

    override fun toString(): String {
        return "FileDataEntry[name=$name]"
    }

    companion object {
        fun of(path: Path): FileDataEntry {
            val baseDir = path.parent ?: Paths.get("")
            return FileDataEntry(baseDir, path.relativeTo(baseDir))
        }

        fun of(baseDir: Path, path: Path): FileDataEntry {
            require(!path.isAbsolute)
            return FileDataEntry(baseDir, path)
        }
    }
}