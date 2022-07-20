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

import com.github.netomi.bat.util.Classes
import java.io.IOException
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists

class FileOutputStreamFactory(private val baseDir: Path, private val extension: String) : OutputStreamFactory {
    @Throws(IOException::class)
    override fun createOutputStream(internalClassName: String): OutputStream {
        val packageComponents =
            Classes.internalPackageNameFromInternalName(internalClassName)
                   .split("/".toRegex())
                   .dropLastWhile { it.isEmpty() }
                   .toTypedArray()

        var currentPath = baseDir
        for (component in packageComponents) {
            currentPath = currentPath.resolve(component)
        }

        if (!currentPath.exists()) {
            Files.createDirectories(currentPath)
        }

        currentPath = currentPath.resolve(Classes.simpleClassNameFromInternalName(internalClassName) + '.' + extension)

        return Files.newOutputStream(currentPath)
    }
}