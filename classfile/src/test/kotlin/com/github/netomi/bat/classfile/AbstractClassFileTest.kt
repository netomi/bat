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

import com.github.netomi.bat.classfile.io.ClassFileReader
import com.github.netomi.bat.util.asExternalClassName

abstract class AbstractClassFileTest {
    protected fun getPackageName(): String {
        return this.javaClass.packageName.replace('.', '/')
    }

    protected fun loadClassFile(simpleName: String): ClassFile {
        val fullyQualifiedClassName = (this.javaClass.packageName + "." + simpleName).asExternalClassName().toInternalClassName()
        val `is` = this.javaClass.classLoader.getResourceAsStream("$fullyQualifiedClassName.class")

        `is`?.use {
            val classFile       = ClassFile.empty()
            val classFileReader = ClassFileReader(`is`)
            classFileReader.visitClassFile(classFile)
            return classFile
        } ?: error("could not load class '$fullyQualifiedClassName'")
    }
}