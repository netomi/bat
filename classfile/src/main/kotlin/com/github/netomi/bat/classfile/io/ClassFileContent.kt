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

package com.github.netomi.bat.classfile.io

import com.github.netomi.bat.classfile.attribute.Attribute

abstract class ClassFileContent {
    internal abstract val dataSize: Int
    internal abstract fun write(output: ClassDataOutput)
}

fun <T: ClassFileContent> List<T>.dataSize(): Int {
    return this.fold(2) { acc, element -> acc + element.dataSize }
}

fun <T: Attribute> List<T>.attributesDataSize(): Int {
    return this.fold(2) { acc, element -> acc + 6 + element.dataSize }
}