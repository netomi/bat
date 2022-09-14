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

package com.github.netomi.bat.classfile

import java.nio.file.Paths
import java.util.zip.ZipInputStream
import kotlin.io.path.inputStream

fun main(args: Array<String>) {
    val path = Paths.get("/home/tn/workspace/android_sdk/platforms/android-33/android.jar")
    //val path = Paths.get("/home/tn/.sdkman/candidates/java/current/jmods/java.base.jmod")

    val `is` = path.inputStream()
    //`is`.skip(4)

    val pool = mutableListOf<ClassFile>()

    ZipInputStream(`is`).use { zis ->
        generateSequence { zis.nextEntry }
            .filterNot { it.isDirectory }
            .filter { it.name.endsWith(".class") }
            .map {
                println(it.name)

                val classFile = ClassFile.read(zis, false)
                pool.add(classFile)
                zis.closeEntry()
            }.toList()
    }

    println("read ${pool.size} class files")
}
