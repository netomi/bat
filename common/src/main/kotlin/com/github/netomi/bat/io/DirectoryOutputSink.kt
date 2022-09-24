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
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.outputStream

class DirectoryOutputSink private constructor(private val baseDir: Path): OutputSink {
    override fun createOutputStream(entry: DataEntry): OutputStream {
        val elementPath = Path.of(entry.name)

        val outputPath = baseDir.resolve(elementPath)

        val outputParentPath = outputPath.parent
        if (outputParentPath != null && !outputParentPath.exists()) {
            outputParentPath.createDirectories()
        }

        return outputPath.outputStream()
    }

    companion object {
        fun of(baseDir: Path): DirectoryOutputSink {
            return DirectoryOutputSink(baseDir)
        }
    }
}