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

class FileOutputStreamFactory(private val baseDir: Path, private val extension: String) : OutputStreamFactory {
    @Throws(IOException::class)
    override fun createOutputStream(className: String): OutputStream {
        val packageComponents =
            Classes.internalPackageNameFromInternalName(className)
                   .split("/".toRegex())
                   .dropLastWhile { it.isEmpty() }
                   .toTypedArray()

        var currentPath = baseDir
        for (component in packageComponents) {
            currentPath = currentPath.resolve(component)
        }

        Files.createDirectories(currentPath)
        currentPath = currentPath.resolve(Classes.simpleClassNameFromInternalName(className) + '.' + extension)

        return Files.newOutputStream(currentPath)
    }
}