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
package com.github.netomi.bat.io

import java.io.IOException
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists

class FileOutputStreamFactory(private val baseDir:         Path,
                              private val extension:       String,
                              private val pathTransformer: (String) -> Path = { Path.of(it) }) : OutputStreamFactory {

    @Throws(IOException::class)
    override fun createOutputStream(element: String): OutputStream {
        val elementPath = pathTransformer(element)

        var currentPath = baseDir

        if (elementPath.parent != null) {
            currentPath = baseDir.resolve(elementPath.parent)
        }

        if (!currentPath.exists()) {
            Files.createDirectories(currentPath)
        }

        currentPath = currentPath.resolve("${elementPath.fileName}.$extension")
        return Files.newOutputStream(currentPath)
    }
}
